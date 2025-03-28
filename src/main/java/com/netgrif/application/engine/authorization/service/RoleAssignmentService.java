package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.ApplicationRoleAssignment;
import com.netgrif.application.engine.authorization.domain.CaseRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import com.netgrif.application.engine.authorization.domain.repositories.ApplicationRoleAssignmentRepository;
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
    private final ApplicationRoleAssignmentRepository applicationRoleAssignmentRepository;
    private final ApplicationContext applicationContext;

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
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> findAllByRoleIdIn(Set<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.findAllByRoleIdIn(roleIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> findAllByActorId(String actorId) {
        if (actorId == null) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.findAllByActorId(actorId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Set<String> findAllRoleIdsByActorId(String actorId) {
        if (actorId == null) {
            return new HashSet<>();
        }
        List<RoleAssignmentRepository.RoleOnly> result = (List<RoleAssignmentRepository.RoleOnly>) repository.findAllRoleIdsByActorId(actorId);
        return result.stream().map(RoleAssignmentRepository.RoleOnly::getRoleId).collect(Collectors.toSet());
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean existsByActorAndRole(String actorId, String roleId) {
        return repository.existsByActorIdAndRoleId(actorId, roleId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<ApplicationRoleAssignment> findApplicationAssignmentsByActor(String actorId) {
        return applicationRoleAssignmentRepository.findAllByActor(actorId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> createAssignments(String actorId, List<Role> roles) {
        if (actorId == null || roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        List<RoleAssignment> assignments = doCreateAssignments(actorId, roles);
        return repository.saveAll(assignments);
    }

    /**
     * todo javadoc
     * */
    @Override
    public RoleAssignment createAssignment(String actorId, Role role) {
        return createAssignments(actorId, List.of(role)).stream().findFirst().orElse(null);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> removeAssignments(String actorId, Set<String> roleIds) {
        if (actorId == null || roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.removeAllByActorIdAndRoleIdIn(actorId, roleIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public RoleAssignment removeAssignment(String actorId, String roleId) {
        if (actorId == null || roleId == null) {
            return null;
        }
        return repository.removeByActorIdAndRoleId(actorId, roleId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> removeAssignmentsByActor(String actorId) {
        if (actorId == null) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.removeAllByActorId(actorId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> removeAssignmentsByRole(String roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.removeAllByRoleId(roleId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<RoleAssignment> removeAssignmentsByRoles(Set<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return (List<RoleAssignment>) repository.removeAllByRoleIdIn(roleIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<CaseRoleAssignment> removeAssignmentsByCase(String caseId) {
        if (caseId == null) {
            return new ArrayList<>();
        }
        return (List<CaseRoleAssignment>) caseRoleAssignmentRepository.removeAllByCaseId(caseId);
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
