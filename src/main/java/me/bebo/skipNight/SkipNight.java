package me.bebo.skipNight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SkipNight extends JavaPlugin implements Listener, CommandExecutor {

    private final Map<UUID, Boolean> votes = new HashMap<>();
    private final Map<UUID, Long> lastVoteTime = new HashMap<>();
    private final Map<World, Boolean> worldIsNight = new HashMap<>();
    private int totalVotesStarted = 0;
    private int successfulVotes = 0;
    private boolean voteInProgress = false;
    private int timeLeft;
    private BukkitTask voteTimerTask;

    // Configurable values
    private boolean autoStartVote;
    private int requiredPercentage;
    private int voteDuration;
    private int voteCooldown;
    private long nightStartTick;
    private long nightEndTick;
    private Map<String, String> messages;
    private List<String> voteStartedMessages;

    @Override
    public void onEnable() {
        // Print your name to the console
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "                ██████╗ ███████╗██████╗  ██████╗      ██████╗ ██████╗ ██████╗ ███████╗");
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "                ██╔══██╗██╔════╝██╔══██╗██╔═══██╗    ██╔════╝██╔═══██╗██╔══██╗██╔════╝");
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "                ██████╔╝█████╗  ██████╔╝██║   ██║    ██║     ██║   ██║██║  ██║█████╗  ");
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "                ██╔══██╗██╔══╝  ██╔══██╗██║   ██║    ██║     ██║   ██║██║  ██║██╔══╝  ");
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "                ██████╔╝███████╗██████╔╝╚██████╔╝    ╚██████╗╚██████╔╝██████╔╝███████╗");
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "                ╚═════╝ ╚══════╝╚═════╝  ╚═════╝      ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝");
        getServer().getConsoleSender().sendMessage(""); // Empty line for spacing

        // Save and load config
        saveDefaultConfig();
        loadConfigValues(); // reloadConfig() is called inside loadConfigValues()

        // Register events and commands
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("skipnight").setExecutor(this);
        getCommand("skipnightstats").setExecutor(this);

        // Start the automatic night checker
        startDayNightChecker();

        getLogger().info("SkipNight plugin by Bebo has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (voteTimerTask != null && !voteTimerTask.isCancelled()) {
            voteTimerTask.cancel();
        }
        Bukkit.getScheduler().cancelTasks(this); // Cancel all tasks on disable
        votes.clear();
        lastVoteTime.clear();
        worldIsNight.clear();
        getLogger().info("SkipNight has been disabled!");
    }

    private void loadConfigValues() {
        reloadConfig(); // Make sure we have the latest config
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        // Load vote settings
        requiredPercentage = config.getInt("vote-settings.required-percentage", 50);
        voteDuration = config.getInt("vote-settings.vote-duration-seconds", 30);
        voteCooldown = config.getInt("vote-settings.vote-start-cooldown-seconds", 300) * 1000;
        nightStartTick = config.getLong("vote-settings.night-start-tick", 12541);
        nightEndTick = config.getLong("vote-settings.night-end-tick", 23458);
        autoStartVote = config.getBoolean("vote-settings.auto-start-vote-at-night", true);

        // Load messages
        messages = new HashMap<>();
        if (config.getConfigurationSection("messages") != null) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, colorize(config.getString("messages." + key, "")));
            }
            voteStartedMessages = config.getStringList("messages.vote-started");
        }
    }

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
            }
            handleVoteCommand(sender);
            return true;
        } else if (command.getName().equalsIgnoreCase("skipnightstats")) {
            handleStats(sender);
            return true;
        }
        return false;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("skipnight.reload")) {
            sender.sendMessage(messages.getOrDefault("no-permission", "&cYou do not have permission."));
            return;
        }
        loadConfigValues();
        sender.sendMessage(messages.getOrDefault("reload-success", "&aConfiguration reloaded."));
    }

    private void handleStats(CommandSender sender) {
        if (!sender.hasPermission("skipnight.stats")) {
            sender.sendMessage(messages.getOrDefault("no-permission", "&cYou do not have permission."));
            return;
        }

        double successRate = (totalVotesStarted > 0) ? (successfulVotes * 100.0 / totalVotesStarted) : 0;
        sender.sendMessage(messages.getOrDefault("stats-header", "&6&lSkip Night Statistics:"));
        sender.sendMessage(messages.getOrDefault("stats-total-votes",
                        "&eTotal votes started: &a{total_started}")
                .replace("{total_started}", String.valueOf(totalVotesStarted)));
        sender.sendMessage(messages.getOrDefault("stats-successful-votes",
                        "&eSuccessful votes: &a{successful_votes}")
                .replace("{successful_votes}", String.valueOf(successfulVotes)));
        sender.sendMessage(messages.getOrDefault("stats-success-rate",
                        "&eSuccess rate: &a{success_rate}%")
                .replace("{success_rate}", String.format("%.1f", successRate)));
    }

    private void handleVoteCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getOrDefault("error-not-a-player", "&cOnly players can use this command."));
            return;
        }

        if (!player.hasPermission("skipnight.vote")) {
            player.sendMessage(messages.getOrDefault("no-permission", "&cYou do not have permission."));
            return;
        }

        if (voteInProgress) {
            if (votes.containsKey(player.getUniqueId())) {
                player.sendMessage(messages.getOrDefault("already-voted", "&cYou have already voted!"));
            } else {
                votes.put(player.getUniqueId(), true);
                player.sendMessage(colorize(messages.getOrDefault("prefix", "&6&l[Skip Night] &r") + " &aYou voted to skip the night!"));
                checkVotes(true);
            }
        } else {
            startVoteAttempt(player);
        }
    }

    private void startVoteAttempt(Player player) {
        if (voteInProgress) {
            player.sendMessage(messages.getOrDefault("error-vote-in-progress", "&cA vote is already in progress!"));
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (lastVoteTime.containsKey(player.getUniqueId()) &&
                (currentTime - lastVoteTime.get(player.getUniqueId()) < voteCooldown)) {
            player.sendMessage(messages.getOrDefault("error-cooldown", "&cYou must wait before starting another vote!"));
            return;
        }

        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL ||
                (world.getTime() < nightStartTick || world.getTime() > nightEndTick)) {
            player.sendMessage(messages.getOrDefault("error-not-night", "&cYou can only start a vote at night!"));
            return;
        }

        startVote(player);
        lastVoteTime.put(player.getUniqueId(), currentTime);
    }

    private void startVote(@Nullable Player initiator) {
        if (voteInProgress) return; // Prevent starting a vote if one is already running

        voteInProgress = true;
        timeLeft = voteDuration;
        votes.clear();
        totalVotesStarted++;

        if (initiator != null) {
            votes.put(initiator.getUniqueId(), true);
        }

        // Broadcast vote started messages
        String percentagePlaceholder = String.valueOf(requiredPercentage);
        for (String line : voteStartedMessages) {
            Bukkit.broadcastMessage(colorize(line
                    .replace("{prefix}", messages.getOrDefault("prefix", ""))
                    .replace("{percentage}", percentagePlaceholder))
            );
        }

        // Start the vote timer
        startVoteTimer();
    }

    private void startVoteTimer() {
        if (voteTimerTask != null && !voteTimerTask.isCancelled()) {
            voteTimerTask.cancel();
        }

        voteTimerTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!voteInProgress) {
                voteTimerTask.cancel();
                return;
            }

            if (timeLeft <= 0) {
                Bukkit.broadcastMessage(messages.getOrDefault("vote-ended-failure",
                        "&cThe vote has ended. Not enough players voted to skip the night."));
                endVote();
                return;
            }

            if (timeLeft == voteDuration || timeLeft % 10 == 0 || timeLeft <= 5) {
                if (timeLeft != voteDuration) { // Don't announce on first tick
                    Bukkit.broadcastMessage(messages.getOrDefault("time-remaining",
                                    "&eTime remaining: &c{time} seconds")
                            .replace("{time}", String.valueOf(timeLeft)));
                }
            }
            timeLeft--;
        }, 0L, 20L);
    }

    private void startDayNightChecker() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            // Only run if the feature is enabled, no vote is in progress, and players are online
            if (!autoStartVote || voteInProgress || Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }

            // Find the primary overworld to check the time
            World primaryWorld = null;
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() == World.Environment.NORMAL) {
                    primaryWorld = world;
                    break;
                }
            }
            if (primaryWorld == null) {
                return; // No normal world found
            }

            long time = primaryWorld.getTime();
            boolean isCurrentlyNight = (time >= nightStartTick && time <= nightEndTick);

            // Get the previous state for this world, defaulting to the current state if not present
            boolean wasNight = worldIsNight.getOrDefault(primaryWorld, isCurrentlyNight);

            // If it just became night (transition from day to night)
            if (isCurrentlyNight && !wasNight) {
                getLogger().info("Night detected. Automatically starting a vote to skip the night.");
                startVote(null); // Start a vote with no specific initiator
            }

            // Update the stored state for the world
            worldIsNight.put(primaryWorld, isCurrentlyNight);

        }, 100L, 100L); // Start after 5s, check every 5s (100 ticks)
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;

        if (voteInProgress) {
            Player player = event.getPlayer();
            if (!votes.containsKey(player.getUniqueId())) {
                votes.put(player.getUniqueId(), true);
                player.sendMessage(colorize(messages.getOrDefault("prefix", "") + " &aYou voted to skip the night by sleeping!"));
                checkVotes(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (voteInProgress) {
            // We check if the player had voted before removing them
            if (votes.remove(event.getPlayer().getUniqueId()) != null) {
                checkVotes(false); // Recalculate percentages if a voter leaves
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (voteInProgress) {
            event.getPlayer().sendMessage(colorize(messages.getOrDefault("prefix", "") +
                    " &eA vote to skip the night is in progress! Type /skipnight to vote."));
        }
    }

    private void checkVotes(boolean announceProgress) {
        if (!voteInProgress) return;

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers == 0) {
            endVote();
            return;
        }

        int agreedVotes = (int) votes.values().stream().filter(b -> b).count();
        double currentPercentage = (double) agreedVotes * 100 / onlinePlayers;

        if (currentPercentage >= requiredPercentage) {
            successfulVotes++;
            Bukkit.broadcastMessage(messages.getOrDefault("vote-ended-success",
                    "&aThe vote passed! Skipping the night..."));

            // Skip night in all normal worlds
            Bukkit.getScheduler().runTask(this, () -> {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == World.Environment.NORMAL) {
                        world.setTime(0);
                        world.setStorm(false);
                        world.setThundering(false);
                    }
                }
            });
            endVote();
        } else if (announceProgress) {
            Bukkit.broadcastMessage(messages.getOrDefault("vote-progress",
                            "&eCurrent votes: &a{current_votes}/{online_players} &e- Need {percentage}% to skip.")
                    .replace("{current_votes}", String.valueOf(agreedVotes))
                    .replace("{online_players}", String.valueOf(onlinePlayers))
                    .replace("{percentage}", String.valueOf(requiredPercentage))
            );
        }
    }

    private void endVote() {
        if (voteTimerTask != null && !voteTimerTask.isCancelled()) {
            voteTimerTask.cancel();
        }
        voteInProgress = false;
        votes.clear();
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}