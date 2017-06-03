package com.fmworkflow.petrinet.service;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.domain.UserProcessRole;
import com.fmworkflow.auth.domain.repositories.UserProcessRoleRepository;
import com.fmworkflow.auth.domain.repositories.UserRepository;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.repositories.PetriNetRepository;
import com.fmworkflow.petrinet.domain.roles.ProcessRole;
import com.fmworkflow.petrinet.service.interfaces.IProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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