package com.netgrif.application.engine.authorization.service.factory;

import com.netgrif.application.engine.authorization.domain.CaseRole;
import com.netgrif.application.engine.authorization.domain.CaseRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import org.springframework.stereotype.Service;

@Service
public class CaseRoleAssignmentFactory extends RoleAssignmentFactory {

    @Override
    protected RoleAssignment createAssignmentInternal(Role role) {
        CaseRole caseRole = (CaseRole) role;
        CaseRoleAssignment assignment = new CaseRoleAssignment();
        assignment.setCaseId(caseRole.getCaseId());
        return assignment;
    }
}
