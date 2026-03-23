# yMessages

Lightweight announcement plugin for Velocity with a clean, maintainable configuration layout.

## Features

- Automatic announcements with configurable interval and selection mode.
- Selection mode: random or round-robin.
- Server-wide delivery modes:
  - Chat
  - Actionbar
  - Title
  - Bossbar (auto mode is dynamic and fills from 0% to 100% across each interval)
- MiniMessage support everywhere.
- Runtime modern-text conversion tag:
  - `<modern>your text</modern>`
  - Output style: `ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢ`
  - Conversion is render-time only, config content is never rewritten.
- Optional sound effects on each announcement.
- Configurable bossbar animation speed (updates per second).
- Debug mode for verbose logging.

## Build

```bash
gradle build
```

Output jar:

`build/libs/ymessages-1.0.0.jar`

## Install

1. Build the jar.
2. Put it in your Velocity `plugins` folder.
3. Start/restart proxy once to generate config.
4. Edit `plugins/ymessages/config.yml`.
5. Reload with command.

## Config Structure

The configuration is organized into separate sections for each announcement type.

**Global Settings:**

- `config-version`: Schema version (currently 2). Used for tracking backward compatibility.
- `debug`: Enable verbose logging for announcements and scheduler operations (default: false).
- `announcer`: scheduler behavior (`enabled`, `interval-seconds`, `selection: random|round-robin`).
- `command`: command enable toggle, aliases, and permission behavior.

**Announcement Sections** (define defaults + announcement lists):

- `chat` → list of announcement text messages (MiniMessage supported).
- `actionbar` → list of actionbar text messages (MiniMessage supported).
- `title` → defaults for title timing (fade-in-ms, stay-ms, fade-out-ms) + list of title/subtitle pairs.
- `bossbar` → defaults for color, overlay style, and animation-speed + list of dynamic progress bar messages.

### Example Config

```yaml
# yMessages Configuration
# Config Version: This is tracked for future schema upgrades.
config-version: 2

# Debug mode: Enable for verbose logging
debug: false

announcer:
  enabled: true
  interval-seconds: 120
  selection: random

command:
  enabled: true
  aliases: ["vannounce", "vannouce", "announce"]
  require-permission: false
  permission: "ymessages.admin"

chat:
  announcements:
    - message: "<modern>join our discord</modern> <gray>-</gray> <aqua>discord.gg/example"

actionbar:
  announcements:
    - message: "<green>Store:</green> <modern>shop.example.net</modern>"

title:
  defaults:
    fade-in-ms: 400
    stay-ms: 2200
    fade-out-ms: 500
  announcements:
    - title: "<gold><modern>weekly events</modern>"
      subtitle: "<yellow>Saturday 8PM UTC"

bossbar:
  defaults:
    color: blue
    overlay: progress
    animation-speed: 5  # Updates per second (higher = smoother but more updates)
  announcements:
    - message: "<aqua><modern>double rewards</modern> <gray>(<progress>)"
    - message: "<green><modern>playtime meter</modern> <white>{progress}%"
      color: green
      sound: entity_player_levelup  # Optional sound effect on announcement
```

### Sound Effects (Optional)

Any announcement can include a sound effect that plays when the announcement is broadcast:

```yaml
chat:
  announcements:
    - message: "An important update!"
      sound: ui_toast_in  # Plays when broadcasted
```

Common sound names: `ui_button_click`, `ui_toast_in`, `ui_toast_out`, `entity_experience_orb_pickup`, `entity_player_levelup`, `block_note_block_bell`, `block_note_block_xylophone`.

See [Minecraft Sound List](https://github.com/PaperMC/Paper/blob/master/paper-api/src/main/java/org/bukkit/Sound.java) for all available sounds.

### Bossbar Animation Speed

Control how smoothly the progress bar animates with `animation-speed` (updates per second):

- `animation-speed: 1` → Update once per second (less smooth, fewer network updates)
- `animation-speed: 5` → Update 5 times per second (balanced, default)
- `animation-speed: 10` → Update 10 times per second (very smooth, more network pressure)

### Debug Mode

Enable debug mode to see detailed logs about announcement scheduling and broadcasts:

```yaml
debug: true
```

Logs will show:
- When announcements are selected (random vs round-robin)
- When bossbars start with their animation settings
- Any sound effect issues

### MiniMessage & Modern Tag

All announcement text supports **MiniMessage** formatting and the custom `<modern>...</modern>` tag:

- Example: `<modern>your text</modern>` → `ʏᴏᴜʀ ᴛᴇхᴛ` (Unicode smallcaps)
- Conversion occurs at render-time; config files are never modified.

### Bossbar Progress Tokens

For bossbar messages, use dynamic progress placeholders:

- `<progress>` → replaced with percentage (e.g., `"64%"`).
- `{progress}` → replaced with number only (e.g., `"64"`).

Bossbars fill 0% → 100% over the full `announcer.interval-seconds` duration.

## Commands

- `/vannounce reload`
- `/vannounce send <chat|actionbar|title|bossbar> <message>`
- Title subtitle separator for command send: `|`

Default aliases include `vannounce`, `vannouce` (typo-friendly), and `announce`.

Permission behavior is configurable:

- `command.require-permission: false` means everyone can use command.
- `command.require-permission: true` + `command.permission: ymessages.admin` enforces permission.

Example:

`/vannounce send title <modern>weekly event</modern> | <yellow>Saturday 8PM UTC`

## Permission

- `ymessages.admin`

## Compatibility Note

The plugin accepts both the new config keys and the previous legacy keys (`auto-announcement` and `announcements`) for smoother upgrades.