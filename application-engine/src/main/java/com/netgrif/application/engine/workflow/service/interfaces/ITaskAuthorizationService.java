package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.objects.workflow.domain.Task;

public interface ITaskAuthorizationService {
    Boolean userHasAtLeastOneRolePermission(AbstractUser user, String taskId, RolePermission... permissions);

    Boolean userHasAtLeastOneRolePermission(AbstractUser user, Task task, RolePermission... permissions);

    Boolean userHasUserListPermission(AbstractUser user, String taskId, RolePermission... permissions);

    Boolean userHasUserListPermission(AbstractUser user, Task task, RolePermission... permissions);

    boolean isAssignee(AbstractUser user, String taskId);

    boolean isAssignee(AbstractUser user, Task task);

    boolean canCallAssign(AbstractUser user, String taskId);

    boolean canCallDelegate(AbstractUser user, String taskId);

    boolean canCallFinish(AbstractUser user, String taskId) throws IllegalTaskStateException;

    boolean canCallCancel(AbstractUser user, String taskId) throws IllegalTaskStateException;

    boolean canCallSaveData(AbstractUser user, String taskId);

    boolean canCallSaveFile(AbstractUser user, String taskId);

}
