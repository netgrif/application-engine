package com.netgrif.application.engine.workflow.service.sanitization;

public enum SanitizationMode {

    OFF,
    PLAIN_TEXT,
    SAFE_HTML,
    SAFE_HTML_BASIC,
    SAFE_HTML_LINKS_ONLY,
    SAFE_HTML_NO_LINKS,
    DISABLE_JAVASCRIPT,
    SAFE_HTML_RELAXED;

    public static SanitizationMode from(String value) {
        if (value == null || value.isBlank()) {
            return OFF;
        }

        for (SanitizationMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }

        return OFF;
    }
}