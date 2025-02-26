package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.CaseRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;

import java.util.List;
import java.util.Set;

public interface IRoleAssignmentService {

    List<RoleAssignment> findAllByUserIdAndRoleIdIn(String userId, Set<String> roleIds);
    List<RoleAssignment> findAllByRoleIdIn(Set<String> roleIds);

    List<RoleAssignment> createAssignments(String userId, List<Role> roles);
    RoleAssignment createAssignment(String userId, Role role);
    List<RoleAssignment> removeAssignments(String userId, Set<String> roleIds);
    RoleAssignment removeAssignment(String userId, String roleId);
    List<RoleAssignment> removeAssignmentsByUser(String userId);
    List<RoleAssignment> removeAssignmentsByRole(String roleId);
    List<RoleAssignment> removeAssignmentsByRoles(Set<String> roleIds);
    List<CaseRoleAssignment> removeAssignmentsByCase(String caseId);
}
