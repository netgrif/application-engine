package com.netgrif.application.engine.authorization.service.factory;

import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.domain.ApplicationRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import org.springframework.stereotype.Service;

@Service
public class ApplicationRoleAssignmentFactory extends RoleAssignmentFactory {

    @Override
    protected RoleAssignment doCreateAssignment(Role role) {
        ApplicationRole appRole = (ApplicationRole) role;
        ApplicationRoleAssignment assignment = new ApplicationRoleAssignment();
        assignment.setApplicationId(appRole.getApplicationId());
        return assignment;
    }
}
