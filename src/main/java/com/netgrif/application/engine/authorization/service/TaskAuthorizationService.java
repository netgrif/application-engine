package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.authorization.service.interfaces.*;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import com.netgrif.application.engine.workflow.domain.State;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskAuthorizationService extends AuthorizationService implements ITaskAuthorizationService {

    private final ITaskService taskService;

    public TaskAuthorizationService(ISessionManagerService sessionManagerService, IRoleAssignmentService roleAssignmentService,
                                    ApplicationRoleRunner applicationRoleRunner, ITaskService taskService) {
        super(sessionManagerService, applicationRoleRunner, roleAssignmentService);
        this.taskService = taskService;
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallAssign(String taskId) {
        if (taskId == null) {
            return false;
        }

        Task task = taskService.findById(taskId);
        return !isAssigned(task)
                && canCallEvent(task.getProcessRolePermissions(), task.getCaseRolePermissions(), TaskPermission.ASSIGN);
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallCancel(String taskId) {
        if (taskId == null) {
            return false;
        }

        Task task = taskService.findById(taskId);
        return isAssigned(task) && isAssignee(task)
                && canCallEvent(task.getProcessRolePermissions(), task.getCaseRolePermissions(), TaskPermission.CANCEL);
    }

    @Override
    public boolean canCallReassign(String taskId) {
        if (taskId == null) {
            return false;
        }

        Task task = taskService.findById(taskId);
        return isAssigned(task)
                && canCallEvent(task.getProcessRolePermissions(), task.getCaseRolePermissions(), TaskPermission.REASSIGN);
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallFinish(String taskId) {
        if (taskId == null) {
            return false;
        }

        Task task = taskService.findById(taskId);
        return isAssigned(task) && isAssignee(task)
                && canCallEvent(task.getProcessRolePermissions(), task.getCaseRolePermissions(), TaskPermission.FINISH);
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallSetData(String taskId) {
        return taskId != null && (isAssignee(taskId) || isAdmin());
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallGetData(String taskId) {
        if (taskId == null) {
            return false;
        }

        return canCallGetData(taskService.findById(taskId));
    }

    @Override
    public boolean canCallGetData(Task task) {
        // todo 2058 unit test
        if (task == null) {
            return false;
        }

        TaskPermission permission = task.getState().equals(State.ENABLED) ? TaskPermission.VIEW : TaskPermission.VIEW_DISABLED;

        return canCallEvent(task.getProcessRolePermissions(), task.getCaseRolePermissions(), permission);
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean canCallSaveFile(String taskId) {
        return taskId != null && isAssignee(taskId);
    }

    private boolean isAssignee(String taskId) {
        LoggedIdentity loggedIdentity = sessionManagerService.getLoggedIdentity();
        if (loggedIdentity == null || loggedIdentity.getActiveActorId() == null) {
            return false;
        }

        return taskService.existsByTaskAndAssignee(taskId, loggedIdentity.getActiveActorId());
    }

    private boolean isAssignee(Task task) {
        LoggedIdentity loggedIdentity = sessionManagerService.getLoggedIdentity();
        if (loggedIdentity == null || loggedIdentity.getActiveActorId() == null) {
            return false;
        }

        return task.getAssigneeId().equals(loggedIdentity.getActiveActorId());
    }

    private boolean isAssigned(Task task) {
        return task.getAssigneeId() != null;
    }
}
