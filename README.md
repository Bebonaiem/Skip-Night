
# ☀️ SkipNight v1.1
SkipNight is a lightweight and highly configurable Minecraft plugin that allows players on a server to democratically vote to skip the night. It provides a modern, feature-rich experience with a live GUI, clickable messages, and per-world voting, offering a seamless way for communities to bypass the dark.

## ✨ Key Features
### ⭐ Advanced Features
**📊 Live Vote GUI:** Use /skipnight gui to open an interactive menu showing a live timer and the heads of all players, indicating who has and hasn't voted.
**🖱️ Clickable Chat Voting:** When a vote starts, a clickable message is sent in chat, allowing players to vote instantly without typing a command.
**🌍 Per-World Voting:** Votes are handled independently for each Overworld. A vote in one world won't interfere with another, perfect for multi-world servers!
**� Blacklisted Worlds:** Easily disable the plugin in specific worlds (e.g., creative, event arenas) via the config.
**� Persistent Statistics:** Vote counts and success rates are saved in config.yml, so your server's history persists through restarts.

### ⚡ Core Functionality
**🌐 Multi-Version Support:** Works seamlessly on Minecraft versions from 1.16.5 to 1.21+.
**🌅 Automatic Vote Start:** Can be configured to automatically begin a vote as soon as night begins.
**🛌 Multiple Ways to Vote:** Players can vote using commands, the GUI, clickable messages, or by simply sleeping in a bed.
**⚙️ Highly Configurable:** Adjust percentage requirements, vote duration, cooldowns, and more.

## 📥 Installation Guide
1. 🔽 Download SkipNight.jar from the releases page.
2. 📂 Place the .jar file into your server's plugins/ folder.
3. 🔄 Restart your server to generate the default configuration files.
4. ⚙️ Edit plugins/SkipNight/config.yml to customize settings to your liking.
5. 🔃 Reload the configuration with /skipnight reload or restart the server.

## ⚙️ Configuration Details
### 🌙 Vote Settings
```yaml
vote-settings:
  auto-start-vote-at-night: true
  required-percentage: 50
  vote-duration-seconds: 30
  vote-start-cooldown-seconds: 300
  night-start-tick: 12541
  night-end-tick: 23458
  # Add worlds here where you DON'T want the plugin to work.
  blacklisted-worlds:
    - "world_the_end"
    - "world_nether"
    - "some_event_world"
```

### 💬 Messages
The example below shows just a portion of the fully customizable messages.
```yaml
messages:
  prefix: "&6&l[Skip Night] &r"
  
  # --- Vote Lifecycle ---
  vote-started:
    - "{prefix}&eA vote to skip the night has started in your world!"
    - "{prefix}&e{percentage}% of players need to agree."
    - "{prefix}&eClick the button below or type &a/skipnight&e to vote!"
  vote-started-clickable: "{prefix}&a&l[Click Here to Vote Yes]"
  vote-ended-success: "{prefix}&aThe vote passed! Skipping the night..."
  vote-ended-failure: "{prefix}&cThe vote has ended. Not enough players voted..."
  time-remaining: "{prefix}&eTime remaining: &c{time} seconds"
  vote-progress: "{prefix}&eCurrent votes: &a{current_votes}/{online_players} &e- Need {percentage}%"

  # --- Player Feedback & Errors ---
  voted-successfully: "{prefix}&aYou voted to skip the night!"
  already-voted: "{prefix}&cYou have already voted!"
  no-permission: "&cYou do not have permission to use this command."
  error-cooldown: "{prefix}&cYou must wait before starting another vote!"
  error-world-blacklisted: "{prefix}&cNight skipping is disabled in this world."
  
  # --- GUI & Stats ---
  gui-title: "&8Skip Night Vote Status"
  stats-header: "&6&lSkip Night Global Statistics:"
  # ...and every other message is customizable!
```

### 💾 Statistics
```yaml
# Do not edit this section manually! It's used to store plugin data.
statistics:
  total-votes-started: 0
  successful-votes: 0
```

## 🔑 Permission Nodes

| Permission         | Description                                      | Default |
|--------------------|--------------------------------------------------|---------|
| skipnight.vote     | Allows a player to start and participate in votes.| true    |
| skipnight.gui      | Allows a player to use the /skipnight gui command.| true    |
| skipnight.stats    | Allows a player to view voting statistics.        | true    |
| skipnight.reload   | Allows an admin to reload the plugin's config.    | op      |
| skipnight.*        | Grants all permissions for the plugin.            | op      |

## 🕹️ Usage Guide
### 👨‍💻 Player Commands
/skipnight - Starts a vote or votes 'yes' in an ongoing vote. (Alias: /sn, /skip)
/skipnight gui - Opens the live vote status GUI for the current world.
/skipnightstats - Shows global, persistent voting statistics.

### 👨‍💼 Admin Commands
/skipnight reload - Reloads the config.yml file from disk.

## 🛠️ Technical Specifications
**📦 Dependencies:**
SpigotMC API 1.16.5+
**📊 Data Storage:**
Configuration File (config.yml): All statistics are saved to disk and persist through restarts.
**🔧 Technical Details:**
Java 17+ required.
Built with Maven.

## ❓ FAQ
**Q: Do statistics save after the server restarts?**
A: Yes! All vote statistics are saved in config.yml and will persist through server restarts.
**Q: How do I disable voting in my creative world?**
A: Simply add the name of your creative world to the blacklisted-worlds list in config.yml and reload the plugin.
**Q: Can I change what time the plugin considers to be "night"?**
A: Yes, you can fine-tune the night-start-tick and night-end-tick values in the configuration.
**Q: What happens if a player who voted leaves or changes worlds?**
A: The plugin automatically updates the vote count for the world they were in, ensuring the percentage required is always accurate for the players currently present.

## 🌟 Pro Tips
Encourage players to use /skipnight gui for a fun, visual way to track the vote's progress.
Customize the messages and the prefix in the config to match your server's theme and colors.
The required-percentage can be adjusted based on your community size. A lower value works well for smaller, cooperative servers, while a higher value may be better for larger ones.

## 🤝 Support & Contributing
Found a bug or have a feature request? Please report it on the project's GitHub Issues page. Contributions are welcome via pull requests!

## 📜 License
This project is licensed under the MIT License.

## 📌 Version Information
Current Version: 1.1
Minecraft Version: 1.16.5 - 1.21+
API Version: 1.16
Java Version: 17+

## 🏗️ Building from Source
Clone the repository from GitHub.
Run mvn clean package in the project's root directory.
Find the compiled .jar file in the target/ folder.


