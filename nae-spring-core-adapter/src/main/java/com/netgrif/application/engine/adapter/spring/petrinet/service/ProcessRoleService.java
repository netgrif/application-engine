package com.netgrif.application.engine.adapter.spring.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.PredefinedProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;

import java.util.*;

public interface ProcessRoleService {
    ProcessRole save(ProcessRole processRole);
    List<ProcessRole> saveAll(Iterable<ProcessRole> processRoles);
    List<ProcessRole> findAllByNetId(String netId);
    Optional<ProcessRole> get(ProcessResourceId id);
    void delete(String id);
    void deleteAll(Collection<String> ids);
    void deleteAll();
    void assignRolesToUser(IUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser);
    void assignRolesToUser(IUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser, Map<String, String> params);
    void assignRolesToGroup(Group group, Set<ProcessResourceId> requestedRolesIds);
    void assignNegativeRolesToUser(IUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser);
    void assignNegativeRolesToUser(IUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser, Map<String, String> params);
    void assignNegativeRolesToGroup(Group group, Set<ProcessResourceId> requestedRolesIds);
    ProcessRole getDefaultRole();
    ProcessRole getAnonymousRole();
    Collection<ProcessRole> findAllByIds(Collection<ProcessResourceId> roleIds);
    ProcessRole findById(ProcessResourceId id);
    Collection<ProcessRole> findAllByDefaultName(String name);
    Set<ProcessRole> findAllByImportId(String importId);
    ProcessRole findById(String id);
    Set<ProcessRole> findByIds(Set<String> ids);
    ProcessRole findByImportId(String importId);
    List<ProcessRole> findAll();
    List<ProcessRole> findAllByWorkspaceId(String workspaceId);
    Set<ProcessRole> findAllGlobalRoles();
    List<ProcessRole> findAll(String netId);
    ProcessRole defaultRole();
    ProcessRole anonymousRole();
    ProcessRole createDefaultOrAnonymousRole(PredefinedProcessRole role, String workspaceId);
    void deleteDefaultOrAnonymousRole(PredefinedProcessRole role, String workspaceId);
    void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser);
    void clearCache();
}
