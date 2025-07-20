package com.netgrif.application.engine.adapter.spring.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
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

    void assignRolesToUser(AbstractUser user, Collection<ProcessResourceId> roleIds, LoggedUser loggedUser);

    void assignRolesToUser(AbstractUser user, Collection<ProcessResourceId> roleIds, LoggedUser loggedUser, Map<String, String> params);

    void assignRolesToGroup(Group group, Collection<ProcessResourceId> requestedRolesIds);

    ProcessRole getDefaultRole();

    ProcessRole getAnonymousRole();

    List<ProcessRole> findAllByNetStringId(String netStringId);

    Page<ProcessRole> findAllByNetIdentifier(String identifier, Pageable pageable);

    Collection<ProcessRole> findAllByIds(Collection<ProcessResourceId> roleIds);

    ProcessRole findById(ProcessResourceId id);

    Page<ProcessRole> findAllByDefaultName(String name, Pageable pageable);

    Page<ProcessRole> findAllByImportId(String importId, Pageable pageable);

    ProcessRole findById(String id);

    Set<ProcessRole> findByIds(Collection<String> ids);

    ProcessRole findByImportId(String importId);

    Page<ProcessRole> findAll(Pageable pageable);

    Page<ProcessRole> findAllGlobalRoles(Pageable pageable);

    ProcessRole defaultRole();

    ProcessRole anonymousRole();

    void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser);

    void clearCache();
}
