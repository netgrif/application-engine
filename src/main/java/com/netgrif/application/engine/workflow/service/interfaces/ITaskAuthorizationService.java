package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.workflow.domain.Task;

public interface ITaskAuthorizationService {
    Boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions);

    Boolean userHasAtLeastOneRolePermission(IUser user, Task task, RolePermission... permissions);

    Boolean userHasUserListPermission(LoggedUser loggedUser, String taskId, RolePermission... permissions);

    Boolean userHasUserListPermission(IUser user, Task task, RolePermission... permissions);

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
