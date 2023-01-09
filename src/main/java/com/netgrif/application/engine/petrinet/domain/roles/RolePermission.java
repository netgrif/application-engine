package com.netgrif.application.engine.petrinet.domain.roles;


public enum RolePermission {
    DELEGATE("DELEGATE"),
    CANCEL("CANCEL"),
    ASSIGN("ASSIGN"),
    FINISH("FINISH"),
    SET("SET"),
    VIEW("VIEW");

    private final String value;

    RolePermission(String value) {
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
        return this.value.equalsIgnoreCase(str);
    }
}
