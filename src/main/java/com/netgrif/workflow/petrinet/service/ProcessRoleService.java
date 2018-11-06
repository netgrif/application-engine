package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.service.UserDetailsServiceImpl;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.user.UserRoleChangeEvent;
import com.netgrif.workflow.importer.model.EventPhaseType;
import com.netgrif.workflow.petrinet.domain.Event;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.EventType;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessRoleService implements IProcessRoleService {

    @Autowired
    private IUserService userService;

    @Autowired
    private UserProcessRoleRepository roleRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private PetriNetRepository netRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private FieldActionsRunner fieldActionsRunner;

    @Override
    public void assignRolesToUser(Long userId, Set<String> requestedRolesIds, LoggedUser loggedUser) {
        User user = userService.findById(userId, true);
        List<UserProcessRole> requestedRoles = roleRepository.findByRoleIdIn(requestedRolesIds);
        if (requestedRoles.isEmpty())
            throw new IllegalArgumentException("No process roles found.");
        if (requestedRoles.size() != requestedRolesIds.size())
            throw new IllegalArgumentException("Not all process roles were found!");

        Set<UserProcessRole> userOldRoles = user.getUserProcessRoles();

        Set<UserProcessRole> rolesNewToUser = getRolesNewToUser(userOldRoles, requestedRoles);
        Set<UserProcessRole> rolesRemovedFromUser = getRolesRemovedFromUser(userOldRoles, requestedRoles);

        Set<String> rolesNewToUserIds = mapUserRolesToIds(rolesNewToUser);
        Set<String> rolesRemovedFromUserIds = mapUserRolesToIds(rolesRemovedFromUser);

        Set<ProcessRole> newRoles = processRoleRepository.findAllBy_idIn(rolesNewToUserIds);
        Set<ProcessRole> removedRoles = processRoleRepository.findAllBy_idIn(rolesRemovedFromUserIds);


        runAllPreActions(newRoles, removedRoles);
        replaceUserRolesAndPublishEvent(requestedRolesIds, user, requestedRoles);
        runAllPostActions(newRoles, removedRoles);


        if (Objects.equals(userId, loggedUser.getId())) {
            loggedUser.getProcessRoles().clear();
            loggedUser.parseProcessRoles(user.getUserProcessRoles());
            userDetailsService.reloadSecurityContext(loggedUser);
        }
    }

    private void replaceUserRolesAndPublishEvent(Set<String> requestedRolesIds, User user, List<UserProcessRole> requestedRoles) {
        assignNewRolesToUser(user, requestedRoles);
        publisher.publishEvent(new UserRoleChangeEvent(user, processRoleRepository.findAllBy_idIn(requestedRolesIds)));
    }

    private Set<UserProcessRole> getRolesNewToUser(Set<UserProcessRole> userOldRoles, List<UserProcessRole> newRequestedRoles) {
        Set<UserProcessRole> rolesNewToUser = new HashSet<>(newRequestedRoles);
        rolesNewToUser.removeAll(userOldRoles);

        return rolesNewToUser;
    }

    private Set<UserProcessRole> getRolesRemovedFromUser(Set<UserProcessRole> userOldRoles, List<UserProcessRole> newRequestedRoles) {
        Set<UserProcessRole> rolesRemovedFromUser = new HashSet<>(userOldRoles);
        rolesRemovedFromUser.removeAll(newRequestedRoles);

        return rolesRemovedFromUser;
    }

    private Set<String> mapUserRolesToIds(Collection<UserProcessRole> userProcessRoles) {
        return userProcessRoles.stream()
                .map(role -> role.getRoleId())
                .collect(Collectors.toSet());
    }

    private void runAllPreActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.PRE);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.PRE);
    }

    private void runAllPostActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.POST);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.POST);
    }

    private void runAllSuitableActionsOnRoles(Set<ProcessRole> roles, EventType requiredEventType, EventPhaseType requiredPhase) {
        roles.forEach(role -> runAllSuitableActionsOnOneRole(role.getEvents(), requiredEventType, requiredPhase));
    }

    private void runAllSuitableActionsOnOneRole(Map<EventType, Event> eventMap, EventType requiredEventType, EventPhaseType requiredPhase) {
        eventMap.forEach((eventType, event) -> {

            if(eventType != requiredEventType) {
                return;
            }

            runActionsBasedOnPhase(event, requiredPhase);
        });
    }

    private void runActionsBasedOnPhase(Event event, EventPhaseType requiredPhase) {
        switch (requiredPhase) {
            case PRE:
                runActions(event.getPreActions());
                break;
            case POST:
                runActions(event.getPostActions());
                break;
        }
    }

    private void runActions(List<Action> actions) {
        actions.forEach(fieldActionsRunner::run);
    }

    private void assignNewRolesToUser(User user, List<UserProcessRole> requestedRoles) {
        user.getUserProcessRoles().clear();
        user.getUserProcessRoles().addAll(requestedRoles);

        userService.save(user);
    }

    @Override
    public List<ProcessRole> findAll(String netId) {
        PetriNet net = netRepository.findOne(netId);
        return new LinkedList<>(net.getRoles().values());
    }
}