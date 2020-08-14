package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.workflow.domain.Task;

public interface ITaskAuthenticationService {
	boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions);

	boolean userHasAtLeastOneRolePermission(User user, Task task, RolePermission... permissions);

	boolean isAssignee(LoggedUser loggedUser, String taskId);

	boolean isAssignee(User user, String taskId);

	boolean isAssignee(User user, Task task);

	boolean canCallAssign(LoggedUser loggedUser, String taskId);

	boolean canCallDelegate(LoggedUser loggedUser, String taskId);

	boolean canCallFinish(LoggedUser loggedUser, String taskId);

	boolean canCallCancel(LoggedUser loggedUser, String taskId);

	boolean canCallSaveData(LoggedUser loggedUser, String taskId);

	boolean canCallSaveFile(LoggedUser loggedUser, String taskId);

}
