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
     * Finds permission value by provided permissions and role ids
     *
     * @param roleIds role ids assigned to some actor
     * @param resourcePermissions permission collection of some resource
     * @param permission permission type to search for
     *
     * @return Optional boolean value, which is:
     * <ol>
     *     <li>Empty optional: if the permission value was not found by provided role ids</li>
     *     <li>Optional of true: if the positive permission was found by provided role ids (there must be no negative permission found)</li>
     *     <li>Optional of false: if the negative permission was found by provided role ids</li>
     * </ol>
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
            isPermittedOpt = resolveNextPermissionValue(isPermittedOpt.orElse(null), permissions.get(permission));
            if (isPermittedOpt.isPresent() && !isPermittedOpt.get()) {
                // permission is prohibited, no need of continuing
                return isPermittedOpt;
            }
        }

        return isPermittedOpt;
    }

    /**
     * Resolves next permission value between previous iteration result and current iteration result
     *
     * @param previousPermissionValue isPermitted value of previous iteration of for loop in {@link #isPermitted}
     * @param currentPermissionValue isPermitted value of current iteration of for loop in {@link #isPermitted}
     *
     * @return Optional isPermitted value. Scenarios:
     * <ol>
     *     <li>Empty optional: if the previous value was null (permission not found) and the current permission is also not found</li>
     *     <li>Optional of true: if the previous value was true or null, and the current value is true</li>
     *     <li>Optional of false: if the previous value was false, or the current value is false</li>
     * </ol>
     * */
    private Optional<Boolean> resolveNextPermissionValue(Boolean previousPermissionValue, Boolean currentPermissionValue) {
        if (previousPermissionValue == null) {
            return Optional.ofNullable(currentPermissionValue);
        }
        if (previousPermissionValue && currentPermissionValue != null) {
            return Optional.of(currentPermissionValue);
        }
        return Optional.of(previousPermissionValue);
    }
}
