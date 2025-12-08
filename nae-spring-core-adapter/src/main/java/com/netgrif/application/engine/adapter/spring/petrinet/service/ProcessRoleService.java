package com.netgrif.application.engine.adapter.spring.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProcessRoleService {
    ProcessRole save(ProcessRole processRole);
    List<ProcessRole> saveAll(Collection<ProcessRole> processRoles);
    Page<ProcessRole> getAll(Pageable pageable);
    Optional<ProcessRole> get(ProcessResourceId id);
    void delete(String id);
    void deleteAll(Collection<String> ids);
    void deleteAll();
    void assignRolesToUser(User user, Collection<ProcessResourceId> roleIds, LoggedUser loggedUser);
    void assignRolesToUser(User user, Collection<ProcessResourceId> roleIds, LoggedUser loggedUser, Map<String, String> params);
    void assignRolesToGroup(Group group, Collection<ProcessResourceId> requestedRolesIds);
    ProcessRole getDefaultRole();
    ProcessRole getAnonymousRole();

    Page<ProcessRole> findAll(Pageable pageable);
    List<ProcessRole> findAllByNetStringId(String netStringId);
    List<ProcessRole> findAllByIds(Collection<ProcessResourceId> roleIds);
    ProcessRole findById(ProcessResourceId id);
    Page<ProcessRole> findAllByDefaultName(String name, Pageable pageable);
    Page<ProcessRole> findAllByImportId(String importId, Pageable pageable);
    ProcessRole findByImportId(String importId);
    ProcessRole findById(String id);
    List<ProcessRole> findByIds(Collection<String> ids);
    Page<ProcessRole> findAllGlobalRoles(Pageable pageable);
    void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser);
    void clearCache();
    void deleteGlobalRole(String roleId, LoggedUser loggedUser);
}
