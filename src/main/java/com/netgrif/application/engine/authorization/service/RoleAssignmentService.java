package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.CaseRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import com.netgrif.application.engine.authorization.domain.repositories.CaseRoleAssignmentRepository;
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository;
import com.netgrif.application.engine.authorization.service.factory.RoleAssignmentFactory;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAssignmentService implements IRoleAssignmentService {

    private final RoleAssignmentRepository repository;
    private final CaseRoleAssignmentRepository caseRoleAssignmentRepository;
    private final ApplicationContext applicationContext;

    @Override
    public List<RoleAssignment> findAllByUserIdAndRoleIdIn(String userId, Set<String> roleIds) {
        return (List<RoleAssignment>) repository.findAllByUserIdAndRoleIdIn(userId, roleIds);
    }

    @Override
    public List<RoleAssignment> findAllByRoleIdIn(Set<String> roleIds) {
        return (List<RoleAssignment>) repository.findAllById(roleIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> createAssignments(String userId, List<Role> roles) {
        List<RoleAssignment> assignments = doCreateAssignments(userId, roles);
        return repository.saveAll(assignments);
    }

    /**
     * todo javadoc
     * */
    @Override
    public RoleAssignment createAssignment(String userId, Role role) {
        RoleAssignment assignment = doCreateAssignment(userId, role);
        return repository.save(assignment);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> removeAssignments(String userId, Set<String> roleIds) {
        return (List<RoleAssignment>) repository.removeAllByUserIdAndRoleIdIn(userId, roleIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public RoleAssignment removeAssignment(String userId, String roleId) {
        return repository.removeByUserIdAndRoleId(userId, roleId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> removeAssignmentsByUser(String userId) {
        return (List<RoleAssignment>) repository.removeAllByUserId(userId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> removeAssignmentsByRole(String roleId) {
        return (List<RoleAssignment>) repository.removeAllByRoleId(roleId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> removeAssignmentsByRoles(Set<String> roleIds) {
        return (List<RoleAssignment>) repository.removeAllByRoleIdIn(roleIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<CaseRoleAssignment> removeAssignmentsByCase(String caseId) {
        return (List<CaseRoleAssignment>) caseRoleAssignmentRepository.removeAllByCaseId(caseId);
    }

    private List<RoleAssignment> doCreateAssignments(String userId, List<Role> roles) {
        return roles.stream().map(role -> createAssignment(userId, role)).collect(Collectors.toList());
    }

    private RoleAssignment doCreateAssignment(String userId, Role role) {
        RoleAssignmentFactory factory = getAssignmentFactoryBean(role);
        return factory.createAssignment(role, userId);
    }

    private RoleAssignmentFactory getAssignmentFactoryBean(Role role) {
        return (RoleAssignmentFactory) applicationContext.getBean(role.getAssignmentFactoryClass());
    }
}
