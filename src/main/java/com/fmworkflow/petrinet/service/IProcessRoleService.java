package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.roles.ProcessRole;

import java.util.List;

public interface IProcessRoleService {
    void assignRoleToUser(String userId, String netId, String roleId);

    List<ProcessRole> findAll(String netId);
}