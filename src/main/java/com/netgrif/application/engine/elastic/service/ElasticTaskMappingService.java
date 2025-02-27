package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.elastic.ElasticMappingUtils;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.workflow.domain.State;
import com.netgrif.application.engine.workflow.domain.Task;
import org.springframework.stereotype.Service;

@Service
public class ElasticTaskMappingService implements IElasticTaskMappingService {

    @Override
    public ElasticTask transform(Task task) {
        ElasticTask transformedTask = new ElasticTask(task);

        TaskPermission permission = task.getState() == State.ENABLED ? TaskPermission.VIEW : TaskPermission.VIEW_DISABLED;
        this.populatePermissions(transformedTask, task, permission);

        return transformedTask;
    }

    protected void populatePermissions(ElasticTask transformedTask, Task task, TaskPermission permission) {
        transformedTask.setViewProcessRoles(ElasticMappingUtils.filterRoleIdsByPermissionType(
                task.getProcessRolePermissions(), permission));
        transformedTask.setPositiveViewProcessRoles(ElasticMappingUtils.filterRoleIdsByPermissionValue(
                task.getProcessRolePermissions(), permission, true));
        transformedTask.setNegativeViewProcessRoles(ElasticMappingUtils.filterRoleIdsByPermissionValue(
                task.getProcessRolePermissions(), permission, false));

        transformedTask.setViewCaseRoles(ElasticMappingUtils.filterRoleIdsByPermissionType(
                task.getCaseRolePermissions(), permission));
        transformedTask.setPositiveViewCaseRoles(ElasticMappingUtils.filterRoleIdsByPermissionValue(
                task.getCaseRolePermissions(), permission, true));
        transformedTask.setNegativeViewCaseRoles(ElasticMappingUtils.filterRoleIdsByPermissionValue(
                task.getCaseRolePermissions(), permission, false));
    }
}
