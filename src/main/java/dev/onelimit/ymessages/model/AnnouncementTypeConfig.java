package dev.onelimit.ymessages.model;

import java.util.List;

public final class AnnouncementTypeConfig {
    private final boolean enabled;
    private final int intervalSeconds;
    private final boolean randomSelection;
    private final List<String> messages;
    private final int fadeInMs;
    private final int stayMs;
    private final int fadeOutMs;
    private final String defaultColor;
    private final String defaultOverlay;
    private final int animationSpeed;

    public AnnouncementTypeConfig(
        boolean enabled,
        int intervalSeconds,
        boolean randomSelection,
        List<String> messages,
        int fadeInMs,
        int stayMs,
        int fadeOutMs,
        String defaultColor,
        String defaultOverlay,
        int animationSpeed
    ) {
        this.enabled = enabled;
        this.intervalSeconds = intervalSeconds;
        this.randomSelection = randomSelection;
        this.messages = messages;
        this.fadeInMs = fadeInMs;
        this.stayMs = stayMs;
        this.fadeOutMs = fadeOutMs;
        this.defaultColor = defaultColor;
        this.defaultOverlay = defaultOverlay;
        this.animationSpeed = animationSpeed;
    }

    public boolean enabled() {
        return enabled;
    }

    public int intervalSeconds() {
        return intervalSeconds;
    }

    public boolean randomSelection() {
        return randomSelection;
    }

    public List<String> messages() {
        return messages;
    }

    public int fadeInMs() {
        return fadeInMs;
    }

    public int stayMs() {
        return stayMs;
    }

    public int fadeOutMs() {
        return fadeOutMs;
    }

    public String defaultColor() {
        return defaultColor;
    }

    public String defaultOverlay() {
        return defaultOverlay;
    }

    public int animationSpeed() {
        return animationSpeed;
    }
}

