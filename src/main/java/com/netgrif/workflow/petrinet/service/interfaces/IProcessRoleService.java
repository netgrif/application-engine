package com.netgrif.workflow.petrinet.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;

import java.util.List;
import java.util.Set;

public interface IProcessRoleService {

    List<ProcessRole> saveAll(Iterable<ProcessRole> entities);

    ProcessRole findById(String id);

    Set<ProcessRole> findByIds(Set<String> ids);

    ProcessRole findByImportId(String importId);

    void assignRolesToUser(String userId, Set<String> roleIds, LoggedUser user);

    List<ProcessRole> findAll();

    List<ProcessRole> findAll(String netId);

    ProcessRole defaultRole();

    void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser);
}