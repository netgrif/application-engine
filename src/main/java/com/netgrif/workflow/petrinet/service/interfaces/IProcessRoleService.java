package com.netgrif.workflow.petrinet.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;

import java.util.List;
import java.util.Set;

public interface IProcessRoleService {
    void assignRolesToUser(Long userId, Set<String> roleIds, LoggedUser user);

    List<ProcessRole> findAll(String netId);

    ProcessRole defaultRole();
}