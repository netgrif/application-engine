package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskAuthorizationService extends AbstractAuthorizationService implements ITaskAuthorizationService {

    @Autowired
    private ITaskService taskService;

    @Override
    public Boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasAtLeastOneRolePermission(loggedUser, taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(AbstractUser user, Task task, RolePermission... permissions) {
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
    public Boolean userHasUserListPermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasUserListPermission(loggedUser, taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasUserListPermission(AbstractUser user, Task task, RolePermission... permissions) {
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
    public boolean isAssignee(LoggedUser loggedUser, String taskId) {
        return isAssignee(loggedUser, taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(AbstractUser user, String taskId) {
        return isAssignee(user, taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(AbstractUser user, Task task) {
        if (!isAssigned(task))
            return false;
        else
            // TODO: impersonation user.getSelfOrImpersonated().getStringId()
            return task.getUserId().equals(user.getStringId())
                    || (user.getAttributeValue("anonymous") != null && (Boolean) user.getAttributeValue("anonymous"));
    }

    private boolean isAssigned(String taskId) {
        return isAssigned(taskService.findById(taskId));
    }

    private boolean isAssigned(Task task) {
        return task.getUserId() != null;
    }

    @Override
    public boolean canCallAssign(LoggedUser loggedUser, String taskId) {
        // TODO: impersonation loggedUser.getSelfOrImpersonated().isAdmin()
        if (loggedUser.isAdmin()) {
            return true;
        }

        Task task = taskService.findById(taskId);
        // TODO: impersonation
        Boolean userPerm = userHasUserListPermission(loggedUser, task, RolePermission.ASSIGN);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, task, RolePermission.ASSIGN);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallDelegate(LoggedUser loggedUser, String taskId) {
        // TODO: impersonation loggedUser.getSelfOrImpersonated().isAdmin()
        if (loggedUser.isAdmin()) {
            return true;
        }

        Task task = taskService.findById(taskId);
        // TODO: impersonation
        Boolean userPerm = userHasUserListPermission(loggedUser, task, RolePermission.DELEGATE);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, task, RolePermission.DELEGATE);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallFinish(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId)) {
            throw new IllegalTaskStateException("Task with ID '%s' cannot be finished, because it is not assigned!".formatted(taskId));
        }
        // TODO: impersonation
        if (loggedUser.isAdmin()) {
            return true;
        }

        Task task = taskService.findById(taskId);
        // TODO: impersonation
        if (!isAssignee(loggedUser, task)) {
            return false;
        }
        // TODO: impersonation
        Boolean userPerm = userHasUserListPermission(loggedUser, task, RolePermission.FINISH);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, task, RolePermission.FINISH);
        return rolePerm != null && rolePerm;
    }

    /**
     * To return true, the task should not have set up the assigned user policy for cancel to "false"
     * */
    private boolean canAssignedCancel(Task task) {
        return task.getAssignedUserPolicy() == null || task.getAssignedUserPolicy().get("cancel") == null
                || task.getAssignedUserPolicy().get("cancel");
    }

    @Override
    public boolean canCallCancel(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId)) {
            throw new IllegalTaskStateException("Task with ID '%s' cannot be canceled, because it is not assigned!".formatted(taskId));
        }
        // TODO: impersonation
        if (loggedUser.isAdmin()) {
            return true;
        }

        Task task = taskService.findById(taskId);
        // TODO: impersonation
        if (!isAssignee(loggedUser, task) || !canAssignedCancel(task)) {
            return false;
        }

        // TODO: impersonation
        Boolean userPerm = userHasUserListPermission(loggedUser, task, RolePermission.CANCEL);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, task, RolePermission.CANCEL);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallSaveData(LoggedUser loggedUser, String taskId) {
        // TODO: impersonation loggedUser.getSelfOrImpersonated().isAdmin()
        return loggedUser.isAdmin() || isAssignee(loggedUser, taskId);
    }

    @Override
    public boolean canCallSaveFile(LoggedUser loggedUser, String taskId) {
        // TODO: impersonation loggedUser.getSelfOrImpersonated().isAdmin()
        return loggedUser.isAdmin() || isAssignee(loggedUser, taskId);
    }

    private Map<String, Boolean> findUserPermissions(Task task, AbstractUser user) {
        return findUserPermissions(task.getActors(), user);
    }
}
