package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.ITaskAuthorizationService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskAuthorizationService extends AbstractAuthorizationService implements ITaskAuthorizationService {

    @Autowired
    ITaskService taskService;

    @Override
    public boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasAtLeastOneRolePermission(loggedUser.transformToUser(), taskService.findById(taskId), permissions);
    }

    @Override
    public boolean userHasAtLeastOneRolePermission(User user, Task task, RolePermission... permissions) {
        Map<String, Boolean> aggregatePermissions = getAggregatePermissions(user, task.getRoles());

        for (RolePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission.toString()))) {
                return false;
            }
        }

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission.toString())));
    }

    @Override
    public boolean isAssignee(LoggedUser loggedUser, String taskId) {
        return isAssignee(loggedUser.transformToUser(), taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(User user, String taskId) {
        return isAssignee(user, taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(User user, Task task) {
        if (!isAssigned(task))
            return false;
        else
            return task.getUserId().equals(user.getId());
    }

    private boolean isAssigned(String taskId) {
        return isAssigned(taskService.findById(taskId));
    }

    private boolean isAssigned(Task task) {
        return task.getUserId() != null;
    }

    @Override
    public boolean canCallAssign(LoggedUser loggedUser, String taskId) {
        return loggedUser.isAdmin() || userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.PERFORM);
    }

    @Override
    public boolean canCallDelegate(LoggedUser loggedUser, String taskId) {
        return loggedUser.isAdmin() || userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.PERFORM, RolePermission.DELEGATE);
    }

    @Override
    public boolean canCallFinish(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId))
            throw new IllegalTaskStateException("Task with ID '"+taskId+"' cannot be finished, because it is not assigned!");

        return loggedUser.isAdmin()
                || (userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.PERFORM)
                    && isAssignee(loggedUser, taskId));
    }

    private boolean canAssignedCancel(User user, String taskId) {
        Task task = taskService.findById(taskId);
        if (!isAssigned(task) || !task.getUserId().equals(user.getId())) {
            return true;
        }
        return (task.getAssignedUserPolicy() == null || task.getAssignedUserPolicy().get("cancel") == null) || task.getAssignedUserPolicy().get("cancel");
    }

    @Override
    public boolean canCallCancel(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId))
            throw new IllegalTaskStateException("Task with ID '"+taskId+"' cannot be canceled, because it is not assigned!");

        return loggedUser.isAdmin()
                || (userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.PERFORM, RolePermission.CANCEL)
                    && isAssignee(loggedUser, taskId) && canAssignedCancel(loggedUser.transformToUser(), taskId));
    }

    @Override
    public boolean canCallSaveData(LoggedUser loggedUser, String taskId) {
        return loggedUser.isAdmin() || isAssignee(loggedUser, taskId);
    }

    @Override
    public boolean canCallSaveFile(LoggedUser loggedUser, String taskId) {
        return loggedUser.isAdmin() || isAssignee(loggedUser, taskId);
    }
}
