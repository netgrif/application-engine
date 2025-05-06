package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.service.interfaces.IAllActorService;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public abstract class AuthorizationService {

    protected final ISessionManagerService sessionManagerService;
    private final IRoleAssignmentService roleAssignmentService;
    private final ApplicationRoleRunner applicationRoleRunner;
    private final IAllActorService allActorService;
    private final IGroupService groupService;

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

        Set<String> roleIds = findRoleIdsByActorAndGroups(loggedIdentity.getActiveActorId());

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
        return isAdmin(roleAssignmentService.findAllRoleIdsByActorId(loggedIdentity.getActiveActorId()));
    }

    private boolean isAdmin(Set<String> roleIds) {
        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        return roleIds.contains(adminAppRole.getStringId());
    }

    private Set<String> findRoleIdsByActorAndGroups(String actorId) {
        Optional<Actor> actorOpt = allActorService.findById(actorId);
        if (actorOpt.isEmpty()) {
            throw new IllegalStateException(String.format("Actor with id [%s] doesn't exist.", actorId));
        }

        Set<String> roleIds = roleAssignmentService.findAllRoleIdsByActorId(actorId);
        roleIds.addAll(findRoleIdsByGroups(actorOpt.get().getGroupIds()));

        return roleIds;
    }

    private Set<String> findRoleIdsByGroups(List<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> alreadyProcessedGroupIds = new HashSet<>();
        Set<String> roleIds = new HashSet<>();
        for (String groupId : groupIds) {
            roleIds.addAll(findRoleIdsByGroupRecursive(groupId, alreadyProcessedGroupIds));
            alreadyProcessedGroupIds.add(groupId);
        }
        return roleIds;
    }

    private Set<String> findRoleIdsByGroupRecursive(String groupId, Set<String> alreadyProcessedGroupIds) {
        if (alreadyProcessedGroupIds.contains(groupId)) {
            return new HashSet<>();
        }
        Optional<Group> groupOpt = groupService.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new IllegalStateException(String.format("Group with id [%s] doesn't exist.", groupId));
        }

        Set<String> roleIds = roleAssignmentService.findAllRoleIdsByActorId(groupId);
        if (groupOpt.get().getParentGroupId() != null) {
            roleIds.addAll(findRoleIdsByGroupRecursive(groupOpt.get().getParentGroupId(), alreadyProcessedGroupIds));
        }
        alreadyProcessedGroupIds.add(groupId);

        return roleIds;
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
