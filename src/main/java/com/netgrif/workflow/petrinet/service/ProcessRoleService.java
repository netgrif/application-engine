package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class ProcessRoleService implements IProcessRoleService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProcessRoleRepository roleRepository;
    @Autowired
    private PetriNetRepository netRepository;

    @Override
    public boolean assignRolesToUser(Long userId, Set<String> roleIds){
        User user = userRepository.findOne(userId);
        List<UserProcessRole> processRoles = roleRepository.findByRoleIdIn(roleIds);
        if(processRoles.isEmpty()) return false;

        user.getUserProcessRoles().clear();
        user.getUserProcessRoles().addAll(processRoles);

        return userRepository.save(user) != null;
    }

    @Override
    public List<ProcessRole> findAll(String netId) {
        PetriNet net = netRepository.findOne(netId);
        return new LinkedList<>(net.getRoles().values());
    }
}