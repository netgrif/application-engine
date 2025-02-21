package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;

import java.util.Collection;
import java.util.List;

public interface IRoleService {

    Role save(Role role);
    List<Role> saveAll(Collection<Role> roles);
    void resolveCaseRolesOnCase(Case useCase, AccessPermissions<CasePermission> caseRolePermissions, boolean saveUseCase);
    void resolveCaseRolesOnTask(Case useCase, Task task, AccessPermissions<TaskPermission> caseRolePermissions, boolean saveUseCase);
}
