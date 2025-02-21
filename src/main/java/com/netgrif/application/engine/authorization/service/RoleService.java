package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.CaseRole;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.authorization.domain.repositories.RoleRepository;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final RoleRepository repository;
    private final WorkflowService workflowService;

    /**
     * todo javadoc
     * */
    @Override
    public Role save(Role role) {
        return repository.save(role);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<Role> saveAll(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.saveAll(roles);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void resolveCaseRolesOnCase(Case useCase, AccessPermissions<CasePermission> caseRolePermissions,
                                       boolean saveUseCase) {
        useCase.addPermissionsForRoles(createRolesAndBuildPermissions(useCase, caseRolePermissions, saveUseCase));
    }

    /**
     * todo javadoc
     * */
    @Override
    public void resolveCaseRolesOnTask(Case useCase, Task task, AccessPermissions<TaskPermission> caseRolePermissions,
                                       boolean saveUseCase) {
        task.addPermissionsForRoles(createRolesAndBuildPermissions(useCase, caseRolePermissions, saveUseCase));
    }

    /**
     * todo javadoc
     * */
    private <T> AccessPermissions<T> createRolesAndBuildPermissions(Case useCase, AccessPermissions<T> userRefPermissions,
                                                                    boolean saveUseCase) {
        List<Role> rolesToSave = new ArrayList<>();
        AccessPermissions<T> resultPermissions = new AccessPermissions<>();

        userRefPermissions.forEach((userListId, permissions) -> {
            CaseRole caseRole = new CaseRole(userListId, useCase.getStringId());
            Field<?> userListField = useCase.getDataSet().getFields().get(userListId);
            if (userListField != null) {
                ((UserListField) userListField).setRoleId(caseRole.getStringId());
            } else {
                throw new IllegalStateException(String.format("Case role [%s} in process [%s] references non existing dataField in case [%s]",
                        userListId, useCase.getPetriNetId(), useCase.getStringId()));
            }
        });

        if (!userRefPermissions.isEmpty() && saveUseCase) {
            workflowService.save(useCase);
        }
        saveAll(rolesToSave);
        return resultPermissions;
    }
}
