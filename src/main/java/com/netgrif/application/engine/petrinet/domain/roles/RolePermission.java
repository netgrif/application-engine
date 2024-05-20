package com.netgrif.application.engine.petrinet.domain.roles;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RolePermission {
    DELEGATE,
    CANCEL,
    ASSIGN,
    FINISH,
    SET,
    VIEW;

    @JsonValue
    public String value() {
        return super.name().toLowerCase();
    }
}
