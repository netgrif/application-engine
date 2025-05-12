package com.netgrif.application.engine.adapter.spring.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface ProcessRoleService {
    ProcessRole save(ProcessRole processRole);
    List<ProcessRole> saveAll(Iterable<ProcessRole> processRoles);
    void delete(String id);
    void deleteAll(Collection<String> ids);
    void deleteAll();
    void assignRolesToUser(AbstractUser user, Collection<ProcessResourceId> roleIds, LoggedUser loggedUser);
    void assignRolesToUser(AbstractUser user, Collection<ProcessResourceId> roleIds, LoggedUser loggedUser, Map<String, String> params);
    void assignRolesToGroup(Group group, Collection<ProcessResourceId> requestedRolesIds);
    ProcessRole getDefaultRole();
    ProcessRole getAnonymousRole();

    List<ProcessRole> findAll(Pageable pageable);
    List<ProcessRole> findAllByNetStringId(String netStringId);
    List<ProcessRole> findAllByNetIdentifier(String identifier);
    Collection<ProcessRole> findAllByIds(Collection<ProcessResourceId> roleIds);
    ProcessRole findById(ProcessResourceId id);
    Collection<ProcessRole> findAllByDefaultName(String name);
    List<ProcessRole> findAllByImportId(String importId);
    ProcessRole findById(String id);
    List<ProcessRole> findByIds(Collection<String> ids);
    List<ProcessRole> findAllGlobalRoles();

    ProcessRole defaultRole();
    ProcessRole anonymousRole();
    void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser);
    void clearCache();
}
