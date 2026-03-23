# VelocityAnnouces

Lightweight announcement plugin for Velocity with a clean, maintainable configuration layout.

## Features

- Automatic announcements with configurable interval and selection mode.
- Selection mode: random or round-robin.
- Server-wide delivery modes:
  - Chat
  - Actionbar
  - Title
  - Bossbar (timed hide to avoid interfering with normal bossbar flow)
- MiniMessage support everywhere.
- Runtime modern-text conversion tag:
  - `<modern>your text</modern>`
  - Output style: `ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡхʏᴢ`
  - Conversion is render-time only, config content is never rewritten.

## Build

```bash
gradle build
```

Output jar:

`build/libs/VelocityAnnouces-1.0.0.jar`

## Install

1. Build the jar.
2. Put it in your Velocity `plugins` folder.
3. Start/restart proxy once to generate config.
4. Edit `plugins/velocityannouces/config.yml`.
5. Reload with command.

## Config Structure

- `announcer`: scheduler behavior (`enabled`, `interval-seconds`, `selection`).
- `delivery`: shared defaults for title and bossbar rendering.
- `command`: command enable toggle + aliases.
- `command`: command enable toggle, aliases, and permission behavior.
- `messages.auto`: announcement list.

Each message entry supports MiniMessage and `<modern>...</modern>`.

Message type keys:

- `chat`
- `actionbar`
- `title`
- `bossbar`

## Commands

- `/vannounce reload`
- `/vannounce send <chat|actionbar|title|bossbar> <message>`
- Title subtitle separator for command send: `|`

Default aliases include `vannounce`, `vannouce` (typo-friendly), and `announce`.

Permission behavior is configurable:

- `command.require-permission: false` means everyone can use command.
- `command.require-permission: true` + `command.permission: velocityannouces.admin` enforces permission.

Example:

`/vannounce send title <modern>weekly event</modern> | <yellow>Saturday 8PM UTC`

## Permission

- `velocityannouces.admin`

## Compatibility Note

The plugin accepts both the new config keys and the previous legacy keys (`auto-announcement` and `announcements`) for smoother upgrades.