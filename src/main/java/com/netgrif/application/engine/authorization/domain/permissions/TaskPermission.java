package com.netgrif.application.engine.authorization.domain.permissions;


import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPermission {
    CANCEL,
    ASSIGN,
    FINISH,
    VIEW,
    REASSIGN,
    /**
     * A permission to view task, that is disabled
     * */
    VIEW_DISABLED;

    @JsonValue
    public String value() {
        return super.name().toLowerCase();
    }
}
