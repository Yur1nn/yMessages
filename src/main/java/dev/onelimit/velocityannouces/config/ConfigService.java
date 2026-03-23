package dev.onelimit.velocityannouces.config;

import dev.onelimit.velocityannouces.model.AnnounceMode;
import dev.onelimit.velocityannouces.model.AnnouncementTypeConfig;
import dev.onelimit.velocityannouces.model.PluginConfig;
import dev.onelimit.ycore.velocity.api.config.YamlConfigLoader;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConfigService {
    private final YamlConfigLoader<PluginConfig> configLoader;

    public ConfigService(Logger logger, Path dataDirectory) {
        this.configLoader = new YamlConfigLoader<>(
            logger,
            dataDirectory,
            "config.yml",
            "config.yml",
            this::parse,
            PluginConfig::defaults
        );
    }

    public PluginConfig load() {
        return configLoader.load();
    }

    @SuppressWarnings("unchecked")
    private PluginConfig parse(Map<?, ?> root) {
        int configVersion = integer(root.get("config-version"), 2);
        boolean debug = bool(root.get("debug"), false);

        Map<String, Object> command = map(root.get("command"));
        boolean commandEnabled = bool(command.get("enabled"), true);
        boolean commandRequirePermission = bool(command.get("require-permission"), false);
        String commandPermission = string(command.get("permission"), "velocityannouces.admin");

        List<String> aliases = new ArrayList<>();
        Object aliasesNode = command.get("aliases");
        if (aliasesNode instanceof List<?> aliasList) {
            for (Object alias : aliasList) {
                if (alias != null) {
                    String value = String.valueOf(alias).trim();
                    if (!value.isEmpty()) {
                        aliases.add(value.toLowerCase());
                    }
                }
            }
        }
        if (aliases.isEmpty()) {
            aliases.add("announce");
            aliases.add("vannounce");
        }

        AnnouncementTypeConfig chatConfig = parseTypeConfig(root, "chat", AnnounceMode.CHAT);
        AnnouncementTypeConfig actionbarConfig = parseTypeConfig(root, "actionbar", AnnounceMode.ACTIONBAR);
        AnnouncementTypeConfig titleConfig = parseTypeConfig(root, "title", AnnounceMode.TITLE);
        AnnouncementTypeConfig bossbarConfig = parseTypeConfig(root, "bossbar", AnnounceMode.BOSSBAR);

        return new PluginConfig(
            configVersion,
            debug,
            commandEnabled,
            aliases,
            commandRequirePermission,
            commandPermission,
            chatConfig,
            actionbarConfig,
            titleConfig,
            bossbarConfig
        );
    }

    private AnnouncementTypeConfig parseTypeConfig(Map<?, ?> root, String typeKey, AnnounceMode mode) {
        Map<String, Object> section = map(root.get(typeKey));
        
        boolean enabled = bool(section.get("enabled"), true);
        int intervalSeconds = Math.max(5, integer(section.get("interval-seconds"), 120));
        boolean randomSelection = !"round-robin".equalsIgnoreCase(string(section.get("selection"), "random").trim());
        
        List<String> messages = new ArrayList<>();
        Object messagesNode = section.get("messages");
        if (messagesNode instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String str) {
                    messages.add(str);
                } else if (item instanceof Map<?, ?> map && mode == AnnounceMode.TITLE) {
                    // Title messages have title/subtitle structure
                    String title = string(map.get("title"), "");
                    String subtitle = string(map.get("subtitle"), "");
                    if (!title.isEmpty()) {
                        messages.add(title + "|" + subtitle);
                    }
                } else if (item instanceof Map<?, ?> map && mode == AnnounceMode.BOSSBAR) {
                    // Bossbar messages are defined as maps with a "message" field.
                    String message = string(map.get("message"), "");
                    if (!message.isEmpty()) {
                        messages.add(message);
                    }
                }
            }
        }

        int fadeInMs = integer(section.get("fade-in-ms"), 400);
        int stayMs = integer(section.get("stay-ms"), 2200);
        int fadeOutMs = integer(section.get("fade-out-ms"), 500);

        Map<String, Object> defaults = map(section.get("defaults"));
        String defaultColor = string(defaults.get("color"), "blue");
        String defaultOverlay = string(defaults.get("overlay"), "progress");
        int animationSpeed = integer(defaults.get("animation-speed"), 5);

        return new AnnouncementTypeConfig(
            enabled,
            intervalSeconds,
            randomSelection,
            messages,
            fadeInMs,
            stayMs,
            fadeOutMs,
            defaultColor,
            defaultOverlay,
            animationSpeed
        );
    }

    private Map<String, Object> map(Object input) {
        if (input instanceof Map<?, ?> source) {
            return (Map<String, Object>) source;
        }
        return Map.of();
    }

    private Map<String, Object> firstMap(Map<?, ?> root, String... keys) {
        for (String key : keys) {
            Map<String, Object> mapped = map(root.get(key));
            if (!mapped.isEmpty()) {
                return mapped;
            }
        }
        return Map.of();
    }

    private String string(Object input, String fallback) {
        if (input == null) {
            return fallback;
        }
        String value = String.valueOf(input);
        return value.isEmpty() ? fallback : value;
    }

    private int integer(Object input, int fallback) {
        if (input == null) {
            return fallback;
        }
        if (input instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(input));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private float decimal(Object input, float fallback) {
        if (input == null) {
            return fallback;
        }
        if (input instanceof Number n) {
            return n.floatValue();
        }
        try {
            return Float.parseFloat(String.valueOf(input));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private boolean bool(Object input, boolean fallback) {
        if (input == null) {
            return fallback;
        }
        if (input instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(String.valueOf(input));
    }

    private boolean parseSelection(Map<String, Object> announcer) {
        String selection = string(announcer.get("selection"), "").trim().toLowerCase();
        if (selection.isEmpty()) {
            return bool(announcer.get("random-pick"), true);
        }
        return !"round-robin".equals(selection);
    }
}
