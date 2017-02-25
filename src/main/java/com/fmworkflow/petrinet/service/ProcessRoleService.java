package com.fmworkflow.petrinet.service;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.domain.UserRepository;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.PetriNetRepository;
import com.fmworkflow.petrinet.domain.roles.ProcessRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class ProcessRoleService implements IProcessRoleService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PetriNetRepository netRepository;

    @Override
    public void assignRoleToUser(String userId, String netId, String roleId) {
        User user = userRepository.findOne(Long.valueOf(userId));
        PetriNet net = netRepository.findOne(netId);
        user.addProcessRole(net.getRoles().get(roleId));
        userRepository.save(user);
    }

    @Override
    public List<ProcessRole> findAll(String netId) {
        PetriNet net = netRepository.findOne(netId);
        return new LinkedList<>(net.getRoles().values());
    }
}