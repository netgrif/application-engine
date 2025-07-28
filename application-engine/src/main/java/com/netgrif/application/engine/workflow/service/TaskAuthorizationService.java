package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.objects.workflow.domain.Task;
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

    @Autowired
    UserService userService;

    @Override
    public Boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasAtLeastOneRolePermission(userService.transformToUser((LoggedUserImpl) loggedUser), taskService.findById(taskId), permissions);
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

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission.toString())));
    }

    @Override
    public Boolean userHasUserListPermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasUserListPermission(userService.transformToUser((LoggedUserImpl) loggedUser), taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasUserListPermission(AbstractUser user, Task task, RolePermission... permissions) {
        if (task.getUserRefs() == null || task.getUserRefs().isEmpty())
            return null;

        // TODO: impersonation
//        if (!task.getUsers().containsKey(user.getSelfOrImpersonated().getStringId())) {
        if (!task.getUsers().containsKey(user.getStringId())) {
            return null;
        }

        // TODO: impersonation
//        Map<String, Boolean> userPermissions = task.getUsers().get(user.getSelfOrImpersonated().getStringId());
        Map<String, Boolean> userPermissions = task.getUsers().get(user.getStringId());

        for (RolePermission permission : permissions) {
            Boolean perm = userPermissions.get(permission.toString());
            if (hasRestrictedPermission(perm)) {
                return false;
            }
        }
        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(userPermissions.get(permission.toString())));
    }

    @Override
    public boolean isAssignee(LoggedUser loggedUser, String taskId) {
        return isAssignee(userService.transformToUser((LoggedUserImpl) loggedUser), taskService.findById(taskId));
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
            // TODO: impersonation
//            return task.getUserId().equals(user.getSelfOrImpersonated().getStringId()) || (Boolean) user.getAttributeValue("anonymous");
            return task.getUserId().equals(user.getStringId()) || (Boolean) user.getAttributeValue("anonymous");
    }

    private boolean isAssigned(String taskId) {
        return isAssigned(taskService.findById(taskId));
    }

    private boolean isAssigned(Task task) {
        return task.getUserId() != null;
    }

    @Override
    public boolean canCallAssign(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.ASSIGN);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.ASSIGN);
        // TODO: impersonation
//        return loggedUser.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
        return loggedUser.isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallDelegate(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.DELEGATE);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.DELEGATE);
        // TODO: impersonation
//        return loggedUser.getSelfOrImpersonated().isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
        return loggedUser.isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallFinish(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId))
            throw new IllegalTaskStateException("Task with ID '" + taskId + "' cannot be finished, because it is not assigned!");

        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.FINISH);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.FINISH);
        // TODO: impersonation
//        return loggedUser.getSelfOrImpersonated().isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId));
        return loggedUser.isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId));
    }

    private boolean canAssignedCancel(AbstractUser user, String taskId) {
        Task task = taskService.findById(taskId);
        // TODO: impersonation
//        if (!isAssigned(task) || !task.getUserId().equals(user.getSelfOrImpersonated().getStringId())) {
        if (!isAssigned(task) || !task.getUserId().equals(user.getStringId())) {
            return true;
        }
        return (task.getAssignedUserPolicy() == null || task.getAssignedUserPolicy().get("cancel") == null) || task.getAssignedUserPolicy().get("cancel");
    }

    @Override
    public boolean canCallCancel(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId))
            throw new IllegalTaskStateException("Task with ID '" + taskId + "' cannot be canceled, because it is not assigned!");

        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.CANCEL);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.CANCEL);
        // TODO: impersonation
//        return loggedUser.getSelfOrImpersonated().isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId)) && canAssignedCancel(userService.transformToUser((LoggedUserImpl) loggedUser), taskId);
        return loggedUser.isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId)) && canAssignedCancel(userService.transformToUser((LoggedUserImpl) loggedUser), taskId);
    }

    @Override
    public boolean canCallSaveData(LoggedUser loggedUser, String taskId) {
        // TODO: impersonation
//        return loggedUser.getSelfOrImpersonated().isAdmin() || isAssignee(loggedUser, taskId);
        return loggedUser.isAdmin() || isAssignee(loggedUser, taskId);
    }

    @Override
    public boolean canCallSaveFile(LoggedUser loggedUser, String taskId) {
        // TODO: impersonation
//        return loggedUser.getSelfOrImpersonated().isAdmin() || isAssignee(loggedUser, taskId);
        return loggedUser.isAdmin() || isAssignee(loggedUser, taskId);
    }
}
