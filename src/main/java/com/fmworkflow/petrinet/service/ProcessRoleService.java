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

@Service
public class ProcessRoleService implements IProcessRoleService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProcessRoleRepository roleRepository;
    @Autowired
    private PetriNetRepository netRepository;

    @Override
    public void assignRoleToUser(String email, String netId, List<String> roleIds) {
        User user = userRepository.findByEmail(email);
        PetriNet net = netRepository.findOne(netId);

        user.setUserProcessRoles(new HashSet<>());
        roleIds.forEach(roleId -> {
            UserProcessRole role = new UserProcessRole();
            role.setRoleId(net.getRoles().get(roleId).getStringId());
            role = roleRepository.save(role);
            user.addProcessRole(role);
        });

        userRepository.save(user);
    }

    @Override
    public List<ProcessRole> findAll(String netId) {
        PetriNet net = netRepository.findOne(netId);
        return new LinkedList<>(net.getRoles().values());
    }
}