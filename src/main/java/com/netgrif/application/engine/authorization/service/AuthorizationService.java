package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public abstract class AuthorizationService {

    protected final IIdentityService identityService;
    private final IRoleAssignmentService roleAssignmentService;
    private final ApplicationRoleRunner applicationRoleRunner;

    /**
     * todo javadoc
     * popisat vzorec (tak ako v elastic view permission service)
     * */
    protected <T> boolean canCallEvent(AccessPermissions<T> processRolePermissions,
                                       AccessPermissions<T> caseRolePermissions, T permission) {
        LoggedIdentity loggedIdentity = identityService.getLoggedIdentity();
        if (loggedIdentity == null || loggedIdentity.getActiveActorId() == null) {
            return false;
        }

        Set<String> roleIds = roleAssignmentService.findAllRoleIdsByActorId(loggedIdentity.getActiveActorId());
        if (roleIds.isEmpty() || permission == null) {
            return false;
        }

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        if (roleIds.contains(adminAppRole.getStringId())) {
            return true;
        }

        Optional<Boolean> permittedByCaseRoleOpt = isPermitted(roleIds, caseRolePermissions, permission);
        if (permittedByCaseRoleOpt.isPresent()) {
            return permittedByCaseRoleOpt.get();
        }

        Optional<Boolean> permittedByProcessRoleOpt = isPermitted(roleIds, processRolePermissions, permission);

        return permittedByProcessRoleOpt.orElse(false);
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

        for (Map.Entry<String, Map<T, Boolean>> entry : resourcePermissions.entrySet()) {
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
