package dev.onelimit.velocityannouces.model;

import java.util.ArrayList;
import java.util.List;

public final class PluginConfig {
    private final boolean autoEnabled;
    private final int intervalSeconds;
    private final boolean randomPick;
    private final boolean commandEnabled;
    private final List<String> commandAliases;
    private final boolean commandRequirePermission;
    private final String commandPermission;
    private final List<AnnouncementEntry> announcements;

    public PluginConfig(
        boolean autoEnabled,
        int intervalSeconds,
        boolean randomPick,
        boolean commandEnabled,
        List<String> commandAliases,
        boolean commandRequirePermission,
        String commandPermission,
        List<AnnouncementEntry> announcements
    ) {
        this.autoEnabled = autoEnabled;
        this.intervalSeconds = intervalSeconds;
        this.randomPick = randomPick;
        this.commandEnabled = commandEnabled;
        this.commandAliases = commandAliases;
        this.commandRequirePermission = commandRequirePermission;
        this.commandPermission = commandPermission;
        this.announcements = announcements;
    }

    public static PluginConfig defaults() {
        return new PluginConfig(true, 120, true, true, List.of("announce", "vannounce"), false, "velocityannouces.admin", new ArrayList<>());
    }

    public boolean autoEnabled() {
        return autoEnabled;
    }

    public int intervalSeconds() {
        return intervalSeconds;
    }

    public boolean randomPick() {
        return randomPick;
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

    public List<AnnouncementEntry> announcements() {
        return announcements;
    }
}
