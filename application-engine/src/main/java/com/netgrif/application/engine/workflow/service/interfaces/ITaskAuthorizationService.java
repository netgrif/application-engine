package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.objects.workflow.domain.Task;

public interface ITaskAuthorizationService {
    Boolean userHasAtLeastOneRolePermission(AbstractUser loggedUser, String taskId, RolePermission... permissions);

    Boolean userHasAtLeastOneRolePermission(AbstractUser user, Task task, RolePermission... permissions);

    Boolean userHasUserListPermission(AbstractUser loggedUser, String taskId, RolePermission... permissions);

    Boolean userHasUserListPermission(AbstractUser user, Task task, RolePermission... permissions);

    boolean isAssignee(AbstractUser user, String taskId);

    boolean isAssignee(AbstractUser user, Task task);

    boolean canCallAssign(AbstractUser loggedUser, String taskId);

    boolean canCallDelegate(AbstractUser loggedUser, String taskId);

    boolean canCallFinish(AbstractUser loggedUser, String taskId) throws IllegalTaskStateException;

    boolean canCallCancel(AbstractUser loggedUser, String taskId) throws IllegalTaskStateException;

    boolean canCallSaveData(AbstractUser loggedUser, String taskId);

    boolean canCallSaveFile(AbstractUser loggedUser, String taskId);

}
