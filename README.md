# ☀️ SkipNight v1.0

SkipNight is a lightweight and highly configurable Minecraft plugin that allows players on a server to democratically vote to skip the night. When night falls, a vote can be initiated either automatically or by a player, providing a seamless way for communities to bypass the dark without requiring every single player to sleep.

## ✨ Key Features

### ⚡ Core Functionality
- **🌐 Multi-Version Support:** Works seamlessly on Minecraft versions from 1.16.5 to 1.21+.
- **🗳️ Player-Driven Voting:** Allows any player with permission to start or participate in a vote to skip the night.
- **🌅 Automatic Vote Start:** Can be configured to automatically begin a vote as soon as night begins.
- **🛌 Multiple Ways to Vote:** Players can vote using the /skipnight command or by simply sleeping in a bed.
- **📊 Real-time Vote Tracking:** Broadcasts vote progress to all players as more people vote.
- **⚙️ Highly Configurable:** Adjust percentage requirements, vote duration, cooldowns, and more.

### 📈 Statistics System
- **📈 Vote Tracking:** Records the total number of votes started and how many were successful.
- **📊 Success Rate:** Automatically calculates and displays the vote success rate.
- **💬 In-Game Command:** Use /skipnightstats to view the current session's voting statistics.

### 🎨 Customization Options
- **💬 Custom Messages:** Almost every message sent to players is fully customizable, with color code support.
- **⏱️ Adjustable Timings:** Control the vote duration and the cooldown period between manually started votes.
- **🎯 Configurable Thresholds:** Define the exact percentage of players needed for a vote to pass.
- **🌙 Definable Night Time:** Set the exact game ticks for when night is considered to have started and ended.

## 📥 Installation Guide

1. 🔽 Download the latest SkipNight.jar from the releases page.
2. 📂 Place the .jar file into your server's plugins/ folder.
3. 🔄 Restart your server to generate the default configuration files.
4. ⚙️ Edit plugins/SkipNight/config.yml to customize settings to your liking.
5. 🔃 Reload the configuration with /skipnight reload or restart the server.

## ⚙️ Configuration Details

### 🌙 Vote Settings
```yaml
vote-settings:
  # Enable this to automatically start a vote when night begins.
  auto-start-vote-at-night: true
  # The percentage of online players required to vote yes.
  required-percentage: 50
  # How long the vote lasts in seconds.
  vote-duration-seconds: 30
  # Cooldown in seconds before a player can start another vote manually.
  vote-start-cooldown-seconds: 300
  # The game tick when night is considered to have started (Default: 12541).
  night-start-tick: 12541
  # The game tick when night is considered to have ended (Default: 23458).
  night-end-tick: 23458
```

### 💬 Messages
```yaml
messages:
  prefix: "&6&l[Skip Night] &r"
  vote-started:
    - "{prefix}&eA vote to skip the night has started!"
    - "{prefix}&eType &a/skipnight &eor sleep in a bed to vote!"
    - "{prefix}&e{percentage}% of online players need to agree to skip the night."
  vote-ended-failure: "{prefix}&cThe vote has ended. Not enough players voted to skip the night."
  vote-ended-success: "{prefix}&aThe vote passed! Skipping the night..."
  time-remaining: "{prefix}&eTime remaining: &c{time} seconds"
  vote-progress: "{prefix}&eCurrent votes: &a{current_votes}/{online_players} &e- Need {percentage}% to skip."
  already-voted: "{prefix}&cYou have already voted!"
  # ... and many more!
```

## 🔑 Permission Nodes

| Permission | Description | Default |
|------------|-------------|---------|
| skipnight.vote | Allows a player to start and participate in votes. | true |
| skipnight.stats | Allows a player to view voting statistics. | true |
| skipnight.reload | Allows an admin to reload the plugin's config. | op |

## 🕹️ Usage Guide

### 👨‍💻 Player Commands
- `/skipnight` - Starts a vote or votes 'yes' in an ongoing vote. (Alias: /sn)
- `/skipnightstats` - Shows statistics for votes in the current server session.

### 👨‍💼 Admin Commands
- `/skipnight reload` - Reloads the config.yml file from disk.

## 🛠️ Technical Specifications

### 📦 Dependencies:
- SpigotMC API 1.16.5+
- JetBrains Annotations (for development, provided)

### ⚙️ API Support:
- This is a standalone plugin with no required API dependencies.

### 📊 Data Storage:
- In-memory storage for voting statistics (resets on server restart).

### 🔧 Technical Details:
- Java 17+ required.
- Built with Maven.

## ❓ FAQ

**Q: Can I disable the automatic voting at night?**  
A: Yes! Simply set auto-start-vote-at-night: false in the config.yml.

**Q: Do statistics save after the server restarts?**  
A: No, all statistics are stored in-memory and are reset when the server stops or restarts.

**Q: What happens if a player who voted leaves the server?**  
A: The plugin automatically removes their vote and recalculates the required percentage based on the new online player count.

**Q: Can I change what time the plugin considers to be "night"?**  
A: Yes, you can fine-tune the night-start-tick and night-end-tick values in the configuration.

## 🌟 Pro Tips
- Adjust the required-percentage based on your community size and playstyle. A lower percentage works well for smaller, cooperative servers.
- Customize the messages and the prefix in the config to match your server's theme and colors.
- Inform your players that sleeping in a bed also counts as a 'yes' vote during an active poll.

## 🤝 Support & Contributing
Found a bug or have a feature request? Please report it on the project's GitHub Issues page. Contributions are welcome via pull requests!

## 📜 License
This project is licensed under the MIT License.

## 📌 Version Information
- Current Version: 1.0
- Minecraft Version: 1.16.5 - 1.21+
- API Version: 1.16
- Java Version: 17+

## 🏗️ Building from Source
1. Clone the repository from GitHub.
2. Run `mvn clean package` in the project's root directory.
3. Find the compiled .jar file in the target/ folder.

## ⚠️ Known Limitations
- Statistics are not persistent and will reset on every server restart.
- The automatic night-check is based on the primary overworld's time, which is standard for most server setups.

