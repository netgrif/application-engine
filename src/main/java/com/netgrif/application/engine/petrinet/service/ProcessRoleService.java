package com.netgrif.application.engine.petrinet.service;

import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.adapter.auth.service.UserService;
import com.netgrif.core.event.events.user.UserRoleChangeEvent;
import com.netgrif.core.importer.model.EventPhaseType;
import com.netgrif.core.petrinet.domain.PetriNet;
import com.netgrif.core.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.RoleContext;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.RoleActionsRunner;
import com.netgrif.core.petrinet.domain.events.Event;
import com.netgrif.core.petrinet.domain.events.EventType;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.core.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.adapter.petrinet.service.PetriNetService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.core.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ProcessRoleService implements com.netgrif.adapter.petrinet.service.ProcessRoleService {

    private static final Logger log = LoggerFactory.getLogger(ProcessRoleService.class);

    private final UserService userService;
    private final ProcessRoleRepository processRoleRepository;
    private final PetriNetRepository netRepository;
    private final ApplicationEventPublisher publisher;
    private final RoleActionsRunner roleActionsRunner;
    private final PetriNetService petriNetService;
    private final ISecurityContextService securityContextService;

    private ProcessRole defaultRole;
    private ProcessRole anonymousRole;

    public ProcessRoleService(ProcessRoleRepository processRoleRepository,
                              PetriNetRepository netRepository,
                              ApplicationEventPublisher publisher, RoleActionsRunner roleActionsRunner,
                              @Lazy PetriNetService petriNetService, @Lazy UserService userService, ISecurityContextService securityContextService) {
        this.processRoleRepository = processRoleRepository;
        this.netRepository = netRepository;
        this.publisher = publisher;
        this.roleActionsRunner = roleActionsRunner;
        this.petriNetService = petriNetService;
        this.userService = userService;
        this.securityContextService = securityContextService;
    }

    @Override
    public ProcessRole save(ProcessRole processRole) {
        return null;
    }

    @Override
    public List<ProcessRole> getAll() {
        return List.of();
    }

    @Override
    public List<ProcessRole> findAllByNetId(String s) {
        return List.of();
    }

    @Override
    public Optional<ProcessRole> get(ProcessResourceId processResourceId) {
        return Optional.empty();
    }

    @Override
    public void delete(String s) {

    }

    @Override
    public void deleteAll(Collection<String> collection) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public ProcessRole getDefaultRole() {
        return null;
    }

    @Override
    public ProcessRole getAnonymousRole() {
        return null;
    }

    @Override
    public Collection<ProcessRole> findAllByIds(Collection<ProcessResourceId> collection) {
        return List.of();
    }

    @Override
    public ProcessRole findById(ProcessResourceId processResourceId) {
        return null;
    }

    @Override
    public List<ProcessRole> saveAll(Iterable<ProcessRole> entities) {
        return StreamSupport.stream(entities.spliterator(), false).map(processRole -> {
            if (!processRole.isGlobal() || processRoleRepository.findAllByImportId(processRole.getImportId()).isEmpty()) {
                return processRoleRepository.save(processRole);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Set<ProcessRole> findByIds(Set<String> ids) {
        return StreamSupport.stream(processRoleRepository.findAllById(ids).spliterator(), false).collect(Collectors.toSet());
    }

    @Override
    public void assignRolesToUser(String userId, Set<ProcessResourceId> requestedRolesIds, LoggedUser loggedUser) {
        assignRolesToUser(userId, requestedRolesIds, loggedUser, new HashMap<>());
    }

    @Override
    public void assignRolesToUser(String userId, Set<ProcessResourceId> requestedRolesIds, LoggedUser loggedUser, Map<String, String> params) {
        IUser user = userService.findById(userId, null);
        Set<ProcessRole> requestedRoles = this.findByIds(requestedRolesIds.stream().map(ProcessResourceId::toString).collect(Collectors.toSet()));
        if (requestedRoles.isEmpty() && !requestedRolesIds.isEmpty())
            throw new IllegalArgumentException("No process roles found.");
        if (requestedRoles.size() != requestedRolesIds.size())
            throw new IllegalArgumentException("Not all process roles were found!");

        Set<ProcessRole> userOldRoles = user.getProcessRoles();

        Set<ProcessRole> rolesNewToUser = getRolesNewToUser(userOldRoles, requestedRoles);
        Set<ProcessRole> rolesRemovedFromUser = getRolesRemovedFromUser(userOldRoles, requestedRoles);

        String idOfPetriNetContainingRole = getPetriNetIdRoleBelongsTo(rolesNewToUser, rolesRemovedFromUser);
        if (!isGlobalFromFirstRole(rolesNewToUser) && !isGlobalFromFirstRole(rolesRemovedFromUser) && idOfPetriNetContainingRole == null) {
            return;
        }
        PetriNet petriNet = null;
        if (idOfPetriNetContainingRole != null) {
            petriNet = petriNetService.getPetriNet(idOfPetriNetContainingRole);
        }
        Set<String> rolesNewToUserIds = mapUserRolesToIds(rolesNewToUser);
        Set<String> rolesRemovedFromUserIds = mapUserRolesToIds(rolesRemovedFromUser);

        Set<ProcessRole> newRoles = this.findByIds(rolesNewToUserIds);
        Set<ProcessRole> removedRoles = this.findByIds(rolesRemovedFromUserIds);
        if (petriNet != null) {
            runAllPreActions(newRoles, removedRoles, user, petriNet, params);
        }
        requestedRoles = updateRequestedRoles(user, rolesNewToUser, rolesRemovedFromUser);

        replaceUserRolesAndPublishEvent(requestedRolesIds.stream().map(ProcessResourceId::toString).collect(Collectors.toSet()), user, requestedRoles);
        if (petriNet != null) {
            runAllPostActions(newRoles, removedRoles, user, petriNet, params);
        }
        securityContextService.saveToken(userId);
        if (Objects.equals(userId, loggedUser.getId())) {
            loggedUser.getProcessRoles().clear();
            loggedUser.setProcessRoles(user.getProcessRoles());
            securityContextService.reloadSecurityContext(loggedUser);
        }
    }

    private Set<ProcessRole> updateRequestedRoles(IUser user, Set<ProcessRole> rolesNewToUser, Set<ProcessRole> rolesRemovedFromUser) {
        Set<ProcessRole> userRolesAfterPreActions = user.getProcessRoles();
        userRolesAfterPreActions.addAll(rolesNewToUser);
        userRolesAfterPreActions.removeAll(rolesRemovedFromUser);

        return new HashSet<>(userRolesAfterPreActions);
    }

    private String getPetriNetIdRoleBelongsTo(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles) {

        if (!newRoles.isEmpty()) {
            return getPetriNetIdFromFirstRole(newRoles);
        }

        if (!removedRoles.isEmpty()) {
            return getPetriNetIdFromFirstRole(removedRoles);
        }

        return null;
    }

    private boolean isGlobalFromFirstRole(Set<ProcessRole> roles) {
        if (roles.isEmpty()) {
            return false;
        }
        ProcessRole role = roles.iterator().next();
        return role.isGlobal();
    }

    private String getPetriNetIdFromFirstRole(Set<ProcessRole> newRoles) {
        return newRoles.iterator().next().getNetId();
    }

    private void replaceUserRolesAndPublishEvent(Set<String> requestedRolesIds, IUser user, Set<ProcessRole> requestedRoles) {
        removeOldAndAssignNewRolesToUser(user, requestedRoles);
        publisher.publishEvent(new UserRoleChangeEvent(userService.transformToLoggedUser(user), this.findByIds(requestedRolesIds)));
    }

    private Set<ProcessRole> getRolesNewToUser(Set<ProcessRole> userOldRoles, Set<ProcessRole> newRequestedRoles) {
        Set<ProcessRole> rolesNewToUser = new HashSet<>(newRequestedRoles);
        rolesNewToUser.removeAll(userOldRoles);

        return rolesNewToUser;
    }

    private Set<ProcessRole> getRolesRemovedFromUser(Set<ProcessRole> userOldRoles, Set<ProcessRole> newRequestedRoles) {
        Set<ProcessRole> rolesRemovedFromUser = new HashSet<>(userOldRoles);
        rolesRemovedFromUser.removeAll(newRequestedRoles);

        return rolesRemovedFromUser;
    }

    private Set<String> mapUserRolesToIds(Collection<ProcessRole> processRoles) {
        return processRoles.stream()
                .map(ProcessRole::getStringId)
                .collect(Collectors.toSet());
    }

    private void runAllPreActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles, IUser user, PetriNet petriNet, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.PRE, user, petriNet, params);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.PRE, user, petriNet, params);
    }

    private void runAllPostActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles, IUser user, PetriNet petriNet, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.POST, user, petriNet, params);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.POST, user, petriNet, params);
    }

    private void runAllSuitableActionsOnRoles(Set<ProcessRole> roles, EventType requiredEventType, EventPhaseType requiredPhase, IUser user, PetriNet petriNet, Map<String, String> params) {
        roles.forEach(role -> {
            RoleContext roleContext = new RoleContext<>(user, role, petriNet);
            //MODULARISATION: events to be resolved
//            runAllSuitableActionsOnOneRole(role.getEvents(), requiredEventType, requiredPhase, roleContext, params);
        });
    }

    private void runAllSuitableActionsOnOneRole(Map<EventType, Event> eventMap, EventType requiredEventType, EventPhaseType requiredPhase, RoleContext roleContext, Map<String, String> params) {
        if (eventMap == null) {
            return;
        }
        eventMap.forEach((eventType, event) -> {

            if (eventType != requiredEventType) {
                return;
            }

            runActionsBasedOnPhase(event, requiredPhase, roleContext, params);
        });
    }

    private void runActionsBasedOnPhase(Event event, EventPhaseType requiredPhase, RoleContext roleContext, Map<String, String> params) {
        switch (requiredPhase) {
            case PRE:
                runActions(event.getPreActions(), roleContext, params);
                break;
            case POST:
                runActions(event.getPostActions(), roleContext, params);
                break;
        }
    }

    private void runActions(List<Action> actions, RoleContext roleContext, Map<String, String> params) {
        actions.forEach(action -> roleActionsRunner.run(action, roleContext, params));
    }

    private void removeOldAndAssignNewRolesToUser(IUser user, Set<ProcessRole> requestedRoles) {
        user.getProcessRoles().clear();
        user.getProcessRoles().addAll(requestedRoles);

        userService.saveUser(user, null);
    }

    @Override
    public List<ProcessRole> findAll() {
        return processRoleRepository.findAll();
    }

    @Override
    public Set<ProcessRole> findAllGlobalRoles() {
        return processRoleRepository.findAllByGlobalIsTrue();
    }

    @Override
    public List<ProcessRole> findAll(String netId) {
        Optional<PetriNet> netOptional = netRepository.findById(netId);
        if (netOptional.isEmpty())
            throw new IllegalArgumentException("Could not find model with id [" + netId + "]");
        return findAll(netOptional.get());
    }

    private List<ProcessRole> findAll(PetriNet net) {
        return new LinkedList<>(net.getRoles().values());
    }

    @Override
    public ProcessRole defaultRole() {
        if (defaultRole == null) {
            Set<ProcessRole> roles = processRoleRepository.findAllByName_DefaultValue(ProcessRole.DEFAULT_ROLE);
            if (roles.isEmpty())
                throw new IllegalStateException("No default process role has been found!");
            if (roles.size() > 1)
                throw new IllegalStateException("More than 1 default process role exists!");
            defaultRole = roles.stream().findFirst().orElse(null);
        }
        return defaultRole;
    }

    @Override
    public ProcessRole anonymousRole() {
        if (anonymousRole == null) {
            Set<ProcessRole> roles = processRoleRepository.findAllByImportId(ProcessRole.ANONYMOUS_ROLE);
            if (roles.isEmpty())
                throw new IllegalStateException("No anonymous process role has been found!");
            if (roles.size() > 1)
                throw new IllegalStateException("More than 1 anonymous process role exists!");
            anonymousRole = roles.stream().findFirst().orElse(null);
        }
        return anonymousRole;
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
    public Set<ProcessRole> findAllByDefaultName(String name) {
        return processRoleRepository.findAllByName_DefaultValue(name);
    }

    @Override
    public ProcessRole findById(String id) {
        ObjectId objectId = extractObjectId(id);
        return processRoleRepository.findByIdObjectId(objectId).orElse(null);
    }

    @Override
    public void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser) {
        log.info("[" + net.getStringId() + "]: Initiating deletion of all roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());
        List<ProcessResourceId> deletedRoleIds = this.findAll(net.getStringId()).stream().filter(processRole -> processRole.getNetId() != null).map(ProcessRole::get_id).collect(Collectors.toList());
        Set<String> deletedRoleStringIds = deletedRoleIds.stream().map(ProcessResourceId::toString).collect(Collectors.toSet());

        List<IUser> usersWithRemovedRoles = this.userService.findAllByProcessRoles(new HashSet<>(deletedRoleIds), null);
        for (IUser user : usersWithRemovedRoles) {
            log.info("[" + net.getStringId() + "]: Removing deleted roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString() + " from user " + user.getFullName() + " with id " + user.getStringId());

            if (user.getProcessRoles().size() == 0)
                continue;

            Set<ProcessResourceId> newRoles = user.getProcessRoles().stream()
                    .filter(role -> !deletedRoleStringIds.contains(role.getStringId()))
                    .map(ProcessRole::get_id)
                    .collect(Collectors.toSet());
            this.assignRolesToUser(user.getStringId(), newRoles, loggedUser);
        }

        log.info("[" + net.getStringId() + "]: Deleting all roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());
        this.processRoleRepository.deleteAllBy_idIn(deletedRoleIds);
    }

    public void clearCache() {
        this.defaultRole = null;
        this.anonymousRole = null;
    }

    private ObjectId extractObjectId(String caseId) {
        String[] parts = caseId.split("-");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid NetgrifId format: " + caseId);
        }
        String objectIdPart = parts[1];

        return new ObjectId(objectIdPart);
    }
}
