package com.netgrif.application.engine.petrinet.domain.roles;

public enum AssignedUserPermission {
    CANCEL("CANCEL"),
    REASSIGN("REASSIGN");

    private final String value;

    AssignedUserPermission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
