package com.fmworkflow.petrinet.domain.roles;

public abstract class RoleFunction implements IRoleFunction {
    private String roleId;

    public RoleFunction(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}