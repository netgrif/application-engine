package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.authentication.domain.AnonymousUser;
import com.netgrif.application.engine.authentication.domain.IUser;
import com.netgrif.application.engine.authentication.domain.LoggedUser;
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
    public Boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, TaskPermission... permissions) {
        return userHasAtLeastOneRolePermission(loggedUser.transformToUser(), taskService.findById(taskId), permissions);
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
    public Boolean userHasUserListPermission(LoggedUser loggedUser, String taskId, TaskPermission... permissions) {
        return userHasUserListPermission(loggedUser.transformToUser(), taskService.findById(taskId), permissions);
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
    public boolean isAssignee(LoggedUser loggedUser, String taskId) {
        if (loggedUser.isAnonymous()) {
            return isAssignee(loggedUser.transformToAnonymousUser(), taskService.findById(taskId));
        }
        return isAssignee(loggedUser.transformToUser(), taskService.findById(taskId));
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
    public boolean canCallAssign(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, TaskPermission.ASSIGN);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, TaskPermission.ASSIGN);
        return loggedUser.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    // TODO: release/8.0.0 delegate doesnt exist anymore
    @Override
    public boolean canCallDelegate(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, TaskPermission.REASSIGN);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, TaskPermission.REASSIGN);
        return loggedUser.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallFinish(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (isNotAssigned(taskId)) {
            throw new IllegalTaskStateException(String.format("Task with ID '%s' cannot be finished, because it is not assigned!", taskId));
        }
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, TaskPermission.FINISH);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, TaskPermission.FINISH);
        return loggedUser.getSelfOrImpersonated().isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId));
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
    public boolean canCallCancel(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (isNotAssigned(taskId)) {
            throw new IllegalTaskStateException(String.format("Task with ID '%s' cannot be canceled, because it is not assigned!", taskId));
        }
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, TaskPermission.CANCEL);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, TaskPermission.CANCEL);
        return loggedUser.getSelfOrImpersonated().isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId)) && canAssignedCancel(loggedUser.transformToUser(), taskId);
    }

    @Override
    public boolean canCallSaveData(LoggedUser loggedUser, String taskId) {
        return loggedUser.getSelfOrImpersonated().isAdmin() || isAssignee(loggedUser, taskId);
    }

    @Override
    public boolean canCallSaveFile(LoggedUser loggedUser, String taskId) {
        return loggedUser.getSelfOrImpersonated().isAdmin() || isAssignee(loggedUser, taskId);
    }
}
