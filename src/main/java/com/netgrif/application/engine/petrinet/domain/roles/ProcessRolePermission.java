package com.netgrif.application.engine.petrinet.domain.roles;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProcessRolePermission {
    CREATE,
    DELETE,
    VIEW;

    @JsonValue
    public String value() {
        return super.name().toLowerCase();
    }

    public static ProcessRolePermission fromValue(String v) {
        return valueOf(v);
    }
}
