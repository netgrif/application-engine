package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.workflow.workflow.domain.Task;

public interface ITaskAuthorizationService {
    boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions);

    boolean userHasAtLeastOneRolePermission(IUser user, Task task, RolePermission... permissions);

    boolean userHasUserListPermission(LoggedUser loggedUser, String taskId, RolePermission... permissions);

    boolean userHasUserListPermission(IUser user, Task task, RolePermission... permissions);

    boolean isAssignee(LoggedUser loggedUser, String taskId);

    boolean isAssignee(IUser user, String taskId);

    boolean isAssignee(IUser user, Task task);

    boolean canCallAssign(LoggedUser loggedUser, String taskId);

    boolean canCallDelegate(LoggedUser loggedUser, String taskId);

    boolean canCallFinish(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException;

    boolean canCallCancel(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException;

    boolean canCallSaveData(LoggedUser loggedUser, String taskId);

    boolean canCallSaveFile(LoggedUser loggedUser, String taskId);

}
