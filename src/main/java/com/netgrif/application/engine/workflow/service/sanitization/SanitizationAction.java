package com.netgrif.application.engine.workflow.service.sanitization;

public enum SanitizationAction {
    SANITIZE,
    REJECT;

    static SanitizationAction from(String value) {
        if (value == null || value.isBlank()) {
            return SANITIZE;
        }

        for (SanitizationAction action : values()) {
            if (action.name().equalsIgnoreCase(value)) {
                return action;
            }
        }

        return SANITIZE;
    }
}