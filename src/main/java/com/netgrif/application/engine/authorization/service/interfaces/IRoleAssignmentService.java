package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.CaseRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;

import java.util.List;
import java.util.Set;

public interface IRoleAssignmentService {

    List<RoleAssignment> findAllByActorIdAndRoleIdIn(String actorId, Set<String> roleIds);
    List<RoleAssignment> findAllByRoleIdIn(Set<String> roleIds);
    List<RoleAssignment> findAllByActorId(String actorId);

    List<RoleAssignment> createAssignments(String actorId, List<Role> roles);
    RoleAssignment createAssignment(String actorId, Role role);
    List<RoleAssignment> removeAssignments(String actorId, Set<String> roleIds);
    RoleAssignment removeAssignment(String actorId, String roleId);
    List<RoleAssignment> removeAssignmentsByActor(String actorId);
    List<RoleAssignment> removeAssignmentsByRole(String roleId);
    List<RoleAssignment> removeAssignmentsByRoles(Set<String> roleIds);
    List<CaseRoleAssignment> removeAssignmentsByCase(String caseId);
}
