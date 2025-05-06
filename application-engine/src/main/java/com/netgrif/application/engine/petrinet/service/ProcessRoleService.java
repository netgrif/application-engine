package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.event.events.user.UserRoleChangeEvent;
import com.netgrif.application.engine.objects.importer.model.EventPhaseType;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.core.auth.domain.AbstractUser;
import com.netgrif.core.auth.domain.Group;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.auth.service.UserService;
import com.netgrif.core.event.events.user.UserRoleChangeEvent;
import com.netgrif.core.importer.model.EventPhaseType;
import com.netgrif.core.petrinet.domain.PetriNet;
import com.netgrif.core.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.RoleContext;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.RoleActionsRunner;
import com.netgrif.application.engine.objects.petrinet.domain.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ProcessRoleService implements com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService {

    private static final Logger log = LoggerFactory.getLogger(ProcessRoleService.class);

    private final UserService userService;
    private final ProcessRoleRepository processRoleRepository;
    private final PetriNetRepository netRepository;
    private final ApplicationEventPublisher publisher;
    private final RoleActionsRunner roleActionsRunner;
    private final IPetriNetService petriNetService;
    private final ISecurityContextService securityContextService;

    private ProcessRole defaultRole;
    private ProcessRole anonymousRole;

    public ProcessRoleService(ProcessRoleRepository processRoleRepository,
                              PetriNetRepository netRepository,
                              ApplicationEventPublisher publisher, RoleActionsRunner roleActionsRunner,
                              @Lazy IPetriNetService petriNetService, @Lazy UserService userService, ISecurityContextService securityContextService) {
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
        return processRoleRepository.save(processRole);
    }

    @Override
    public void delete(String s) {
        Optional<ProcessRole> processRole = processRoleRepository.findByCompositeId(s);
        processRole.ifPresent(processRoleRepository::delete);
    }

    @Override
    public void deleteAll(Collection<String> collection) {
        List<ProcessRole> processRoles = processRoleRepository.findAllByCompositeId(collection);
        processRoleRepository.deleteAll(processRoles);
    }

    @Override
    public void deleteAll() {
        processRoleRepository.deleteAll();
    }

    @Override
    public ProcessRole getDefaultRole() {
        return processRoleRepository.findAllByImportId(ProcessRole.DEFAULT_ROLE).stream().findFirst().orElse(null);
    }

    @Override
    public ProcessRole getAnonymousRole() {
        return processRoleRepository.findAllByImportId(ProcessRole.ANONYMOUS_ROLE).stream().findFirst().orElse(null);
    }

    @Override
    public Set<ProcessRole> saveAll(Iterable<ProcessRole> entities) {
        return StreamSupport.stream(entities.spliterator(), false).map(processRole -> {
            if (!processRole.isGlobal() || processRoleRepository.findAllByImportId(processRole.getImportId()).isEmpty()) {
                return processRoleRepository.save(processRole);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public void assignRolesToUser(AbstractUser user, Set<ProcessResourceId> requestedRolesIds, LoggedUser loggedUser) {
        assignRolesToUser(user, requestedRolesIds, loggedUser, new HashMap<>());
    }

    @Override
    public void assignRolesToUser(AbstractUser user, Set<ProcessResourceId> requestedRolesIds, LoggedUser loggedUser, Map<String, String> params) {
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
        securityContextService.saveToken(user.getStringId());
        if (Objects.equals(user.getStringId(), loggedUser.getStringId())) {
            loggedUser.getProcessRoles().clear();
            loggedUser.setProcessRoles(user.getProcessRoles());
            securityContextService.reloadSecurityContext(loggedUser);
        }
    }

    @Override
    public void assignRolesToGroup(Group group, Set<ProcessResourceId> requestedRolesIds) {

    }

    @Override
    public void assignNegativeRolesToUser(AbstractUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser) {

    }

    @Override
    public void assignNegativeRolesToUser(AbstractUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser, Map<String, String> params) {

    }

    @Override
    public void assignNegativeRolesToGroup(Group group, Set<ProcessResourceId> requestedRolesIds) {

    }

    private Set<ProcessRole> updateRequestedRoles(AbstractUser user, Set<ProcessRole> rolesNewToUser, Set<ProcessRole> rolesRemovedFromUser) {
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
        return newRoles.iterator().next().getProcessId();
    }

    private void replaceUserRolesAndPublishEvent(Set<String> requestedRolesIds, AbstractUser user, Set<ProcessRole> requestedRoles) {
        removeOldAndAssignNewRolesToUser(user, requestedRoles);
        publisher.publishEvent(new UserRoleChangeEvent(user, this.findByIds(requestedRolesIds)));
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

    private void runAllPreActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles, AbstractUser user, PetriNet petriNet, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.PRE, user, petriNet, params);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.PRE, user, petriNet, params);
    }

    private void runAllPostActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles, AbstractUser user, PetriNet petriNet, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.POST, user, petriNet, params);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.POST, user, petriNet, params);
    }

    private void runAllSuitableActionsOnRoles(Set<ProcessRole> roles, EventType requiredEventType, EventPhaseType requiredPhase, AbstractUser user, PetriNet petriNet, Map<String, String> params) {
        roles.forEach(role -> {
            RoleContext roleContext = new RoleContext<>(user, role, petriNet);
            runAllSuitableActionsOnOneRole(role.getEvents(), requiredEventType, requiredPhase, roleContext, params);
        });
    }

    private void runAllSuitableActionsOnOneRole(Map<EventType, Event> eventMap, EventType requiredEventType, EventPhaseType requiredPhase, RoleContext<?> roleContext, Map<String, String> params) {
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

    private void runActionsBasedOnPhase(Event event, EventPhaseType requiredPhase, RoleContext<?> roleContext, Map<String, String> params) {
        switch (requiredPhase) {
            case PRE:
                runActions(event.getPreActions(), roleContext, params);
                break;
            case POST:
                runActions(event.getPostActions(), roleContext, params);
                break;
        }
    }

    private void runActions(List<Action> actions, RoleContext<?> roleContext, Map<String, String> params) {
        actions.forEach(action -> roleActionsRunner.run(action, roleContext, params));
    }

    private void removeOldAndAssignNewRolesToUser(AbstractUser user, Set<ProcessRole> requestedRoles) {
        user.getProcessRoles().clear();
        user.getProcessRoles().addAll(requestedRoles);

        userService.saveUser(user, null);
    }

    @Override
    public Set<ProcessRole> findAll(Pageable pageable) {
        return processRoleRepository.findAll(pageable).toSet();
    }

    @Override
    public Set<ProcessRole> findAllByNetStringId(String netStringId) {
        Optional<PetriNet> netOptional = netRepository.findById(netStringId);
        if (netOptional.isEmpty())
            throw new IllegalArgumentException("Could not find model with id [" + netStringId + "]");
        return new HashSet<>(netOptional.get().getRoles().values());
    }

    @Override
    public Set<ProcessRole> findAllByNetIdentifier(String identifier) {
        return processRoleRepository.findAllByProcessId(identifier);
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
    public Collection<ProcessRole> findAllByIds(Collection<ProcessResourceId> collection) {
        return processRoleRepository.findAllByCompositeId(collection.stream().map(ProcessResourceId::getStringId).collect(Collectors.toList()));
    }

    @Override
    public ProcessRole findById(ProcessResourceId processResourceId) {
        return processRoleRepository.findByCompositeId(processResourceId.getStringId()).orElse(null);
    }

    @Override
    public Set<ProcessRole> findByIds(Set<String> ids) {
        return new HashSet<>(processRoleRepository.findAllById(ids));
    }

    @Override
    public Set<ProcessRole> findAllGlobalRoles() {
        return processRoleRepository.findAllByGlobalIsTrue();
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

    @Override
    public void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser) {
        log.info("[" + net.getStringId() + "]: Initiating deletion of all roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());
        List<ProcessResourceId> deletedRoleIds = this.findAllByNetStringId(net.getStringId()).stream().filter(processRole -> processRole.getProcessId() != null).map(ProcessRole::get_id).collect(Collectors.toList());
        Set<String> deletedRoleStringIds = deletedRoleIds.stream().map(ProcessResourceId::toString).collect(Collectors.toSet());

        Set<AbstractUser> usersWithRemovedRoles = this.userService.findAllByProcessRoles(new HashSet<>(deletedRoleIds), null);
        for (AbstractUser user : usersWithRemovedRoles) {
            log.info("[" + net.getStringId() + "]: Removing deleted roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString() + " from user " + user.getName() + " with id " + user.getStringId());

            if (user.getProcessRoles().isEmpty())
                continue;

            Set<ProcessResourceId> newRoles = user.getProcessRoles().stream()
                    .filter(role -> !deletedRoleStringIds.contains(role.getStringId()))
                    .map(ProcessRole::get_id)
                    .collect(Collectors.toSet());
            this.assignRolesToUser(user, newRoles, loggedUser);
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
