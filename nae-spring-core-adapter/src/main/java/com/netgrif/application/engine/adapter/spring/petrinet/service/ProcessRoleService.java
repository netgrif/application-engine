package com.netgrif.application.engine.adapter.spring.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface ProcessRoleService {
    ProcessRole save(ProcessRole processRole);
    List<ProcessRole> saveAll(Iterable<ProcessRole> processRoles);
    Page<ProcessRole> getAll(Pageable pageable);
    Page<ProcessRole> findAllByNetId(String netId, Pageable pageable);
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
    Page<ProcessRole> findAllByDefaultName(String name, Pageable pageable);
    Page<ProcessRole> findAllByImportId(String importId, Pageable pageable);
    ProcessRole findById(String id);
    Set<ProcessRole> findByIds(Set<String> ids);
    ProcessRole findByImportId(String importId);
    Page<ProcessRole> findAll(Pageable pageable);
    Page<ProcessRole> findAllGlobalRoles(Pageable pageable);
    List<ProcessRole> findAll(String netId);
    ProcessRole defaultRole();
    ProcessRole anonymousRole();
    void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser);
    void clearCache();
}
