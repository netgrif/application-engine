package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.AnonymousUser;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.roles.AssignedUserPermission;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class TaskAuthorizationService extends AbstractAuthorizationService implements ITaskAuthorizationService {

    @Autowired
    ITaskService taskService;

    @Override
    public Boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasAtLeastOneRolePermission(loggedUser.transformToUser(), taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(IUser user, Task task, RolePermission... permissions) {
        if (task.getPermissions() == null || task.getPermissions().isEmpty()) {
            return null;
        }

        Map<RolePermission, Boolean> aggregatePermissions = getAggregateRolePermissions(user, task.getPermissions());

        for (RolePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission))) {
                return false;
            }
        }

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission)));
    }

    @Override
    public Boolean userHasUserListPermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasUserListPermission(loggedUser.transformToUser(), taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasUserListPermission(IUser user, Task task, RolePermission... permissions) {
//        TODO: release/8.0.0
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
//        TODO: release/8.0.0
//        if (!isAssigned(task)) {
//            return false;
//        }
//        return task.getUserId().equals(user.getSelfOrImpersonated().getStringId()) || user instanceof AnonymousUser;
        return true;
    }

//    TODO: release/8.0.0
//    private boolean isAssigned(String taskId) {
//        return isAssigned(taskService.findById(taskId));
//    }
//
//    private boolean isAssigned(Task task) {
//        return task.getUserId() != null;
//    }

    @Override
    public boolean canCallAssign(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.ASSIGN);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.ASSIGN);
        return loggedUser.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallDelegate(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.DELEGATE);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.DELEGATE);
        return loggedUser.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallFinish(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
//        TODO: release/8.0.0
//        if (!isAssigned(taskId)) {
//            throw new IllegalTaskStateException("Task with ID '" + taskId + "' cannot be finished, because it is not assigned!");
//        }
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.FINISH);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.FINISH);
        return loggedUser.getSelfOrImpersonated().isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId));
    }

    private boolean canAssignedCancel(IUser user, String taskId) {
//        TODO: release/8.0.0
//        Task task = taskService.findById(taskId);
//        if (!isAssigned(task) || !task.getUserId().equals(user.getSelfOrImpersonated().getStringId())) {
//            return true;
//        }
//        return (task.getAssignedUserPolicy() == null || task.getAssignedUserPolicy().get(AssignedUserPermission.CANCEL) == null) || task.getAssignedUserPolicy().get(AssignedUserPermission.CANCEL);
        return true;
    }

    @Override
    public boolean canCallCancel(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
//        TODO: release/8.0.0
//        if (!isAssigned(taskId)) {
//            throw new IllegalTaskStateException("Task with ID '" + taskId + "' cannot be canceled, because it is not assigned!");
//        }
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.CANCEL);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.CANCEL);
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
