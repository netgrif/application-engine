package com.netgrif.application.engine.authorization.service.factory;

import com.netgrif.application.engine.authorization.domain.ProcessRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import com.netgrif.application.engine.authorization.domain.Session;
import com.netgrif.application.engine.configuration.RBACProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessRoleAssignmentFactory extends RoleAssignmentFactory {

    private final RBACProperties properties;

    @Override
    protected RoleAssignment doCreateAssignment(Role role) {
        if (properties.getDefaultAssignmentSessionDuration() != null) {
            return new ProcessRoleAssignment(Session.withDuration(properties.getDefaultAssignmentSessionDuration()));
        }
        return new ProcessRoleAssignment();
    }
}
