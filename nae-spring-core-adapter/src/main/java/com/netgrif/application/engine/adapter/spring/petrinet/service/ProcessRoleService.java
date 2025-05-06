package com.netgrif.application.engine.adapter.spring.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ProcessRoleService {
    ProcessRole save(ProcessRole processRole);
    Set<ProcessRole> saveAll(Iterable<ProcessRole> processRoles);
    void delete(String id);
    void deleteAll(Collection<String> ids);
    void deleteAll();
    void assignRolesToUser(AbstractUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser);
    void assignRolesToUser(AbstractUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser, Map<String, String> params);
    void assignRolesToGroup(Group group, Set<ProcessResourceId> requestedRolesIds);
    void assignNegativeRolesToUser(AbstractUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser);
    void assignNegativeRolesToUser(AbstractUser user, Set<ProcessResourceId> roleIds, LoggedUser loggedUser, Map<String, String> params);
    void assignNegativeRolesToGroup(Group group, Set<ProcessResourceId> requestedRolesIds);
    ProcessRole getDefaultRole();
    ProcessRole getAnonymousRole();

    Set<ProcessRole> findAll(Pageable pageable);
    Set<ProcessRole> findAllByNetStringId(String netStringId);
    Set<ProcessRole> findAllByNetIdentifier(String identifier);
    Collection<ProcessRole> findAllByIds(Collection<ProcessResourceId> roleIds);
    ProcessRole findById(ProcessResourceId id);
    Collection<ProcessRole> findAllByDefaultName(String name);
    Set<ProcessRole> findAllByImportId(String importId);
    ProcessRole findById(String id);
    Set<ProcessRole> findByIds(Set<String> ids);
    Set<ProcessRole> findAllGlobalRoles();

    ProcessRole defaultRole();
    ProcessRole anonymousRole();
    void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser);
    void clearCache();
}
