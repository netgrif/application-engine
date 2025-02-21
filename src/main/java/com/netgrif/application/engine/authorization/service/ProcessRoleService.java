package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.event.events.user.UserRoleChangeEvent;
import com.netgrif.application.engine.importer.model.EventPhaseType;
import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import com.netgrif.application.engine.authorization.domain.repositories.ProcessRoleRepository;
import com.netgrif.application.engine.authorization.service.interfaces.IProcessRoleService;
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
public class ProcessRoleService implements IProcessRoleService {

    private final IUserService userService;
    private final ProcessRoleRepository processRoleRepository;
    private final ApplicationEventPublisher publisher;
    private final ISecurityContextService securityContextService;
    private final ActionRunner actionsRunner;

    private ProcessRole defaultProcessRole;
    private ProcessRole anonymousProcessRole;

    public ProcessRoleService(
            ProcessRoleRepository processRoleRepository,
            ApplicationEventPublisher publisher,
            @Lazy IUserService userService,
            ISecurityContextService securityContextService,
            ActionRunner actionsRunner
    ) {
        this.processRoleRepository = processRoleRepository;
        this.publisher = publisher;
        this.userService = userService;
        this.securityContextService = securityContextService;
        this.actionsRunner = actionsRunner;
    }

    @Override
    public List<ProcessRole> saveAll(Iterable<ProcessRole> entities) {
        return processRoleRepository.saveAll(entities);
    }

    @Override
    public Set<ProcessRole> findByIds(Set<String> ids) {
        return StreamSupport.stream(processRoleRepository.findAllById(ids).spliterator(), false).collect(Collectors.toSet());
    }

    @Override
    public void assignRolesToUser(String userId, Set<String> requestedRolesIds, LoggedUser loggedUser) {
        assignRolesToUser(userId, requestedRolesIds, loggedUser, new HashMap<>());
    }

    @Override
    public void assignRolesToUser(String userId, Set<String> requestedRolesIds, LoggedUser loggedUser, Map<String, String> params) {
        IUser user = userService.resolveById(userId);
        Set<ProcessRole> requestedProcessRoles = this.findByIds(requestedRolesIds);
        if (requestedProcessRoles.isEmpty() && !requestedRolesIds.isEmpty()) {
            throw new IllegalArgumentException("No process roles found.");
        }
        if (requestedProcessRoles.size() != requestedRolesIds.size()) {
            throw new IllegalArgumentException("Not all process roles were found!");
        }

        // todo 2058
//        Set<Role> userOldRoles = user.getRoles();
        Set<ProcessRole> userOldProcessRoles = new HashSet<>();

        Set<ProcessRole> rolesNewToUser = getRolesNewToUser(userOldProcessRoles, requestedProcessRoles);
        Set<ProcessRole> rolesRemovedFromUser = getRolesRemovedFromUser(userOldProcessRoles, requestedProcessRoles);

        Set<String> rolesNewToUserIds = mapUserRolesToIds(rolesNewToUser);
        Set<String> rolesRemovedFromUserIds = mapUserRolesToIds(rolesRemovedFromUser);

        Set<ProcessRole> newProcessRoles = this.findByIds(rolesNewToUserIds);
        Set<ProcessRole> removedProcessRoles = this.findByIds(rolesRemovedFromUserIds);

        runAllPreActions(newProcessRoles, removedProcessRoles, user, params);
        user = userService.findById(userId);
        requestedProcessRoles = updateRequestedRoles(user, rolesNewToUser, rolesRemovedFromUser);

        replaceUserRolesAndPublishEvent(requestedRolesIds, user, requestedProcessRoles);
        runAllPostActions(newProcessRoles, removedProcessRoles, user, params);

        securityContextService.saveToken(userId);
        if (Objects.equals(userId, loggedUser.getId())) {
            loggedUser.getRoles().clear();
            // todo 2058
//            loggedUser.parseRoles(user.getRoles());
            securityContextService.reloadSecurityContext(loggedUser);
        }
    }

    private Set<ProcessRole> updateRequestedRoles(IUser user, Set<ProcessRole> rolesNewToUser, Set<ProcessRole> rolesRemovedFromUser) {
        // todo 2058
//        Set<Role> userRolesAfterPreActions = user.getRoles();
        Set<ProcessRole> userRolesAfterPreActions = new HashSet<>();
        userRolesAfterPreActions.addAll(rolesNewToUser);
        userRolesAfterPreActions.removeAll(rolesRemovedFromUser);

        return new HashSet<>(userRolesAfterPreActions);
    }

    private void replaceUserRolesAndPublishEvent(Set<String> requestedRolesIds, IUser user, Set<ProcessRole> requestedProcessRoles) {
        removeOldAndAssignNewRolesToUser(user, requestedProcessRoles);
        publisher.publishEvent(new UserRoleChangeEvent(user, this.findByIds(requestedRolesIds)));
    }

    private Set<ProcessRole> getRolesNewToUser(Set<ProcessRole> userOldProcessRoles, Set<ProcessRole> newRequestedProcessRoles) {
        Set<ProcessRole> rolesNewToUser = new HashSet<>(newRequestedProcessRoles);
        rolesNewToUser.removeAll(userOldProcessRoles);

        return rolesNewToUser;
    }

    private Set<ProcessRole> getRolesRemovedFromUser(Set<ProcessRole> userOldProcessRoles, Set<ProcessRole> newRequestedProcessRoles) {
        Set<ProcessRole> rolesRemovedFromUser = new HashSet<>(userOldProcessRoles);
        rolesRemovedFromUser.removeAll(newRequestedProcessRoles);

        return rolesRemovedFromUser;
    }

    private Set<String> mapUserRolesToIds(Collection<ProcessRole> processRoles) {
        return processRoles.stream()
                .map(ProcessRole::getStringId)
                .collect(Collectors.toSet());
    }

    private void runAllPreActions(Set<ProcessRole> newProcessRoles, Set<ProcessRole> removedProcessRoles, IUser user, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newProcessRoles, EventType.ASSIGN, EventPhaseType.PRE, user, params);
        runAllSuitableActionsOnRoles(removedProcessRoles, EventType.CANCEL, EventPhaseType.PRE, user, params);
    }

    private void runAllPostActions(Set<ProcessRole> newProcessRoles, Set<ProcessRole> removedProcessRoles, IUser user, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newProcessRoles, EventType.ASSIGN, EventPhaseType.POST, user, params);
        runAllSuitableActionsOnRoles(removedProcessRoles, EventType.CANCEL, EventPhaseType.POST, user, params);
    }

    private void runAllSuitableActionsOnRoles(Set<ProcessRole> processRoles, EventType requiredEventType, EventPhaseType requiredPhase, IUser user, Map<String, String> params) {
        processRoles.forEach(role -> {
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

    private void removeOldAndAssignNewRolesToUser(IUser user, Set<ProcessRole> requestedProcessRoles) {
        // todo 2058
//        user.getRoles().clear();
//        user.getRoles().addAll(requestedRoles);

        userService.save(user);
    }

    @Override
    public List<ProcessRole> findAll() {
        return processRoleRepository.findAll();
    }

    @Override
    public List<ProcessRole> findAll(String netId) {
        // TODO: release/8.0.0 fix
        return null;
    }

    @Override
    public ProcessRole defaultRole() {
        // TODO: release/8.0.0 duplicate code
        if (defaultProcessRole == null) {
            Set<ProcessRole> processRoles = processRoleRepository.findAllByTitle_DefaultValue(ProcessRole.DEFAULT_ROLE);
            if (processRoles.isEmpty()) {
                throw new IllegalStateException("No default process role has been found!");
            }
            if (processRoles.size() > 1) {
                throw new IllegalStateException("More than 1 default process role exists!");
            }
            defaultProcessRole = processRoles.stream().findFirst().orElse(null);
        }
        return defaultProcessRole;
    }

    @Override
    public ProcessRole anonymousRole() {
        if (anonymousProcessRole == null) {
            Set<ProcessRole> processRoles = processRoleRepository.findAllByImportId(ProcessRole.ANONYMOUS_ROLE);
            if (processRoles.isEmpty()) {
                throw new IllegalStateException("No anonymous process role has been found!");
            }
            if (processRoles.size() > 1) {
                throw new IllegalStateException("More than 1 anonymous process role exists!");
            }
            anonymousProcessRole = processRoles.stream().findFirst().orElse(null);
        }
        return anonymousProcessRole;
    }

    /**
     * @param importId id from a process of a role
     * @return a process role object
     * @deprecated use {@link ProcessRoleService#findAllByImportId(String)} instead
     */
    @Deprecated(forRemoval = true, since = "6.2.0")
    @Override
    public ProcessRole findByImportId(String importId) {
        return processRoleRepository.findAllByImportId(importId).stream().findFirst().orElse(null);
    }

    @Override
    public Set<ProcessRole> findAllByImportId(String importId) {
        return processRoleRepository.findAllByImportId(importId);
    }

    @Override
    public Set<ProcessRole> findAllByDefaultTitle(String name) {
        return processRoleRepository.findAllByTitle_DefaultValue(name);
    }

    @Override
    public ProcessRole findById(String id) {
        return processRoleRepository.findById(id).orElse(null);
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
            // todo 2058
//            if (user.getRoles().isEmpty()) {
//                continue;
//            }

//            Set<String> newRoles = user.getRoles().stream()
//                    .filter(role -> !deletedRoleStringIds.contains(role.getStringId()))
//                    .map(Role::getStringId)
//                    .collect(Collectors.toSet());
//            this.assignRolesToUser(user.getStringId(), newRoles, loggedUser);
        }

        log.info("[{}]: Deleting all roles of Petri net {} version {}", net.getStringId(), net.getIdentifier(), net.getVersion().toString());
        this.processRoleRepository.deleteAllByIdIn(deletedRoleIds);
    }

    // TODO: release/8.0.0 cache
    @Override
    public boolean existsByImportId(String importId) {
        return processRoleRepository.existsByImportId(importId);
    }

    public void clearCache() {
        this.defaultProcessRole = null;
        this.anonymousProcessRole = null;
    }
}
