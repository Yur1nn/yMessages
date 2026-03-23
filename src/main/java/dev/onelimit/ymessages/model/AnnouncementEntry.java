package dev.onelimit.ymessages.model;

public final class AnnouncementEntry {
    private final AnnounceMode mode;
    private final String message;
    private final String title;
    private final String subtitle;
    private final int fadeInMs;
    private final int stayMs;
    private final int fadeOutMs;
    private final float progress;
    private final String color;
    private final String overlay;
    private final int durationSeconds;
    private final String sound;
    private final int animationSpeed;

    public AnnouncementEntry(
        AnnounceMode mode,
        String message,
        String title,
        String subtitle,
        int fadeInMs,
        int stayMs,
        int fadeOutMs,
        float progress,
        String color,
        String overlay,
        int durationSeconds,
        String sound,
        int animationSpeed
    ) {
        this.mode = mode;
        this.message = message;
        this.title = title;
        this.subtitle = subtitle;
        this.fadeInMs = fadeInMs;
        this.stayMs = stayMs;
        this.fadeOutMs = fadeOutMs;
        this.progress = progress;
        this.color = color;
        this.overlay = overlay;
        this.durationSeconds = durationSeconds;
        this.sound = sound;
        this.animationSpeed = animationSpeed;
    }

    public AnnounceMode mode() {
        return mode;
    }

    public String message() {
        return message;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
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

    public float progress() {
        return progress;
    }

    public String color() {
        return color;
    }

    public String overlay() {
        return overlay;
    }

    public int durationSeconds() {
        return durationSeconds;
    }

    public String sound() {
        return sound;
    }

    public int animationSpeed() {
        return animationSpeed;
    }
}

