package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public abstract class AuthorizationService {

    protected final ISessionManagerService sessionManagerService;
    protected final ApplicationRoleRunner applicationRoleRunner;
    protected final IRoleAssignmentService roleAssignmentService;

    /**
     * todo javadoc
     * popisat vzorec (tak ako v elastic view permission service)
     * */
    protected <T> boolean canCallEvent(AccessPermissions<T> processRolePermissions,
                                       AccessPermissions<T> caseRolePermissions, T permission) {
        LoggedIdentity loggedIdentity = sessionManagerService.getLoggedIdentity();
        if (loggedIdentity == null || loggedIdentity.getActiveActorId() == null) {
            return false;
        }

        Set<String> roleIds = roleAssignmentService.findAllRoleIdsByActorAndGroups(loggedIdentity.getActiveActorId());

        if (isAdmin(roleIds)) {
            return true;
        }

        Optional<Boolean> permittedByCaseRoleOpt = isPermitted(roleIds, caseRolePermissions, permission);
        if (permittedByCaseRoleOpt.isPresent()) {
            return permittedByCaseRoleOpt.get();
        }

        Optional<Boolean> permittedByProcessRoleOpt = isPermitted(roleIds, processRolePermissions, permission);

        return permittedByProcessRoleOpt.orElse(false);
    }

    protected boolean isAdmin() {
        LoggedIdentity loggedIdentity = sessionManagerService.getLoggedIdentity();
        if (loggedIdentity == null || loggedIdentity.getActiveActorId() == null) {
            return false;
        }
        return isAdmin(roleAssignmentService.findAllRoleIdsByActorAndGroups(loggedIdentity.getActiveActorId()));
    }

    private boolean isAdmin(Set<String> roleIds) {
        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        return roleIds.contains(adminAppRole.getStringId());
    }

    /**
     * todo javadoc
     * */
    private <T> Optional<Boolean> isPermitted(Set<String> roleIds, AccessPermissions<T> resourcePermissions, T permission) {
        // private on purpose
        Optional<Boolean> isPermittedOpt = Optional.empty();

        if (resourcePermissions.isEmpty()) {
            return isPermittedOpt;
        }

        for (Map.Entry<String, Map<T, Boolean>> entry : resourcePermissions.getPermissions().entrySet()) {
            if (!roleIds.contains(entry.getKey())) {
                continue;
            }
            Map<T, Boolean> permissions = entry.getValue();
            isPermittedOpt = Optional.ofNullable(permissions.get(permission));
            if (isPermittedOpt.isPresent() && !isPermittedOpt.get()) {
                // permission is prohibited, no need of continuing
                return isPermittedOpt;
            }
        }

        return isPermittedOpt;
    }
}
