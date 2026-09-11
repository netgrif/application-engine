package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskAuthorizationService extends AbstractAuthorizationService implements ITaskAuthorizationService {

    private final ITaskService taskService;

    @Override
    public Boolean userHasAtLeastOneRolePermission(AbstractUser user, String taskId, RolePermission... permissions) {
        return userHasAtLeastOneRolePermission(user, taskService.findById(taskId), permissions);
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

        return checkPermissions(aggregatePermissions, Arrays.stream(permissions).map(RolePermission::toString).toList());
    }

    @Override
    public Boolean userHasUserListPermission(AbstractUser user, String taskId, RolePermission... permissions) {
        return userHasUserListPermission(user, taskService.findById(taskId), permissions);
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

        return checkPermissions(userPermissions, Arrays.stream(permissions).map(RolePermission::toString).toList());
    }

    @Override
    public boolean isAssignee(AbstractUser user, String taskId) {
        return isAssignee(user, taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(AbstractUser user, Task task) {
        if (!isAssigned(task)) {
            return false;
        } else {
            return task.getUserId().equals(user.getStringId())
                    || (task.getUser().isAnonymous() && user.isAnonymous());
        }
    }

    private boolean isAssigned(String taskId) {
        return isAssigned(taskService.findById(taskId));
    }

    private boolean isAssigned(Task task) {
        return task.getUserId() != null;
    }

    @Override
    public boolean canCallAssign(AbstractUser user, String taskId) {
        // TODO: impersonation user.getSelfOrImpersonated().isAdmin()
        if (user.isAdmin()) {
            return true;
        }

        Task task = taskService.findById(taskId);
        // TODO: impersonation
        Boolean userPerm = userHasUserListPermission(user, task, RolePermission.ASSIGN);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation
        Boolean rolePerm = userHasAtLeastOneRolePermission(user, task, RolePermission.ASSIGN);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallDelegate(AbstractUser user, String taskId) {
        // TODO: impersonation user.getSelfOrImpersonated().isAdmin()
        if (user.isAdmin()) {
            return true;
        }

        Task task = taskService.findById(taskId);
        // TODO: impersonation
        Boolean userPerm = userHasUserListPermission(user, task, RolePermission.DELEGATE);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation
        Boolean rolePerm = userHasAtLeastOneRolePermission(user, task, RolePermission.DELEGATE);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallFinish(AbstractUser user, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId)) {
            throw new IllegalTaskStateException("Task with ID '%s' cannot be finished, because it is not assigned!".formatted(taskId));
        }
        // TODO: impersonation
        if (user.isAdmin()) {
            return true;
        }

        Task task = taskService.findById(taskId);
        // TODO: impersonation
        if (!isAssignee(user, task)) {
            return false;
        }
        // TODO: impersonation
        Boolean userPerm = userHasUserListPermission(user, task, RolePermission.FINISH);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation
        Boolean rolePerm = userHasAtLeastOneRolePermission(user, task, RolePermission.FINISH);
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
    public boolean canCallCancel(AbstractUser user, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId)) {
            throw new IllegalTaskStateException("Task with ID '%s' cannot be canceled, because it is not assigned!".formatted(taskId));
        }
        // TODO: impersonation
        if (user.isAdmin()) {
            return true;
        }

        Task task = taskService.findById(taskId);
        // TODO: impersonation
        if (!isAssignee(user, task) || !canAssignedCancel(task)) {
            return false;
        }

        // TODO: impersonation
        Boolean userPerm = userHasUserListPermission(user, task, RolePermission.CANCEL);
        if (userPerm != null) {
            return userPerm;
        }

        // TODO: impersonation
        Boolean rolePerm = userHasAtLeastOneRolePermission(user, task, RolePermission.CANCEL);
        return rolePerm != null && rolePerm;
    }

    @Override
    public boolean canCallSaveData(AbstractUser user, String taskId) {
        // TODO: impersonation user.getSelfOrImpersonated().isAdmin()
        return user.isAdmin() || isAssignee(user, taskId);
    }

    @Override
    public boolean canCallSaveFile(AbstractUser user, String taskId) {
        // TODO: impersonation user.getSelfOrImpersonated().isAdmin()
        return user.isAdmin() || isAssignee(user, taskId);
    }

    private Map<String, Boolean> findUserPermissions(Task task, AbstractUser user) {
        return findUserPermissions(task.getActors(), user);
    }
}
