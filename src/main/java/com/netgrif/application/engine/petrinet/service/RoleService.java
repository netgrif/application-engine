package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.event.events.user.UserRoleChangeEvent;
import com.netgrif.application.engine.importer.model.EventPhaseType;
import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import com.netgrif.application.engine.petrinet.domain.roles.Role;
import com.netgrif.application.engine.petrinet.domain.roles.RoleRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IRoleService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class RoleService implements IRoleService {

    private final IUserService userService;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher publisher;
    private final ISecurityContextService securityContextService;
    private final ActionRunner actionsRunner;

    private Role defaultRole;
    private Role anonymousRole;

    public RoleService(
            RoleRepository roleRepository,
            ApplicationEventPublisher publisher,
            @Lazy IUserService userService,
            ISecurityContextService securityContextService,
            ActionRunner actionsRunner
    ) {
        this.roleRepository = roleRepository;
        this.publisher = publisher;
        this.userService = userService;
        this.securityContextService = securityContextService;
        this.actionsRunner = actionsRunner;
    }

    @Override
    public List<Role> saveAll(Iterable<Role> entities) {
        return roleRepository.saveAll(entities);
    }

    @Override
    public Set<Role> findByIds(Set<String> ids) {
        return StreamSupport.stream(roleRepository.findAllById(ids).spliterator(), false).collect(Collectors.toSet());
    }

    @Override
    public void assignRolesToUser(String userId, Set<String> requestedRolesIds, LoggedUser loggedUser) {
        assignRolesToUser(userId, requestedRolesIds, loggedUser, new HashMap<>());
    }

    @Override
    public void assignRolesToUser(String userId, Set<String> requestedRolesIds, LoggedUser loggedUser, Map<String, String> params) {
        IUser user = userService.resolveById(userId);
        Set<Role> requestedRoles = this.findByIds(requestedRolesIds);
        if (requestedRoles.isEmpty() && !requestedRolesIds.isEmpty()) {
            throw new IllegalArgumentException("No process roles found.");
        }
        if (requestedRoles.size() != requestedRolesIds.size()) {
            throw new IllegalArgumentException("Not all process roles were found!");
        }

        Set<Role> userOldRoles = user.getRoles();

        Set<Role> rolesNewToUser = getRolesNewToUser(userOldRoles, requestedRoles);
        Set<Role> rolesRemovedFromUser = getRolesRemovedFromUser(userOldRoles, requestedRoles);

        Set<String> rolesNewToUserIds = mapUserRolesToIds(rolesNewToUser);
        Set<String> rolesRemovedFromUserIds = mapUserRolesToIds(rolesRemovedFromUser);

        Set<Role> newRoles = this.findByIds(rolesNewToUserIds);
        Set<Role> removedRoles = this.findByIds(rolesRemovedFromUserIds);

        runAllPreActions(newRoles, removedRoles, user, params);
        user = userService.findById(userId);
        requestedRoles = updateRequestedRoles(user, rolesNewToUser, rolesRemovedFromUser);

        replaceUserRolesAndPublishEvent(requestedRolesIds, user, requestedRoles);
        runAllPostActions(newRoles, removedRoles, user, params);

        securityContextService.saveToken(userId);
        if (Objects.equals(userId, loggedUser.getId())) {
            loggedUser.getRoles().clear();
            loggedUser.parseRoles(user.getRoles());
            securityContextService.reloadSecurityContext(loggedUser);
        }
    }

    private Set<Role> updateRequestedRoles(IUser user, Set<Role> rolesNewToUser, Set<Role> rolesRemovedFromUser) {
        Set<Role> userRolesAfterPreActions = user.getRoles();
        userRolesAfterPreActions.addAll(rolesNewToUser);
        userRolesAfterPreActions.removeAll(rolesRemovedFromUser);

        return new HashSet<>(userRolesAfterPreActions);
    }

    private void replaceUserRolesAndPublishEvent(Set<String> requestedRolesIds, IUser user, Set<Role> requestedRoles) {
        removeOldAndAssignNewRolesToUser(user, requestedRoles);
        publisher.publishEvent(new UserRoleChangeEvent(user, this.findByIds(requestedRolesIds)));
    }

    private Set<Role> getRolesNewToUser(Set<Role> userOldRoles, Set<Role> newRequestedRoles) {
        Set<Role> rolesNewToUser = new HashSet<>(newRequestedRoles);
        rolesNewToUser.removeAll(userOldRoles);

        return rolesNewToUser;
    }

    private Set<Role> getRolesRemovedFromUser(Set<Role> userOldRoles, Set<Role> newRequestedRoles) {
        Set<Role> rolesRemovedFromUser = new HashSet<>(userOldRoles);
        rolesRemovedFromUser.removeAll(newRequestedRoles);

        return rolesRemovedFromUser;
    }

    private Set<String> mapUserRolesToIds(Collection<Role> roles) {
        return roles.stream()
                .map(Role::getStringId)
                .collect(Collectors.toSet());
    }

    private void runAllPreActions(Set<Role> newRoles, Set<Role> removedRoles, IUser user, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.PRE, user, params);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.PRE, user, params);
    }

    private void runAllPostActions(Set<Role> newRoles, Set<Role> removedRoles, IUser user, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.POST, user, params);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.POST, user, params);
    }

    private void runAllSuitableActionsOnRoles(Set<Role> roles, EventType requiredEventType, EventPhaseType requiredPhase, IUser user, Map<String, String> params) {
        roles.forEach(role -> {
            runAllSuitableActionsOnOneRole(role.getEvents(), requiredEventType, requiredPhase, params);
        });
    }

    // TODO: release/8.0.0 fix
    private void runAllSuitableActionsOnOneRole(Map<EventType, Event> eventMap, EventType requiredEventType, EventPhaseType requiredPhase, Map<String, String> params) {
        if (eventMap == null) {
            return;
        }
        eventMap.forEach((eventType, event) -> {
            if (eventType != requiredEventType) {
                return;
            }

            runActionsBasedOnPhase(event, requiredPhase, params);
        });
    }

    private void runActionsBasedOnPhase(Event event, EventPhaseType requiredPhase, Map<String, String> params) {
        switch (requiredPhase) {
            case PRE:
                runActions(event.getPreActions(), params);
                break;
            case POST:
                runActions(event.getPostActions(), params);
                break;
        }
    }

    private void runActions(List<Action> actions, Map<String, String> params) {
        actions.forEach(action -> actionsRunner.run(action, null, params));
    }

    private void removeOldAndAssignNewRolesToUser(IUser user, Set<Role> requestedRoles) {
        user.getRoles().clear();
        user.getRoles().addAll(requestedRoles);

        userService.save(user);
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public List<Role> findAll(String netId) {
        // TODO: release/8.0.0 fix
        return null;
    }

    @Override
    public Role defaultRole() {
        // TODO: release/8.0.0 duplicate code
        if (defaultRole == null) {
            Set<Role> roles = roleRepository.findAllByName_DefaultValue(Role.DEFAULT_ROLE);
            if (roles.isEmpty()) {
                throw new IllegalStateException("No default process role has been found!");
            }
            if (roles.size() > 1) {
                throw new IllegalStateException("More than 1 default process role exists!");
            }
            defaultRole = roles.stream().findFirst().orElse(null);
        }
        return defaultRole;
    }

    @Override
    public Role anonymousRole() {
        if (anonymousRole == null) {
            Set<Role> roles = roleRepository.findAllByImportId(Role.ANONYMOUS_ROLE);
            if (roles.isEmpty()) {
                throw new IllegalStateException("No anonymous process role has been found!");
            }
            if (roles.size() > 1) {
                throw new IllegalStateException("More than 1 anonymous process role exists!");
            }
            anonymousRole = roles.stream().findFirst().orElse(null);
        }
        return anonymousRole;
    }

    /**
     * @param importId id from a process of a role
     * @return a process role object
     * @deprecated use {@link RoleService#findAllByImportId(String)} instead
     */
    @Deprecated(forRemoval = true, since = "6.2.0")
    @Override
    public Role findByImportId(String importId) {
        return roleRepository.findAllByImportId(importId).stream().findFirst().orElse(null);
    }

    @Override
    public Set<Role> findAllByImportId(String importId) {
        return roleRepository.findAllByImportId(importId);
    }

    @Override
    public Set<Role> findAllByDefaultName(String name) {
        return roleRepository.findAllByName_DefaultValue(name);
    }

    @Override
    public Role findById(String id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteRolesOfNet(Process net, LoggedUser loggedUser) {
        log.info("[{}]: Initiating deletion of all roles of Petri net {} version {}", net.getStringId(), net.getIdentifier(), net.getVersion().toString());
        // TODO: release/8.0.0 fix
//        List<ObjectId> deletedRoleIds = this.findAll(net).stream().map(ProcessRole::getId).collect(Collectors.toList());
        List<ObjectId> deletedRoleIds = null;
        Set<String> deletedRoleStringIds = deletedRoleIds.stream().map(ObjectId::toString).collect(Collectors.toSet());

        List<IUser> usersWithRemovedRoles = this.userService.findAllByRoles(deletedRoleStringIds);
        for (IUser user : usersWithRemovedRoles) {
            log.info("[{}]: Removing deleted roles of Petri net {} version {} from user {} with id {}", net.getStringId(), net.getIdentifier(), net.getVersion().toString(), user.getFullName(), user.getStringId());
            if (user.getRoles().isEmpty()) {
                continue;
            }

            Set<String> newRoles = user.getRoles().stream()
                    .filter(role -> !deletedRoleStringIds.contains(role.getStringId()))
                    .map(Role::getStringId)
                    .collect(Collectors.toSet());
            this.assignRolesToUser(user.getStringId(), newRoles, loggedUser);
        }

        log.info("[{}]: Deleting all roles of Petri net {} version {}", net.getStringId(), net.getIdentifier(), net.getVersion().toString());
        this.roleRepository.deleteAllByIdIn(deletedRoleIds);
    }

    // TODO: release/8.0.0 cache
    @Override
    public boolean existsByImportId(String importId) {
        return roleRepository.existsByImportId(importId);
    }

    public void clearCache() {
        this.defaultRole = null;
        this.anonymousRole = null;
    }
}
