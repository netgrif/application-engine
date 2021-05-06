package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;

import java.util.Collection;
import java.util.List;

public interface IUserProcessRoleService {

    List<UserProcessRole> findAll();

    UserProcessRole findDefault();

    List<UserProcessRole> saveRoles(Collection<ProcessRole> values, String netId);

    UserProcessRole findByRoleId(String roleId);

    void deleteRolesOfNet(PetriNet net);
}