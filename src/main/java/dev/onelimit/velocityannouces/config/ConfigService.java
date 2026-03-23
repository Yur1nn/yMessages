package dev.onelimit.velocityannouces.config;

import dev.onelimit.velocityannouces.model.AnnounceMode;
import dev.onelimit.velocityannouces.model.AnnouncementEntry;
import dev.onelimit.velocityannouces.model.PluginConfig;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConfigService {
    private final Logger logger;
    private final Path dataDirectory;
    private final Path configPath;
    private final Yaml yaml;

    public ConfigService(Logger logger, Path dataDirectory) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.configPath = dataDirectory.resolve("config.yml");
        this.yaml = new Yaml();
    }

    public PluginConfig load() {
        ensureDefaultExists();

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            Object loaded = yaml.load(reader);
            if (!(loaded instanceof Map<?, ?> root)) {
                logger.warn("Config root is invalid; using defaults.");
                return PluginConfig.defaults();
            }
            return parse(root);
        } catch (Exception ex) {
            logger.error("Failed to load config.yml, using defaults.", ex);
            return PluginConfig.defaults();
        }
    }

    private void ensureDefaultExists() {
        try {
            Files.createDirectories(dataDirectory);
            if (Files.exists(configPath)) {
                return;
            }

            try (InputStream stream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (stream == null) {
                    throw new IOException("Missing bundled config.yml");
                }
                Files.copy(stream, configPath);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize config.yml", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private PluginConfig parse(Map<?, ?> root) {
        Map<String, Object> announcer = firstMap(root, "announcer", "auto-announcement");
        boolean autoEnabled = bool(announcer.get("enabled"), true);
        int intervalSeconds = integer(announcer.get("interval-seconds"), 120);
        boolean randomPick = parseSelection(announcer);

        Map<String, Object> delivery = map(root.get("delivery"));
        Map<String, Object> titleDefaults = map(delivery.get("title"));
        Map<String, Object> bossbarDefaults = map(delivery.get("bossbar"));

        int defaultTitleFadeIn = integer(titleDefaults.get("fade-in-ms"), 400);
        int defaultTitleStay = integer(titleDefaults.get("stay-ms"), 2200);
        int defaultTitleFadeOut = integer(titleDefaults.get("fade-out-ms"), 500);

        float defaultBossbarProgress = decimal(bossbarDefaults.get("progress"), 1.0f);
        String defaultBossbarColor = string(bossbarDefaults.get("color"), "blue");
        String defaultBossbarOverlay = string(bossbarDefaults.get("overlay"), "progress");
        int defaultBossbarDuration = integer(bossbarDefaults.get("duration-seconds"), 5);

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

        List<AnnouncementEntry> entries = new ArrayList<>();
        Object announcementsNode = null;
        Map<String, Object> messages = map(root.get("messages"));
        if (!messages.isEmpty()) {
            announcementsNode = messages.get("auto");
        }
        if (!(announcementsNode instanceof List<?>)) {
            announcementsNode = root.get("announcements");
        }

        if (announcementsNode instanceof List<?> list) {
            for (Object node : list) {
                Map<String, Object> row = map(node);
                String rawMode = string(row.get("mode"), string(row.get("type"), "chat"));
                AnnounceMode mode = AnnounceMode.fromString(rawMode);
                String message = string(row.get("message"), "");
                String title = string(row.get("title"), "");
                String subtitle = string(row.get("subtitle"), "");

                int fadeInMs = integer(row.get("fade-in-ms"), defaultTitleFadeIn);
                int stayMs = integer(row.get("stay-ms"), defaultTitleStay);
                int fadeOutMs = integer(row.get("fade-out-ms"), defaultTitleFadeOut);

                float progress = decimal(row.get("progress"), defaultBossbarProgress);
                String color = string(row.get("color"), defaultBossbarColor);
                String overlay = string(row.get("overlay"), defaultBossbarOverlay);
                int duration = integer(row.get("duration-seconds"), defaultBossbarDuration);

                entries.add(new AnnouncementEntry(mode, message, title, subtitle, fadeInMs, stayMs, fadeOutMs, progress, color, overlay, duration));
            }
        }

        return new PluginConfig(
            autoEnabled,
            Math.max(5, intervalSeconds),
            randomPick,
            commandEnabled,
            aliases,
            commandRequirePermission,
            commandPermission,
            entries
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
