package dev.onelimit.ymessages.model;

import java.util.ArrayList;
import java.util.List;

public final class PluginConfig {
    private final int configVersion;
    private final boolean debug;
    private final boolean commandEnabled;
    private final List<String> commandAliases;
    private final boolean commandRequirePermission;
    private final String commandPermission;
    private final AnnouncementTypeConfig chatConfig;
    private final AnnouncementTypeConfig actionbarConfig;
    private final AnnouncementTypeConfig titleConfig;
    private final AnnouncementTypeConfig bossbarConfig;

    public PluginConfig(
        int configVersion,
        boolean debug,
        boolean commandEnabled,
        List<String> commandAliases,
        boolean commandRequirePermission,
        String commandPermission,
        AnnouncementTypeConfig chatConfig,
        AnnouncementTypeConfig actionbarConfig,
        AnnouncementTypeConfig titleConfig,
        AnnouncementTypeConfig bossbarConfig
    ) {
        this.configVersion = configVersion;
        this.debug = debug;
        this.commandEnabled = commandEnabled;
        this.commandAliases = commandAliases;
        this.commandRequirePermission = commandRequirePermission;
        this.commandPermission = commandPermission;
        this.chatConfig = chatConfig;
        this.actionbarConfig = actionbarConfig;
        this.titleConfig = titleConfig;
        this.bossbarConfig = bossbarConfig;
    }

    public static PluginConfig defaults() {
        AnnouncementTypeConfig empty = new AnnouncementTypeConfig(false, 120, true, new ArrayList<>(), 400, 2200, 500, "blue", "progress", 5);
        return new PluginConfig(
            2, 
            false, 
            true, 
            List.of("announce", "vannounce"), 
            false, 
            "ymessages.admin",
            empty, empty, empty, empty
        );
    }

    public int configVersion() {
        return configVersion;
    }

    public boolean debug() {
        return debug;
    }

    public boolean commandEnabled() {
        return commandEnabled;
    }

    public List<String> commandAliases() {
        return commandAliases;
    }

    public boolean commandRequirePermission() {
        return commandRequirePermission;
    }

    public String commandPermission() {
        return commandPermission;
    }

    public AnnouncementTypeConfig chatConfig() {
        return chatConfig;
    }

    public AnnouncementTypeConfig actionbarConfig() {
        return actionbarConfig;
    }

    public AnnouncementTypeConfig titleConfig() {
        return titleConfig;
    }

    public AnnouncementTypeConfig bossbarConfig() {
        return bossbarConfig;
    }
}

