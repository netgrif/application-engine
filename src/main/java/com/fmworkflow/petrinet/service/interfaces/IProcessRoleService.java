package com.fmworkflow.petrinet.service.interfaces;

import com.fmworkflow.petrinet.domain.roles.ProcessRole;

import java.util.List;
import java.util.Set;

public interface IProcessRoleService {
    boolean assignRolesToUser(Long userId, Set<String> roleIds);

    List<ProcessRole> findAll(String netId);
}