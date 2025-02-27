package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRoleService {
    List<Role> findAll();
    List<Role> findAllById(Set<String> roleIds);
    Role findDefaultRole();
    Role findAnonymousRole();
    Set<ProcessRole> findProcessRolesByDefaultTitle(String title);
    boolean existsProcessRoleByImportId(String importId);
    ProcessRole findProcessRoleByImportId(String importId);

    Role save(Role role);
    List<Role> saveAll(Collection<Role> roles);
    void remove(Role role);
    void removeAll(Collection<Role> roles);
    void removeAllByCase(String caseId);

    void resolveCaseRolesOnCase(Case useCase, AccessPermissions<CasePermission> caseRolePermissions, boolean saveUseCase);
    void resolveCaseRolesOnTask(Case useCase, Task task, AccessPermissions<TaskPermission> caseRolePermissions, boolean saveUseCase);

    List<Role> assignRolesToUser(String userId, Set<String> roleIds);
    List<Role> assignRolesToUser(String userId, Set<String> roleIds, Map<String, String> params);
    List<Role> removeRolesFromUser(String userId, Set<String> roleIds);
    List<Role> removeRolesFromUser(String userId, Set<String> roleIds, Map<String, String> params);
}
