package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.roles.ProcessRole;

import java.util.List;

public interface IProcessRoleService {
    void assignRoleToUser(String email, String netId, List<String> roleIds);

    List<ProcessRole> findAll(String netId);
}