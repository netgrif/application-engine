package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskAuthorizationService extends AbstractAuthorizationService implements ITaskAuthorizationService {

    private final ITaskService taskService;

    // todo javadoc everywhere

    @Override
    public Boolean userHasAtLeastOneRolePermission(Identity identity, String taskId, TaskPermission... permissions) {
        return userHasAtLeastOneRolePermission(identity.transformToUser(), taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(IUser user, Task task, TaskPermission... permissions) {
        if (task.hasPermissions()) {
            return null;
        }

        Map<TaskPermission, Boolean> aggregatePermissions = getAggregateRolePermissions(user, task.getProcessRolePermissions());

        for (TaskPermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission))) {
                return false;
            }
        }

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission)));
    }

    @Override
    public Boolean userHasUserListPermission(Identity identity, String taskId, TaskPermission... permissions) {
        return userHasUserListPermission(identity.transformToUser(), taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasUserListPermission(IUser user, Task task, TaskPermission... permissions) {
//        TODO 2058
//        if (task.getUserRefs() == null || task.getUserRefs().isEmpty()) {
//            return null;
//        }
//        if (!task.getUsers().containsKey(user.getSelfOrImpersonated().getStringId())) {
//            return null;
//        }
//
//        Map<RolePermission, Boolean> userPermissions = task.getUsers().get(user.getSelfOrImpersonated().getStringId());
//
//        for (RolePermission permission : permissions) {
//            Boolean perm = userPermissions.get(permission);
//            if (hasRestrictedPermission(perm)) {
//                return false;
//            }
//        }
//        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(userPermissions.get(permission)));
        return true;
    }

    @Override
    public boolean isAssignee(Identity identity, String taskId) {
        if (identity.isAnonymous()) {
            return isAssignee(identity.transformToAnonymousUser(), taskService.findById(taskId));
        }
        return isAssignee(identity.transformToUser(), taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(IUser user, String taskId) {
        return isAssignee(user, taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(IUser user, Task task) {
        if (isNotAssigned(task)) {
            return false;
        }
        return task.getAssigneeId().equals(user.getSelfOrImpersonated().getStringId()) || user instanceof AnonymousUser;
    }

    private boolean isNotAssigned(String taskId) {
        return isNotAssigned(taskService.findById(taskId));
    }

    private boolean isNotAssigned(Task task) {
        return task.getAssigneeId() == null;
    }

    @Override
    public boolean canCallAssign(Identity identity, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(identity, taskId, TaskPermission.ASSIGN);
        Boolean userPerm = userHasUserListPermission(identity, taskId, TaskPermission.ASSIGN);
        return identity.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    // TODO: release/8.0.0 delegate doesnt exist anymore
    @Override
    public boolean canCallDelegate(Identity identity, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(identity, taskId, TaskPermission.REASSIGN);
        Boolean userPerm = userHasUserListPermission(identity, taskId, TaskPermission.REASSIGN);
        return identity.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallFinish(Identity identity, String taskId) throws IllegalTaskStateException {
        if (isNotAssigned(taskId)) {
            throw new IllegalTaskStateException(String.format("Task with ID '%s' cannot be finished, because it is not assigned!", taskId));
        }
        Boolean rolePerm = userHasAtLeastOneRolePermission(identity, taskId, TaskPermission.FINISH);
        Boolean userPerm = userHasUserListPermission(identity, taskId, TaskPermission.FINISH);
        return identity.getSelfOrImpersonated().isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(identity, taskId));
    }

    private boolean canAssignedCancel(IUser user, String taskId) {
//        TODO 2058
//        Task task = taskService.findById(taskId);
//        if (!isAssigned(task) || !task.getUserId().equals(user.getSelfOrImpersonated().getStringId())) {
//            return true;
//        }
//        return (task.getAssignedUserPolicy() == null || task.getAssignedUserPolicy().get(AssignedUserPermission.CANCEL) == null) || task.getAssignedUserPolicy().get(AssignedUserPermission.CANCEL);
        return true;
    }

    @Override
    public boolean canCallCancel(Identity identity, String taskId) throws IllegalTaskStateException {
        if (isNotAssigned(taskId)) {
            throw new IllegalTaskStateException(String.format("Task with ID '%s' cannot be canceled, because it is not assigned!", taskId));
        }
        Boolean rolePerm = userHasAtLeastOneRolePermission(identity, taskId, TaskPermission.CANCEL);
        Boolean userPerm = userHasUserListPermission(identity, taskId, TaskPermission.CANCEL);
        return identity.getSelfOrImpersonated().isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(identity, taskId)) && canAssignedCancel(identity.transformToUser(), taskId);
    }

    @Override
    public boolean canCallSaveData(Identity identity, String taskId) {
        return identity.getSelfOrImpersonated().isAdmin() || isAssignee(identity, taskId);
    }

    @Override
    public boolean canCallSaveFile(Identity identity, String taskId) {
        return identity.getSelfOrImpersonated().isAdmin() || isAssignee(identity, taskId);
    }
}
