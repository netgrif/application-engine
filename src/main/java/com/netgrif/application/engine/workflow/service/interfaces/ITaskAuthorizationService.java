package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.workflow.domain.Task;

public interface ITaskAuthorizationService {
    Boolean userHasAtLeastOneRolePermission(Identity identity, String taskId, TaskPermission... permissions);

    Boolean userHasAtLeastOneRolePermission(IUser user, Task task, TaskPermission... permissions);

    Boolean userHasUserListPermission(Identity identity, String taskId, TaskPermission... permissions);

    Boolean userHasUserListPermission(IUser user, Task task, TaskPermission... permissions);

    boolean isAssignee(Identity identity, String taskId);

    boolean isAssignee(IUser user, String taskId);

    boolean isAssignee(IUser user, Task task);

    boolean canCallAssign(Identity identity, String taskId);

    boolean canCallDelegate(Identity identity, String taskId);

    boolean canCallFinish(Identity identity, String taskId) throws IllegalTaskStateException;

    boolean canCallCancel(Identity identity, String taskId) throws IllegalTaskStateException;

    boolean canCallSaveData(Identity identity, String taskId);

    boolean canCallSaveFile(Identity identity, String taskId);

}
