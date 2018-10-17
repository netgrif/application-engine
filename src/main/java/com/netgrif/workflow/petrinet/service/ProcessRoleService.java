package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.service.UserDetailsServiceImpl;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.user.UserRoleChangeEvent;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Override
    public void assignRolesToUser(Long userId, Set<String> requestedRolesIds, LoggedUser loggedUser) {
        User user = userService.findById(userId, true);
        List<UserProcessRole> requestedRoles = roleRepository.findByRoleIdIn(requestedRolesIds);
        if (requestedRoles.isEmpty())
            throw new IllegalArgumentException("No process roles found.");
        if (requestedRoles.size() != requestedRolesIds.size())
            throw new IllegalArgumentException("Not all process roles were found!");

        Set<UserProcessRole> userOldRoles = user.getUserProcessRoles();


        Set<UserProcessRole> rolesAddedToUser = new HashSet<>(requestedRoles);
        rolesAddedToUser.removeAll(userOldRoles);

        Set<UserProcessRole> rolesRemovedFromUser = new HashSet<>(userOldRoles);
        rolesRemovedFromUser.removeAll(requestedRoles);




        user.getUserProcessRoles().clear();
        user.getUserProcessRoles().addAll(requestedRoles);

        userService.save(user);
        publisher.publishEvent(new UserRoleChangeEvent(user, processRoleRepository.findAllBy_idIn(requestedRolesIds)));

        if (Objects.equals(userId, loggedUser.getId())) {
            loggedUser.getProcessRoles().clear();
            loggedUser.parseProcessRoles(user.getUserProcessRoles());
            userDetailsService.reloadSecurityContext(loggedUser);
        }
    }

    @Override
    public List<ProcessRole> findAll(String netId) {
        PetriNet net = netRepository.findOne(netId);
        return new LinkedList<>(net.getRoles().values());
    }
}