package com.netgrif.workflow.petrinet.domain.roles;


public enum RolePermission {
    PERFORM("perform"),
    DELEGATE("delegate"),
    CANCEL("cancel"),
    ASSIGNED("assigned");

    private String value;

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
    public boolean equal(String str){
        return str != null && this.value.equalsIgnoreCase(str);
    }
}
