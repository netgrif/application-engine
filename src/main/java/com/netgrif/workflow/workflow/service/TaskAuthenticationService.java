package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.ITaskAuthenticationService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class TaskAuthenticationService implements ITaskAuthenticationService {

	@Autowired
	ITaskService taskService;

	@Override
	public boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
		return userHasAtLeastOneRolePermission(loggedUser.transformToUser(), taskService.findById(taskId), permissions);
	}

	@Override
	public boolean userHasAtLeastOneRolePermission(User user, Task task, RolePermission... permissions) {
		Map<String, Boolean> aggregatePermissions = getAggregatePermissions(user, task);

		for(RolePermission permission : permissions) {
			Boolean hasPermission = aggregatePermissions.get(permission.toString());
			if(hasPermission != null && hasPermission) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isAssignee(LoggedUser loggedUser, String taskId) {
		return isAssignee(loggedUser.transformToUser(), taskService.findById(taskId));
	}

	@Override
	public boolean isAssignee(User user, String taskId) {
		return isAssignee(user, taskService.findById(taskId));
	}

	@Override
	public boolean isAssignee(User user, Task task) {
		if (task.getUserId() == null)
			return false;
		else
			return task.getUserId().equals(user.getId());
	}

	private Map<String, Boolean> getAggregatePermissions(User user, Task task) {
		Map<String, Boolean> aggregatePermissions = new HashMap<>();

		Set<String> userProcessRoleIDs = new LinkedHashSet<>();
		for(ProcessRole role : user.getProcessRoles()) {
			userProcessRoleIDs.add(role.get_id().toString());
		}

		for(Map.Entry<String, Map<String, Boolean>> role : task.getRoles().entrySet()) {
			if(userProcessRoleIDs.contains(role.getKey())) {
				for(Map.Entry<String, Boolean> permission : role.getValue().entrySet()) {
					if(aggregatePermissions.containsKey(permission.getKey())) {
						aggregatePermissions.put(permission.getKey(), aggregatePermissions.get(permission.getKey()) || permission.getValue());
					}
					else {
						aggregatePermissions.put(permission.getKey(), permission.getValue());
					}
				}
			}
		}

		return aggregatePermissions;
	}
}
