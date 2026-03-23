package dev.onelimit.ymessages.model;

public enum AnnounceMode {
    CHAT,
    ACTIONBAR,
    TITLE,
    BOSSBAR;

    public static AnnounceMode fromString(String value) {
        if (value == null) {
            return CHAT;
        }

        try {
            return AnnounceMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CHAT;
        }
    }
}

