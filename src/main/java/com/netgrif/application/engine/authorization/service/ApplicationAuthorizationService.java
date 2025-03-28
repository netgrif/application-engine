package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.service.interfaces.IApplicationAuthorizationService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationAuthorizationService implements IApplicationAuthorizationService {

    private final ApplicationRoleRunner applicationRoleRunner;
    private final IIdentityService identityService;
    private final IRoleAssignmentService roleAssignmentService;

    /**
     * todo javadoc
     * */
    @Override
    public boolean hasApplicationRole(String roleName) {
        if (roleName == null) {
            return false;
        }

        LoggedIdentity loggedIdentity = identityService.getLoggedIdentity();
        if (loggedIdentity == null || loggedIdentity.getActiveActorId() == null) {
            return false;
        }

        ApplicationRole appRole = applicationRoleRunner.getAppRole(roleName);
        if (appRole == null) {
            return false;
        }

        return roleAssignmentService.existsByActorAndRole(loggedIdentity.getActiveActorId(), appRole.getStringId());
    }
}
