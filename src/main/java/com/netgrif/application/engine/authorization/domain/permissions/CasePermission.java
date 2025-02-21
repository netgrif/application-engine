package com.netgrif.application.engine.authorization.domain.permissions;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CasePermission {
    CREATE,
    DELETE,
    VIEW;

    @JsonValue
    public String value() {
        return super.name().toLowerCase();
    }
}
