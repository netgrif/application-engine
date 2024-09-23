package com.netgrif.application.engine.petrinet.domain.roles;


import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPermission {
    CANCEL,
    ASSIGN,
    FINISH,
    VIEW,
    REASSIGN,
    VIEW_DISABLED;

    @JsonValue
    public String value() {
        return super.name().toLowerCase();
    }
}
