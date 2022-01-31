package com.netgrif.application.engine.petrinet.domain.roles;

public enum ProcessRolePermission {
    CREATE("create"),
    DELETE("delete"),
    VIEW("view");

    private String value;

    ProcessRolePermission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public boolean equal(String str) {
        return str != null && this.value.equalsIgnoreCase(str);
    }
}
