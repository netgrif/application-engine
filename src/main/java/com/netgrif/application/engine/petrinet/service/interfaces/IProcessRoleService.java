package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;

import java.util.List;
import java.util.Set;

public interface IProcessRoleService {

    List<ProcessRole> saveAll(Iterable<ProcessRole> entities);

    Set<ProcessRole> findAllByImportId(String importId);

    Set<ProcessRole> findAllByDefaultName(String name);

    ProcessRole findById(String id);

    Set<ProcessRole> findByIds(Set<String> ids);

    ProcessRole findByImportId(String importId);

    void assignRolesToUser(String userId, Set<String> roleIds, LoggedUser user);

    List<ProcessRole> findAll();

    List<ProcessRole> findAll(String netId);

    ProcessRole defaultRole();

    ProcessRole anonymousRole();

    void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser);
}