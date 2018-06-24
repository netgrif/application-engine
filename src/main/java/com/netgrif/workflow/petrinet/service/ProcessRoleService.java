package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.UserDetailsServiceImpl;
import com.netgrif.workflow.event.events.user.UserRoleChangeEvent;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ProcessRoleService implements IProcessRoleService {

    @Autowired
    private UserRepository userRepository;

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
    public void assignRolesToUser(Long userId, Set<String> roleIds, LoggedUser loggedUser) {
        User user = userRepository.findOne(userId);
        List<UserProcessRole> processRoles = roleRepository.findByRoleIdIn(roleIds);
        if (processRoles.isEmpty())
            throw new IllegalArgumentException("No process roles found.");
        if (processRoles.size() != roleIds.size())
            throw new IllegalArgumentException("Not all process roles were found!");

        user.getUserProcessRoles().clear();
        user.getUserProcessRoles().addAll(processRoles);

        userRepository.save(user);
        publisher.publishEvent(new UserRoleChangeEvent(user, processRoleRepository.findAllBy_idIn(roleIds)));

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