package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.roles.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRoleService {

    List<Role> saveAll(Iterable<Role> entities);

    Set<Role> findAllByImportId(String importId);

    Set<Role> findAllByDefaultName(String name);

    Role findById(String id);

    Set<Role> findByIds(Set<String> ids);

    Role findByImportId(String importId);

    boolean existsByImportId(String importId);

    void assignRolesToUser(String userId, Set<String> roleIds, LoggedUser user);

    void assignRolesToUser(String userId, Set<String> roleIds, LoggedUser user, Map<String, String> params);

    List<Role> findAll();

    List<Role> findAll(String netId);

    Role defaultRole();

    Role anonymousRole();

    void deleteRolesOfNet(Process net, LoggedUser loggedUser);
}