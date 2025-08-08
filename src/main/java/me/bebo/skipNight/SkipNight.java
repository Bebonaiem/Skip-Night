package me.bebo.skipNight;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class SkipNight extends JavaPlugin implements Listener, CommandExecutor {

    // --- Plugin State ---
    private final Map<UUID, VoteSession> activeVotes = new HashMap<>();
    private final Map<UUID, Long> lastVoteTime = new HashMap<>();
    private final Map<UUID, Boolean> worldIsNight = new HashMap<>();

    // --- Statistics ---
    private int totalVotesStarted = 0;
    private int successfulVotes = 0;

    // --- Configurable Values ---
    private boolean autoStartVote;
    private int requiredPercentage;
    private int voteDuration;
    private long voteCooldown;
    private long nightStartTick;
    private long nightEndTick;
    private Map<String, String> messages;
    private List<String> voteStartedMessages;
    private List<String> blacklistedWorlds;

    @Override
    public void onEnable() {
        // ASCII Art & Startup Messages
        getLogger().info("Enabling SkipNight by Bebo...");

        // Config and Stats
        saveDefaultConfig();
        loadConfigValues();
        loadStats();

        // Events and Commands
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("skipnight").setExecutor(this);
        getCommand("skipnightstats").setExecutor(this);

        // Schedulers
        startDayNightChecker();

        getLogger().info("SkipNight plugin by Bebo has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Cancel all running votes and tasks
        activeVotes.values().forEach(VoteSession::cancel);
        activeVotes.clear();
        Bukkit.getScheduler().cancelTasks(this);

        // Save statistics
        saveStats();

        getLogger().info("SkipNight has been disabled!");
    }

    // --- Configuration & Data Handling ---

    private void loadConfigValues() {
        reloadConfig();
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);

        autoStartVote = config.getBoolean("vote-settings.auto-start-vote-at-night", true);
        requiredPercentage = config.getInt("vote-settings.required-percentage", 50);
        voteDuration = config.getInt("vote-settings.vote-duration-seconds", 30);
        voteCooldown = config.getInt("vote-settings.vote-start-cooldown-seconds", 300) * 1000L;
        nightStartTick = config.getLong("vote-settings.night-start-tick", 12541);
        nightEndTick = config.getLong("vote-settings.night-end-tick", 23458);
        blacklistedWorlds = config.getStringList("vote-settings.blacklisted-worlds");

        messages = new HashMap<>();
        if (config.getConfigurationSection("messages") != null) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                if (config.isString("messages." + key)) {
                    messages.put(key, config.getString("messages." + key));
                }
            }
            voteStartedMessages = config.getStringList("messages.vote-started");
        }
        saveConfig();
    }

    private void loadStats() {
        FileConfiguration config = getConfig();
        this.totalVotesStarted = config.getInt("statistics.total-votes-started", 0);
        this.successfulVotes = config.getInt("statistics.successful-votes", 0);
    }

    private void saveStats() {
        FileConfiguration config = getConfig();
        config.set("statistics.total-votes-started", this.totalVotesStarted);
        config.set("statistics.successful-votes", this.successfulVotes);
        saveConfig();
    }

    // --- Command Handling ---

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("skipnight")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    handleReload(sender);
                    return true;
                }
                if (args[0].equalsIgnoreCase("stats")) {
                    handleStats(sender);
                    return true;
                }
                if (args[0].equalsIgnoreCase("gui")) {
                    handleGui(sender);
                    return true;
                }
            }
            handleVoteCommand(sender);
            return true;
        } else if (command.getName().equalsIgnoreCase("skipnightstats")) {
            handleStats(sender);
            return true;
        }
        return false;
    }

    private void handleGui(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("error-not-a-player"));
            return;
        }
        Player player = (Player) sender;
        VoteSession session = activeVotes.get(player.getWorld().getUID());
        if (session == null) {
            player.sendMessage(formatMessage("error-no-vote-in-progress"));
            return;
        }
        session.openVoteGui(player);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("skipnight.reload")) {
            sender.sendMessage(formatMessage("no-permission"));
            return;
        }
        loadConfigValues();
        sender.sendMessage(formatMessage("reload-success"));
    }

    private void handleStats(CommandSender sender) {
        if (!sender.hasPermission("skipnight.stats")) {
            sender.sendMessage(formatMessage("no-permission"));
            return;
        }

        double successRate = (totalVotesStarted > 0) ? (successfulVotes * 100.0 / totalVotesStarted) : 0.0;
        sender.sendMessage(formatMessage("stats-header"));
        sender.sendMessage(formatMessage("stats-total-votes", "{total_started}", String.valueOf(totalVotesStarted)));
        sender.sendMessage(formatMessage("stats-successful-votes", "{successful_votes}", String.valueOf(successfulVotes)));
        sender.sendMessage(formatMessage("stats-success-rate", "{success_rate}", String.format("%.1f", successRate)));
    }

    private void handleVoteCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("error-not-a-player"));
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("skipnight.vote")) {
            player.sendMessage(formatMessage("no-permission"));
            return;
        }

        World world = player.getWorld();
        if (isWorldBlacklisted(world)) {
            player.sendMessage(formatMessage("error-world-blacklisted"));
            return;
        }

        if (world.getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(formatMessage("error-wrong-world"));
            return;
        }

        VoteSession session = activeVotes.get(world.getUID());

        if (session != null) {
            session.addVote(player);
        } else {
            startVoteAttempt(player);
        }
    }

    // --- Vote Logic ---

    private void startVoteAttempt(Player player) {
        World world = player.getWorld();

        if (activeVotes.containsKey(world.getUID())) {
            player.sendMessage(formatMessage("error-vote-in-progress"));
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (lastVoteTime.containsKey(player.getUniqueId()) && (currentTime - lastVoteTime.get(player.getUniqueId()) < voteCooldown)) {
            player.sendMessage(formatMessage("error-cooldown"));
            return;
        }

        if (world.getTime() < nightStartTick || world.getTime() > nightEndTick) {
            player.sendMessage(formatMessage("error-not-night"));
            return;
        }

        lastVoteTime.put(player.getUniqueId(), currentTime);
        startVote(world, player);
    }

    public void startVote(World world, @Nullable Player initiator) {
        if (isWorldBlacklisted(world) || world.getEnvironment() != World.Environment.NORMAL || activeVotes.containsKey(world.getUID())) {
            return;
        }

        VoteSession session = new VoteSession(world, initiator);
        activeVotes.put(world.getUID(), session);

        totalVotesStarted++;
        saveStats();

        session.start();
    }

    private boolean isWorldBlacklisted(World world) {
        return blacklistedWorlds.contains(world.getName());
    }


    // --- Schedulers ---

    private void startDayNightChecker() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!autoStartVote) return;

            for (World world : Bukkit.getWorlds()) {
                if (isWorldBlacklisted(world) || world.getEnvironment() != World.Environment.NORMAL || world.getPlayers().isEmpty() || activeVotes.containsKey(world.getUID())) {
                    continue;
                }

                long time = world.getTime();
                boolean isCurrentlyNight = (time >= nightStartTick && time <= nightEndTick);
                boolean wasNight = worldIsNight.getOrDefault(world.getUID(), isCurrentlyNight);

                if (isCurrentlyNight && !wasNight) {
                    getLogger().info("Night detected in world '" + world.getName() + "'. Starting a vote.");
                    startVote(world, null);
                }

                worldIsNight.put(world.getUID(), isCurrentlyNight);
            }
        }, 100L, 100L);
    }


    // --- Event Handlers ---

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(colorize(messages.getOrDefault("gui-title", "")))) {
            event.setCancelled(true); // Make the GUI read-only
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;

        Player player = event.getPlayer();
        VoteSession session = activeVotes.get(player.getWorld().getUID());
        if (session != null) {
            session.addVote(player, true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        VoteSession session = activeVotes.get(player.getWorld().getUID());
        if (session != null) {
            session.handlePlayerStateChange();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        VoteSession session = activeVotes.get(player.getWorld().getUID());
        if (session != null) {
            session.notifyPlayerOnJoin(player);
            session.handlePlayerStateChange();
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        VoteSession fromSession = activeVotes.get(event.getFrom().getUID());
        if (fromSession != null) {
            fromSession.handlePlayerStateChange();
        }

        VoteSession toSession = activeVotes.get(player.getWorld().getUID());
        if (toSession != null) {
            toSession.notifyPlayerOnJoin(player);
            toSession.handlePlayerStateChange();
        }
    }


    // --- Utility Methods ---

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String formatMessage(String key, String... placeholders) {
        String message = messages.getOrDefault(key, "&cMessage '" + key + "' not found.");
        message = message.replace("{prefix}", messages.getOrDefault("prefix", ""));

        if (placeholders.length > 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 < placeholders.length) {
                    message = message.replace(placeholders[i], placeholders[i + 1]);
                }
            }
        }
        return colorize(message);
    }

    // --- Inner Class for Managing a Vote Session ---

    private class VoteSession {
        private final World world;
        private final Set<UUID> playerVotes = new HashSet<>();
        private int timeLeft;
        private BukkitTask timerTask;
        private Inventory voteGui;

        VoteSession(World world, @Nullable Player initiator) {
            this.world = world;
            this.timeLeft = voteDuration;
            if (initiator != null) {
                playerVotes.add(initiator.getUniqueId());
            }
        }

        void start() {
            String prefix = messages.getOrDefault("prefix", "");
            // FIXED: Also get percentage to replace it in the loop
            String percentagePlaceholder = String.valueOf(requiredPercentage);
            for (String line : voteStartedMessages) {
                // FIXED: Replace both {prefix} and {percentage}
                broadcast(colorize(line.replace("{prefix}", prefix).replace("{percentage}", percentagePlaceholder)));
            }

            TextComponent clickableMessage = new TextComponent(formatMessage("vote-started-clickable"));
            clickableMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skipnight"));
            clickableMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(colorize("&eClick to vote to skip the night!")).create()));

            for(Player p : world.getPlayers()) {
                p.spigot().sendMessage(clickableMessage);
            }

            startTimer();
            checkVotes(false);
        }

        void startTimer() {
            this.timerTask = Bukkit.getScheduler().runTaskTimer(SkipNight.this, () -> {
                if (timeLeft <= 0) {
                    broadcast(formatMessage("vote-ended-failure"));
                    end();
                    return;
                }

                if (timeLeft % 10 == 0 || timeLeft <= 5) {
                    broadcast(formatMessage("time-remaining", "{time}", String.valueOf(timeLeft)));
                }

                updateGui();

                timeLeft--;
            }, 20L, 20L);
        }

        void notifyPlayerOnJoin(Player player) {
            player.sendMessage(formatMessage("vote-in-progress-on-join"));
            TextComponent clickableMessage = new TextComponent(formatMessage("vote-started-clickable"));
            clickableMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skipnight"));
            player.spigot().sendMessage(clickableMessage);
        }

        void addVote(Player player) {
            addVote(player, false);
        }

        void addVote(Player player, boolean fromBed) {
            if (playerVotes.contains(player.getUniqueId())) {
                player.sendMessage(formatMessage("already-voted"));
                return;
            }
            playerVotes.add(player.getUniqueId());
            player.sendMessage(formatMessage(fromBed ? "voted-successfully-bed" : "voted-successfully"));
            checkVotes(true);
        }

        void handlePlayerStateChange() {
            Bukkit.getScheduler().runTaskLater(SkipNight.this, () -> checkVotes(true), 1L);
        }

        void checkVotes(boolean announceProgress) {
            List<Player> playersInWorld = world.getPlayers();
            int onlineInWorld = playersInWorld.size();

            if (onlineInWorld == 0) {
                end();
                return;
            }

            long agreedVotes = playerVotes.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).getWorld().equals(world)).count();
            double currentPercentage = (double) agreedVotes * 100 / onlineInWorld;

            if (currentPercentage >= requiredPercentage) {
                broadcast(formatMessage("vote-ended-success"));
                successfulVotes++;
                saveStats();

                Bukkit.getScheduler().runTask(SkipNight.this, () -> {
                    world.setTime(0);
                    world.setStorm(false);
                    world.setThundering(false);
                });
                end();
            } else if (announceProgress) {
                broadcast(formatMessage("vote-progress",
                        "{current_votes}", String.valueOf(agreedVotes),
                        "{online_players}", String.valueOf(onlineInWorld),
                        "{percentage}", String.valueOf(requiredPercentage)
                ));
            }
            updateGui();
        }

        void openVoteGui(Player player) {
            if(voteGui == null) {
                createGui();
            }
            updateGui();
            player.openInventory(voteGui);
        }

        void createGui() {
            voteGui = Bukkit.createInventory(null, 54, colorize(messages.getOrDefault("gui-title", "")));
        }

        void updateGui() {
            if (voteGui == null) createGui();
            voteGui.clear();

            List<Player> playersInWorld = world.getPlayers();
            long agreedVotesCount = playerVotes.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).count();

            ItemStack infoItem = new ItemStack(Material.CLOCK);
            ItemMeta infoMeta = infoItem.getItemMeta();
            infoMeta.setDisplayName(colorize("&eVote Status"));
            List<String> lore = new ArrayList<>();
            lore.add(colorize("&7Time Remaining: &c" + timeLeft + "s"));
            lore.add(colorize("&7Votes: &a" + agreedVotesCount + "&7/&c" + playersInWorld.size()));
            lore.add(colorize("&7Required: &b" + requiredPercentage + "%"));
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
            voteGui.setItem(4, infoItem);

            for(Player p : playersInWorld) {
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                skullMeta.setOwningPlayer(p);
                boolean hasVoted = playerVotes.contains(p.getUniqueId());
                skullMeta.setDisplayName(colorize((hasVoted ? "&a" : "&c") + p.getName()));
                List<String> skullLore = new ArrayList<>();
                skullLore.add(hasVoted ? colorize("&a✔ Voted Yes") : colorize("&c✗ Has Not Voted"));
                skullMeta.setLore(skullLore);
                playerHead.setItemMeta(skullMeta);
                voteGui.addItem(playerHead);
            }
        }

        void end() {
            cancel();
            activeVotes.remove(this.world.getUID());
            if (voteGui != null) {
                new ArrayList<>(voteGui.getViewers()).forEach(human -> human.closeInventory());
            }
        }

        void cancel() {
            if (timerTask != null && !timerTask.isCancelled()) {
                timerTask.cancel();
            }
        }

        void broadcast(String message) {
            for (Player player : this.world.getPlayers()) {
                player.sendMessage(message);
            }
        }
    }
}