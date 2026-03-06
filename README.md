# SkipNight - Vote to Skip the Night! ☀️
Let your players democratically vote to skip the night with a modern, feature-rich plugin!

## 🔹 Description
SkipNight is a lightweight and highly configurable plugin that allows players to initiate a vote to fast-forward through the night. It's a simple, fair, and modern solution for any survival server, giving your community control over the day/night cycle with an amazing user experience, including multi-world support, clickable messages, a real-time vote GUI, boss bar timers, sound effects, and a powerful reward system!

## ✨ Key Features
✔ **YES/NO Voting System** – Players can vote YES or NO, not just abstain! Full democratic control.  
✔ **Interactive Voting** – Vote with `/skipnight yes/no`, sleeping in a bed, or clicking chat buttons!  
✔ **Boss Bar Timer** – Visual countdown at the top of the screen showing votes and time remaining.  
✔ **Action Bar Updates** – Real-time vote progress displayed without chat spam.  
✔ **Real-Time Vote GUI** – Use `/skipnight gui` to see who voted YES (green), NO (red), or abstained (gray).  
✔ **Sound Effects** – Configurable sounds for vote start, voting, success, and failure.  
✔ **Voter Rewards** – Reward players with XP, custom items, or money for participating!  
✔ **Flexible Thresholds** – Choose percentage mode (50% of players) or absolute mode (X number of votes).  
✔ **Instant Skip** – When 100% vote yes, skip immediately without waiting.  
✔ **Admin Controls** – Cancel ongoing votes with `/skipnight cancel`.  
✔ **Advanced Multi-World Support** – Easily blacklist worlds where you don't want night-skipping enabled.  
✔ **Automatic Nightly Votes** – Can start a vote the moment it gets dark.  
✔ **Broad Version Support** – Works on Minecraft 1.16.5 through 1.21+.  
✔ **Fully Customizable** – Change all messages, colors, sounds, rewards, and vote requirements.  
✔ **Optimized Performance** – Minimal server impact, runs smoothly.

## 📥 Installation
1. Download `SkipNight.jar` and place it in your `plugins/` folder.
2. Restart your server to generate the default config.yml.
3. Configure settings in `plugins/SkipNight/config.yml`.
4. Reload with `/skipnight reload` or restart to apply changes.

## ⚙️ Configuration (config.yml)
Customize every aspect of the plugin to fit your server perfectly.
```yaml
vote-settings:
  # Automatically start a vote when night begins?
  auto-start-vote-at-night: true
  # Threshold mode: "percentage" or "absolute"
  threshold-mode: "percentage"
  required-percentage: 50
  required-absolute-count: 3
  # Allow players to vote NO (against skipping)?
  allow-vote-against: true
  # Skip instantly when 100% vote yes?
  instant-skip-at-100-percent: true
  # Disable night-skipping in these worlds.
  blacklisted-worlds:
    - "world_the_end"
    - "world_nether"

display:
  # Show updates in action bar (less spam)?
  use-action-bar: true
  # Show timer as boss bar at top?
  use-boss-bar: true
  use-chat: true

sounds:
  vote-started: "BLOCK_NOTE_BLOCK_PLING"
  player-voted-yes: "ENTITY_EXPERIENCE_ORB_PICKUP"
  player-voted-no: "BLOCK_NOTE_BLOCK_BASS"
  vote-success: "ENTITY_PLAYER_LEVELUP"
  vote-failed: "ENTITY_VILLAGER_NO"

rewards:
  enabled: true
  only-on-success: false
  xp-levels: 1  # Set to 0 to disable
  items:
    - material: "DIAMOND"
      amount: 1
      name: "&b&lVoting Reward"
```

## ⌨️ Commands
| Command | Description |
|---------|-------------|
| /skipnight | Starts a vote or votes YES if one is active. |
| /skipnight yes | Vote YES to skip the night. |
| /skipnight no | Vote NO against skipping the night. |
| /skipnight gui | Opens the live vote status GUI. |
| /skipnight cancel | Cancel the ongoing vote (admin only). |
| /skipnight stats | Shows global voting statistics. |
| /skipnight reload | Reloads the configuration file. |
| /skipnight help | Shows all available commands. |

## 🔑 Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| skipnight.vote | Allows starting a vote | ✅ True |
| skipnight.vote.yes | Allows voting YES | ✅ True |
| skipnight.vote.no | Allows voting NO | ✅ True |
| skipnight.gui | Allows opening the vote status GUI | ✅ True |
| skipnight.stats | Allows viewing vote statistics | ✅ True |
| skipnight.cancel | Allows cancelling ongoing votes | 🔒 Op |
| skipnight.reload | Allows reloading the plugin's config | 🔒 Op |

## 🌟 Why Choose SkipNight?
✅ **Modern User Experience** – Boss bars, action bars, clickable buttons, and real-time GUI make voting engaging and intuitive.  
✅ **Full Democracy** – YES/NO voting gives players real control, not just a "yes or abstain" system.  
✅ **Reward Your Community** – Incentivize participation with XP, items, or economy rewards.  
✅ **Flexible Configuration** – Percentage or absolute thresholds, instant skip, rewards on/off, display toggles.  
✅ **Sound & Visual Effects** – Immersive feedback with configurable sounds and boss bar timers.  
✅ **Empower Your Community** – Let players decide when to skip the night democratically.  
✅ **Plug & Play** – Works right out of the box with zero setup needed.  
✅ **Modern & Maintained** – Built for modern Minecraft versions (1.16.5+).

## 🎯 Perfect For:
- **Survival & SMP Servers** – Enhance the vanilla experience with modern features.
- **Community-Focused Servers** – Promote player interaction and democracy.
- **Reward-Based Servers** – Incentivize participation with customizable rewards.
- **Any server that wants a simple, powerful way to manage the day/night cycle.**

**Lightweight ✔ Interactive ✔ Customizable ✔ Democratic ✔ Rewarding ✔**

## 📥 Download now and empower your community to control the night! ☀️🌙

---

### 🆕 What's New in v2.0?
- 🗳️ **YES/NO Voting** – Players can vote against skipping, not just abstain
- 📊 **Boss Bar Timer** – Visual countdown with live vote counts
- 💬 **Action Bar Updates** – Less chat spam, cleaner interface
- 🔊 **Sound Effects** – Customizable audio feedback for all vote events
- 🎁 **Reward System** – Give XP, items, or money to voters
- ⚡ **Instant Skip** – 100% yes votes skip immediately
- 🎯 **Threshold Modes** – Choose percentage or absolute vote counts
- 🛑 **Vote Cancellation** – Admins can cancel problematic votes
- 🎨 **Enhanced GUI** – Color-coded player heads (green/red/gray)
