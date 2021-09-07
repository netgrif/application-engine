package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.UserDetailsServiceImpl;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.user.UserRoleChangeEvent;
import com.netgrif.workflow.importer.model.EventPhaseType;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.context.RoleContext;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner.RoleActionsRunner;
import com.netgrif.workflow.petrinet.domain.events.Event;
import com.netgrif.workflow.petrinet.domain.events.EventType;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessRoleService implements IProcessRoleService {

    private static final Logger log = LoggerFactory.getLogger(ProcessRoleService.class);

    @Autowired
    private IUserService userService;

    /*@Autowired
    private UserProcessRoleRepository roleRepository;*/

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private PetriNetRepository netRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private RoleActionsRunner roleActionsRunner;

    @Autowired
    private IPetriNetService petriNetService;

    private ProcessRole defaultRole;

    @Override
    public List<ProcessRole> saveAll(Iterable<ProcessRole> entities) {
        return processRoleRepository.saveAll(entities);
    }

    @Override
    public Set<ProcessRole> findByIds(Set<String> ids) {
        return processRoleRepository.findAllBy_idIn(ids);
    }

    @Override
    public void assignRolesToUser(String userId, Set<String> requestedRolesIds, LoggedUser loggedUser) {
        IUser user = userService.resolveById(userId, true);
        Set<ProcessRole> requestedRoles = processRoleRepository.findAllBy_idIn(requestedRolesIds);
        if (requestedRoles.isEmpty() && requestedRolesIds.size() != 0)
            throw new IllegalArgumentException("No process roles found.");
        if (requestedRoles.size() != requestedRolesIds.size())
            throw new IllegalArgumentException("Not all process roles were found!");

        Set<ProcessRole> userOldRoles = user.getProcessRoles();

        Set<ProcessRole> rolesNewToUser = getRolesNewToUser(userOldRoles, requestedRoles);
        Set<ProcessRole> rolesRemovedFromUser = getRolesRemovedFromUser(userOldRoles, requestedRoles);

        String idOfPetriNetContainingRole = getPetriNetIdRoleBelongsTo(rolesNewToUser, rolesRemovedFromUser);

        if (idOfPetriNetContainingRole == null)
            return;

        PetriNet petriNet = petriNetService.getPetriNet(idOfPetriNetContainingRole);

        Set<String> rolesNewToUserIds = mapUserRolesToIds(rolesNewToUser);
        Set<String> rolesRemovedFromUserIds = mapUserRolesToIds(rolesRemovedFromUser);

        Set<ProcessRole> newRoles = processRoleRepository.findAllBy_idIn(rolesNewToUserIds);
        Set<ProcessRole> removedRoles = processRoleRepository.findAllBy_idIn(rolesRemovedFromUserIds);

        runAllPreActions(newRoles, removedRoles, user, petriNet);
        requestedRoles = updateRequestedRoles(user, rolesNewToUser, rolesRemovedFromUser);

        replaceUserRolesAndPublishEvent(requestedRolesIds, user, requestedRoles);
        runAllPostActions(newRoles, removedRoles, user, petriNet);

        if (Objects.equals(userId, loggedUser.getId())) {
            loggedUser.getProcessRoles().clear();
            loggedUser.parseProcessRoles(user.getProcessRoles());
            userDetailsService.reloadSecurityContext(loggedUser);
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

    private String getPetriNetIdFromFirstRole(Set<ProcessRole> newRoles) {
        return newRoles.iterator().next().getNetId();
    }

    private void replaceUserRolesAndPublishEvent(Set<String> requestedRolesIds, IUser user, Set<ProcessRole> requestedRoles) {
        removeOldAndAssignNewRolesToUser(user, requestedRoles);
        publisher.publishEvent(new UserRoleChangeEvent(user, processRoleRepository.findAllBy_idIn(requestedRolesIds)));
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

    private void runAllPreActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles, IUser user, PetriNet petriNet) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.PRE, user, petriNet);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.PRE, user, petriNet);
    }

    private void runAllPostActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles, IUser user, PetriNet petriNet) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.POST, user, petriNet);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.POST, user, petriNet);
    }

    private void runAllSuitableActionsOnRoles(Set<ProcessRole> roles, EventType requiredEventType, EventPhaseType requiredPhase, IUser user, PetriNet petriNet) {
        roles.forEach(role -> {
            RoleContext roleContext = new RoleContext<>(user, role, petriNet);
            runAllSuitableActionsOnOneRole(role.getEvents(), requiredEventType, requiredPhase, roleContext);
        });
    }

    private void runAllSuitableActionsOnOneRole(Map<EventType, Event> eventMap, EventType requiredEventType, EventPhaseType requiredPhase, RoleContext roleContext) {
        if (eventMap == null) {
            return;
        }
        eventMap.forEach((eventType, event) -> {

            if (eventType != requiredEventType) {
                return;
            }

            runActionsBasedOnPhase(event, requiredPhase, roleContext);
        });
    }

    private void runActionsBasedOnPhase(Event event, EventPhaseType requiredPhase, RoleContext roleContext) {
        switch (requiredPhase) {
            case PRE:
                runActions(event.getPreActions(), roleContext);
                break;
            case POST:
                runActions(event.getPostActions(), roleContext);
                break;
        }
    }

    private void runActions(List<Action> actions, RoleContext roleContext) {
        actions.forEach(action -> roleActionsRunner.run(action, roleContext));
    }

    private void removeOldAndAssignNewRolesToUser(IUser user, Set<ProcessRole> requestedRoles) {
        user.getProcessRoles().clear();
        user.getProcessRoles().addAll(requestedRoles);

        userService.save(user);
    }

    @Override
    public List<ProcessRole> findAll() {
        return processRoleRepository.findAll();
    }

    @Override
    public List<ProcessRole> findAll(String netId) {
        Optional<PetriNet> netOptional = netRepository.findById(netId);
        if (!netOptional.isPresent())
            throw new IllegalArgumentException("Could not find model with id [" + netId + "]");
        return findAll(netOptional.get());
    }

    private List<ProcessRole> findAll(PetriNet net) {
        return new LinkedList<>(net.getRoles().values());
    }

    @Override
    public ProcessRole defaultRole() {
        if (defaultRole == null)
            defaultRole = processRoleRepository.findByName_DefaultValue(ProcessRole.DEFAULT_ROLE);
        return defaultRole;
    }

    @Override
    public ProcessRole findByImportId(String importId) {
        return processRoleRepository.findByImportId(importId);
    }

    @Override
    public ProcessRole findById(String id) {
        return processRoleRepository.findBy_id(id);
    }

    @Override
    public void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser) {
        log.info("[" + net.getStringId() + "]: Initiating deletion of all roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());
        List<ObjectId> deletedRoleIds = this.findAll(net).stream().map(ProcessRole::get_id).collect(Collectors.toList());
        Set<String> deletedRoleStringIds = deletedRoleIds.stream().map(ObjectId::toString).collect(Collectors.toSet());

        List<IUser> usersWithRemovedRoles = this.userService.findAllByProcessRoles(deletedRoleStringIds, false);
        for (IUser user : usersWithRemovedRoles) {
            log.info("[" + net.getStringId() + "]: Removing deleted roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString() + " from user " + user.getFullName() + " with id " + user.getStringId());

            if (user.getProcessRoles().size() == 0)
                continue;

            Set<String> newRoles = user.getProcessRoles().stream()
                    .filter(role -> !deletedRoleStringIds.contains(role.getStringId()))
                    .map(ProcessRole::getStringId)
                    .collect(Collectors.toSet());
            this.assignRolesToUser(user.getStringId(), newRoles, loggedUser);
        }

        log.info("[" + net.getStringId() + "]: Deleting all roles of Petri net " + net.getIdentifier() + " version " + net.getVersion().toString());
        this.processRoleRepository.deleteAllBy_idIn(deletedRoleIds);
    }

    public void clearCache() {
        this.defaultRole = null;
    }
}