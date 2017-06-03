package com.netgrif.workflow.petrinet.domain.roles;

/**
 * RoleFunction stores roleId of process role to which its logic is binded. When using unsafeApply() method you have to
 * provide roleIds of current user.
 * <pre>
 *     {
 *         "roleIds": ["id1", "id2", ..., "idn"]
 *     }
 * </pre>
 * Result from calling unsafeApply() is JSON which will contain information if RoleFunction actions are allowed for
 * given User.
 * <pre>
 *     {
 *         "roleIds": ["id1", "id2", ..., "idn"],
 *         "assing": true,
 *         "delegate": false
 *     }
 * </pre>
 */
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