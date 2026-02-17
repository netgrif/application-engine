package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class TaskAuthorizationService extends AbstractAuthorizationService implements ITaskAuthorizationService {

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IWorkflowService workflowService;

    @Override
    public Boolean userHasAtLeastOneRolePermission(LoggedUser user, Task task, RolePermission... permissions) {
        if (task.getRoles() == null || task.getRoles().isEmpty())
            return null;

        Map<String, Boolean> aggregatePermissions = getAggregatePermissions(user, task.getRoles());

        for (RolePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission.toString()))) {
                return false;
            }
        }

        return Arrays.stream(permissions)
                .anyMatch(permission -> hasPermission(aggregatePermissions.get(permission.toString())));
    }

    @Override
    public Boolean userHasUserListPermission(LoggedUser user, Task task, RolePermission... permissions) {
        if (task.getActorRefs() == null || task.getActorRefs().isEmpty()) {
            return null;
        }

        Map<String, Boolean> userPermissions = findUserPermissions(task, user);
        if (userPermissions == null) {
            return null;
        }

        for (RolePermission permission : permissions) {
            Boolean perm = userPermissions.get(permission.toString());
            if (hasRestrictedPermission(perm)) {
                return false;
            }
        }
        return Arrays.stream(permissions)
                .anyMatch(permission -> hasPermission(userPermissions.get(permission.toString())));
    }

    @Override
    public boolean isAssignee(LoggedUser user, Task task) {
        if (!isAssigned(task))
            return false;
        else {
            Case caze = workflowService.findOne(task.getCaseId());
            return user.hasProcessAccess(caze.getProcessIdentifier())
                    && (task.getUserId().equals(user.getSelfOrImpersonatedStringId()) || (Boolean) user.getAttributeValue("anonymous") || task.getImpersonatorUserId().equals(user.getStringId()));
        }
    }

    private boolean isAssigned(Task task) {
        return task.getUserId() != null;
    }

    @Override
    public boolean canCallAssign(LoggedUser loggedUser, String taskId) {
        if (loggedUser.isAdmin()) {
            return true;
        }
        Task currentTask = taskService.findById(taskId);
        Case caze = workflowService.findOne(currentTask.getCaseId());
        boolean processPerm = loggedUser.hasProcessAccess(caze.getProcessIdentifier());
        if (!processPerm) {
            return false;
        }

        Boolean userPerm = userHasUserListPermission(loggedUser, currentTask, RolePermission.ASSIGN);
        if (userPerm != null) {
            return userPerm;
        }

        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, currentTask, RolePermission.ASSIGN);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallDelegate(LoggedUser loggedUser, String taskId) {
        if (loggedUser.isAdmin()) {
            return true;
        }
        Task currentTask = taskService.findById(taskId);
        Case caze = workflowService.findOne(currentTask.getCaseId());
        boolean processPerm = loggedUser.hasProcessAccess(caze.getProcessIdentifier());
        if (!processPerm) {
            return false;
        }
        Boolean userPerm = userHasUserListPermission(loggedUser, currentTask, RolePermission.DELEGATE);
        if (userPerm != null) {
            return userPerm;
        }

        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, currentTask, RolePermission.DELEGATE);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallFinish(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        Task currentTask = taskService.findById(taskId);
        if (!isAssigned(currentTask)) {
            throw new IllegalTaskStateException("Task with ID '" + taskId + "' cannot be finished, because it is not assigned!");
        }
        if (loggedUser.isAdmin()) {
            return true;
        }
        if (!isAssignee(loggedUser, currentTask)) {
            return false;
        }

        Boolean userPerm = userHasUserListPermission(loggedUser, currentTask, RolePermission.FINISH);
        if (userPerm != null) {
            return userPerm;
        }

        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, currentTask, RolePermission.FINISH);
        return rolePerm != null && rolePerm;
    }

    /**
     * To return true, the task should not have set up the assigned user policy for cancel to "false"
     *
     */
    private boolean canAssignedCancel(Task task) {
        return task.getAssignedUserPolicy() == null || task.getAssignedUserPolicy().get("cancel") == null
                || task.getAssignedUserPolicy().get("cancel");
    }


    @Override
    public boolean canCallCancel(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        Task currentTask = taskService.findById(taskId);
        if (!isAssigned(currentTask)) {
            throw new IllegalTaskStateException("Task with ID '%s' cannot be canceled, because it is not assigned!".formatted(taskId));
        }
        if (loggedUser.isAdmin()) {
            return true;
        }
        if (!isAssignee(loggedUser, currentTask) || !canAssignedCancel(currentTask)) {
            return false;
        }

        Boolean userPerm = userHasUserListPermission(loggedUser, currentTask, RolePermission.CANCEL);
        if (userPerm != null) {
            return userPerm;
        }

        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, currentTask, RolePermission.CANCEL);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallSaveData(LoggedUser loggedUser, String taskId) {
        Task currentTask = taskService.findById(taskId);
        return loggedUser.isAdmin() || isAssignee(loggedUser, currentTask);
    }

    @Override
    public boolean canCallSaveFile(LoggedUser loggedUser, String taskId) {
        Task currentTask = taskService.findById(taskId);
        return loggedUser.isAdmin() || isAssignee(loggedUser, currentTask);
    }

    private Map<String, Boolean> findUserPermissions(Task task, LoggedUser user) {
        return findUserPermissions(task.getActors(), user.getSelfOrImpersonated());
    }
}
