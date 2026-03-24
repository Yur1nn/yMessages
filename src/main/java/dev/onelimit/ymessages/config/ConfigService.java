package dev.onelimit.ymessages.config;

import dev.onelimit.ymessages.model.AnnounceMode;
import dev.onelimit.ymessages.model.AnnouncementTypeConfig;
import dev.onelimit.ymessages.model.PluginConfig;
import dev.onelimit.ycore.velocity.api.config.ConfigValueReader;
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

    private PluginConfig parse(Map<?, ?> root) {
        int configVersion = ConfigValueReader.integer(root.get("config-version"), 2);
        boolean debug = ConfigValueReader.bool(root.get("debug"), false);

        Map<String, Object> command = ConfigValueReader.map(root.get("command"));
        boolean commandEnabled = ConfigValueReader.bool(command.get("enabled"), true);
        boolean commandRequirePermission = ConfigValueReader.bool(command.get("require-permission"), false);
        String commandPermission = ConfigValueReader.string(command.get("permission"), "ymessages.admin");

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
        Map<String, Object> section = ConfigValueReader.map(root.get(typeKey));
        
        boolean enabled = ConfigValueReader.bool(section.get("enabled"), true);
        int intervalSeconds = Math.max(5, ConfigValueReader.integer(section.get("interval-seconds"), 120));
        boolean randomSelection = !"round-robin".equalsIgnoreCase(ConfigValueReader.string(section.get("selection"), "random").trim());
        
        List<String> messages = new ArrayList<>();
        Object messagesNode = section.get("messages");
        if (messagesNode instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String str) {
                    messages.add(str);
                } else if (item instanceof Map<?, ?> map && mode == AnnounceMode.TITLE) {
                    // Title messages have title/subtitle structure
                    String title = ConfigValueReader.string(map.get("title"), "");
                    String subtitle = ConfigValueReader.string(map.get("subtitle"), "");
                    if (!title.isEmpty()) {
                        messages.add(title + "|" + subtitle);
                    }
                } else if (item instanceof Map<?, ?> map && mode == AnnounceMode.BOSSBAR) {
                    // Bossbar messages are defined as maps with a "message" field.
                    String message = ConfigValueReader.string(map.get("message"), "");
                    if (!message.isEmpty()) {
                        messages.add(message);
                    }
                }
            }
        }

        int fadeInMs = ConfigValueReader.integer(section.get("fade-in-ms"), 400);
        int stayMs = ConfigValueReader.integer(section.get("stay-ms"), 2200);
        int fadeOutMs = ConfigValueReader.integer(section.get("fade-out-ms"), 500);

        Map<String, Object> defaults = ConfigValueReader.map(section.get("defaults"));
        String defaultColor = ConfigValueReader.string(defaults.get("color"), "blue");
        String defaultOverlay = ConfigValueReader.string(defaults.get("overlay"), "progress");
        int animationSpeed = ConfigValueReader.integer(defaults.get("animation-speed"), 5);

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
}

