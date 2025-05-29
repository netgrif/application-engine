package com.netgrif.application.engine.authorization.service.factory;

import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;

public abstract class RoleAssignmentFactory {

    /**
     * todo javadoc
     * */
    protected abstract RoleAssignment doCreateAssignment(Role role);

    /**
     * todo javadoc
     * */
    public RoleAssignment createAssignment(Role role, String userId) {
        RoleAssignment assignment = doCreateAssignment(role);
        assignment.setRoleId(role.getStringId());
        assignment.setRoleImportId(role.getImportId());
        assignment.setActorId(userId);
        return assignment;
    }

}
