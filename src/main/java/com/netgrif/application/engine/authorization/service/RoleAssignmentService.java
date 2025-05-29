package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.*;
import com.netgrif.application.engine.authorization.domain.repositories.ApplicationRoleAssignmentRepository;
import com.netgrif.application.engine.authorization.domain.repositories.CaseRoleAssignmentRepository;
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository;
import com.netgrif.application.engine.authorization.service.factory.RoleAssignmentFactory;
import com.netgrif.application.engine.authorization.service.interfaces.IAllActorService;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAssignmentService implements IRoleAssignmentService {

    private final RoleAssignmentRepository repository;
    private final CaseRoleAssignmentRepository caseRoleAssignmentRepository;
    private final ApplicationRoleAssignmentRepository applicationRoleAssignmentRepository;
    private final ApplicationContext applicationContext;
    private IAllActorService allActorService;
    private IGroupService groupService;

    @Lazy
    @Autowired
    public void setAllActorService(IAllActorService allActorService) {
        this.allActorService = allActorService;
    }

    @Lazy
    @Autowired
    public void setGroupService(IGroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> findAllByActorIdAndRoleIdIn(String actorId, Set<String> roleIds) {
        if (actorId == null || roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.findAllByActorIdAndRoleIdIn(actorId, roleIds);
    }

    /**
     * Finds all role assignments for given set of role IDs
     * @param roleIds Set of role IDs to find assignments for
     * @return List of matching role assignments, empty list if no matches found or invalid input
     */
    @Override
    public List<RoleAssignment> findAllByRoleIdIn(Set<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.findAllByRoleIdIn(roleIds);
    }

    /**
     * Finds all role assignments for given actor ID
     * @param actorId ID of the actor to find assignments for
     * @return List of all role assignments for the actor, empty list if no matches found or invalid input
     */
    @Override
    public List<RoleAssignment> findAllByActorId(String actorId) {
        if (actorId == null) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.findAllByActorId(actorId);
    }

    /**
     * Finds all role IDs assigned to given actor
     * @param actorId ID of the actor to find role IDs for
     * @return Set of role IDs assigned to the actor, empty set if no matches found or invalid input
     */
    @Override
    public Set<String> findAllRoleIdsByActorId(String actorId) {
        if (actorId == null) {
            return new HashSet<>();
        }
        List<RoleAssignment> result = (List<RoleAssignment>) repository.findAllByActorId(actorId);
        return result.stream().map(RoleAssignment::getRoleId).collect(Collectors.toSet());
    }

    @Override
    public Set<String> findAllRoleIdsByActorAndGroups(String actorId) {
        if (actorId == null) {
            return new HashSet<>();
        }
        Optional<Actor> actorOpt = allActorService.findById(actorId);
        if (actorOpt.isEmpty()) {
            throw new IllegalStateException(String.format("Actor with id [%s] doesn't exist.", actorId));
        }

        Set<String> roleIds = findAllRoleIdsByActorId(actorId);
        roleIds.addAll(findRoleIdsByGroups(actorOpt.get().getGroupIds()));

        return roleIds;
    }

    /**
     * Checks if a role assignment exists for given actor and role
     * @param actorId ID of the actor to check
     * @param roleId ID of the role to check
     * @return true if assignment exists, false otherwise
     */
    @Override
    public boolean existsByActorAndRole(String actorId, String roleId) {
        return repository.existsByActorIdAndRoleId(actorId, roleId);
    }

    /**
     * Finds all application role assignments for given actor
     * @param actorId ID of the actor to find application assignments for
     * @return List of application role assignments for the actor
     */
    @Override
    public List<ApplicationRoleAssignment> findApplicationAssignmentsByActor(String actorId) {
        return applicationRoleAssignmentRepository.findAllByActor(actorId);
    }

    /**
     * Creates new role assignments for given actor and roles
     * @param actorId ID of the actor to create assignments for
     * @param roles List of roles to assign
     * @return List of created role assignments, empty list if invalid input
     */
    @Override
    public List<RoleAssignment> createAssignments(String actorId, List<Role> roles) {
        if (actorId == null || roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        List<RoleAssignment> assignments = doCreateAssignments(actorId, roles);
        return repository.saveAll(assignments);
    }

    /**
     * Creates new role assignment for given actor and role
     * @param actorId ID of the actor to create assignment for
     * @param role Role to assign
     * @return Created role assignment or null if creation failed
     */
    @Override
    public RoleAssignment createAssignment(String actorId, Role role) {
        return createAssignments(actorId, List.of(role)).stream().findFirst().orElse(null);
    }

    /**
     * Removes multiple role assignments for given actor and role IDs
     * @param actorId ID of the actor to remove assignments from
     * @param roleIds Set of role IDs to remove
     * @return List of removed role assignments, empty list if no matches found or invalid input
     */
    @Override
    public List<RoleAssignment> removeAssignments(String actorId, Set<String> roleIds) {
        if (actorId == null || roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.removeAllByActorIdAndRoleIdIn(actorId, roleIds);
    }

    /**
     * Removes single role assignment for given actor and role ID
     * @param actorId ID of the actor to remove assignment from
     * @param roleId ID of the role to remove
     * @return Removed role assignment or null if not found or invalid input
     */
    @Override
    public RoleAssignment removeAssignment(String actorId, String roleId) {
        if (actorId == null || roleId == null) {
            return null;
        }
        return repository.removeByActorIdAndRoleId(actorId, roleId);
    }

    /**
     * Removes all role assignments for given actor
     * @param actorId ID of the actor to remove assignments for
     * @return List of removed role assignments, empty list if no matches found or invalid input
     */
    @Override
    public List<RoleAssignment> removeAssignmentsByActor(String actorId) {
        if (actorId == null) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.removeAllByActorId(actorId);
    }

    /**
     * Removes all role assignments for given role
     * @param roleId ID of the role to remove assignments for
     * @return List of removed role assignments, empty list if no matches found or invalid input
     */
    @Override
    public List<RoleAssignment> removeAssignmentsByRole(String roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.removeAllByRoleId(roleId);
    }

    /**
     * Removes all role assignments for given set of roles
     * @param roleIds Set of role IDs to remove assignments for
     * @return List of removed role assignments, empty list if no matches found or invalid input
     */
    @Override
    public List<RoleAssignment> removeAssignmentsByRoles(Set<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.removeAllByRoleIdIn(roleIds);
    }

    /**
     * Removes all case role assignments for given case
     * @param caseId ID of the case to remove assignments for
     * @return List of removed case role assignments, empty list if no matches found or invalid input
     */
    @Override
    public List<CaseRoleAssignment> removeAssignmentsByCase(String caseId) {
        if (caseId == null) {
            return new ArrayList<>();
        }
        return (List<CaseRoleAssignment>) caseRoleAssignmentRepository.removeAllByCaseId(caseId);
    }

    private Set<String> findRoleIdsByGroups(List<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> alreadyProcessedGroupIds = new HashSet<>();
        Set<String> roleIds = new HashSet<>();
        for (String groupId : groupIds) {
            roleIds.addAll(findRoleIdsByGroupRecursive(groupId, alreadyProcessedGroupIds));
            alreadyProcessedGroupIds.add(groupId);
        }
        return roleIds;
    }

    private Set<String> findRoleIdsByGroupRecursive(String groupId, Set<String> alreadyProcessedGroupIds) {
        if (alreadyProcessedGroupIds.contains(groupId)) {
            return new HashSet<>();
        }
        Optional<Group> groupOpt = groupService.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new IllegalStateException(String.format("Group with id [%s] doesn't exist.", groupId));
        }

        Set<String> roleIds = findAllRoleIdsByActorId(groupId);
        alreadyProcessedGroupIds.add(groupId);
        if (groupOpt.get().getParentGroupId() != null) {
            roleIds.addAll(findRoleIdsByGroupRecursive(groupOpt.get().getParentGroupId(), alreadyProcessedGroupIds));
        }

        return roleIds;
    }

    private List<RoleAssignment> doCreateAssignments(String actorId, List<Role> roles) {
        return roles.stream().map(role -> {
            RoleAssignmentFactory factory = getAssignmentFactoryBean(role);
            return factory.createAssignment(role, actorId);
        }).collect(Collectors.toList());
    }

    private RoleAssignmentFactory getAssignmentFactoryBean(Role role) {
        return (RoleAssignmentFactory) applicationContext.getBean(role.getAssignmentFactoryClass());
    }
}
