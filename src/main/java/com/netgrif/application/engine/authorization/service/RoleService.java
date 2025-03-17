package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.domain.IUser;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.domain.CaseRole;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;
import com.netgrif.application.engine.authorization.domain.repositories.CaseRoleRepository;
import com.netgrif.application.engine.authorization.domain.repositories.ProcessRoleRepository;
import com.netgrif.application.engine.authorization.domain.repositories.RoleRepository;
import com.netgrif.application.engine.authorization.domain.throwable.NotAllRolesAssignedException;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.event.events.user.UserAssignRoleEvent;
import com.netgrif.application.engine.event.events.user.UserRemoveRoleEvent;
import com.netgrif.application.engine.importer.model.EventPhaseType;
import com.netgrif.application.engine.importer.model.RoleEventType;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldWithAllowedRoles;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.events.RoleEvent;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final RoleRepository repository;
    private final ProcessRoleRepository processRoleRepository;
    private final CaseRoleRepository caseRoleRepository;
    private final IRoleAssignmentService roleAssignmentService;
    private final IUserService userService;
    private final ActionRunner actionRunner;
    private final ApplicationEventPublisher eventPublisher;
    private IWorkflowService workflowService;

    private ProcessRole defaultProcessRole;
    private ProcessRole anonymousProcessRole;

    @Lazy
    @Autowired
    public void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<Role> findAll() {
        return repository.findAll();
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<Role> findAllById(Set<String> roleIds) {
        return (List<Role>) repository.findAllById(roleIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Role findDefaultRole() {
        if (defaultProcessRole == null) {
            defaultProcessRole = findSystemRoleByImportId(ProcessRole.DEFAULT_ROLE);
        }
        return defaultProcessRole;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Role findAnonymousRole() {
        if (anonymousProcessRole == null) {
            anonymousProcessRole = findSystemRoleByImportId(ProcessRole.ANONYMOUS_ROLE);
        }
        return anonymousProcessRole;
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<ProcessRole> findAllProcessRoles() {
        return processRoleRepository.findAll();
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<ProcessRole> findAllProcessRolesByImportIds(Set<String> roleImportIds) {
        return processRoleRepository.findAllByImportIdIn(roleImportIds);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<ProcessRole> findProcessRolesByDefaultTitle(String title) {
        return processRoleRepository.findAllByTitle_DefaultValue(title);
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean existsProcessRoleByImportId(String importId) {
        return processRoleRepository.existsByImportId(importId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public ProcessRole findProcessRoleByImportId(String importId) {
        return processRoleRepository.findByImportId(importId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<CaseRole> findAllCaseRoles() {
        return caseRoleRepository.findAll();
    }

    /**
     * todo javadoc
     * */
    @Override
    public Role save(Role role) {
        return repository.save(role);
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<Role> saveAll(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.saveAll(roles);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void remove(Role role) {
        roleAssignmentService.removeAssignmentsByRole(role.getStringId());
        repository.delete(role);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void removeAll(Collection<Role> roles) {
        Set<String> roleIds = roles.stream().map(Role::getStringId).collect(Collectors.toSet());
        roleAssignmentService.removeAssignmentsByRoles(roleIds);
        repository.deleteAll(roles);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void removeAllByCase(String caseId) {
        roleAssignmentService.removeAssignmentsByCase(caseId);
        caseRoleRepository.removeAllByCaseId(caseId);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void resolveCaseRolesOnCase(Case useCase, AccessPermissions<CasePermission> caseRolePermissions,
                                       boolean saveUseCase) {
        useCase.addCaseRolePermissions(createRolesAndBuildPermissions(useCase, caseRolePermissions, saveUseCase));
    }

    /**
     * todo javadoc
     * */
    @Override
    public void resolveCaseRolesOnTask(Case useCase, Task task, AccessPermissions<TaskPermission> caseRolePermissions,
                                       boolean saveUseCase) {
        task.addCaseRolePermissions(createRolesAndBuildPermissions(useCase, caseRolePermissions, saveUseCase));
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<Role> assignRolesToUser(String userId, Set<String> roleIds) {
        return assignRolesToUser(userId, roleIds, new HashMap<>());
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<Role> assignRolesToUser(String userId, Set<String> roleIds, Map<String, String> params) {
        IUser user = userService.findById(userId);

        List<Role> roles = findAllById(roleIds);
        if (roles.isEmpty() && !roleIds.isEmpty()) {
            throw new IllegalArgumentException("No roles found.");
        }
        if (roles.size() != roleIds.size()) {
            throw new IllegalArgumentException("Not all roles were found!");
        }

        roles = filterNotAssignedRoles(user.getStringId(), roles);

        runAllSuitableActionsOnRoles(roles, RoleEventType.ASSIGN, EventPhaseType.PRE, params);
        List<RoleAssignment> newRoleAssignments = roleAssignmentService.createAssignments(userId, roles);
        if (roles.size() > newRoleAssignments.size()) {
            throw new NotAllRolesAssignedException(roles.size() - newRoleAssignments.size());
        }
        eventPublisher.publishEvent(new UserAssignRoleEvent(user, roles));
        runAllSuitableActionsOnRoles(roles, RoleEventType.ASSIGN, EventPhaseType.POST, params);

        return roles;
    }

    /*
     * todo javadoc
     * */
    @Override
    public List<Role> removeRolesFromUser(String userId, Set<String> roleIds) {
        return removeRolesFromUser(userId, roleIds, new HashMap<>());
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<Role> removeRolesFromUser(String userId, Set<String> roleIds, Map<String, String> params) {
        List<Role> roles = findAllById(roleIds);
        if (roles.isEmpty() && !roleIds.isEmpty()) {
            throw new IllegalArgumentException("No roles found.");
        }
        if (roles.size() != roleIds.size()) {
            throw new IllegalArgumentException("Not all roles were found!");
        }

        roles = filterAssignedRoles(userId, roles);

        runAllSuitableActionsOnRoles(roles, RoleEventType.REMOVE, EventPhaseType.PRE, params);
        Set<String> roleIdsToRemove = roles.stream().map(Role::getStringId).collect(Collectors.toSet());
        List<RoleAssignment> removedAssignments = roleAssignmentService.removeAssignments(userId, roleIdsToRemove);
        if (roles.size() > removedAssignments.size()) {
            throw new NotAllRolesAssignedException(roles.size() - removedAssignments.size());
        }
        if (!removedAssignments.isEmpty()) {
            IUser user = userService.findById(userId);
            eventPublisher.publishEvent(new UserRemoveRoleEvent(user, roles));
        }
        runAllSuitableActionsOnRoles(roles, RoleEventType.REMOVE, EventPhaseType.POST, params);

        return roles;
    }

    public void clearCache() {
        this.defaultProcessRole = null;
        this.anonymousProcessRole = null;
    }

    private ProcessRole findSystemRoleByImportId(String importId) {
        List<ProcessRole> processRoles = processRoleRepository.findAllByImportId(importId);
        if (processRoles.isEmpty()) {
            throw new IllegalStateException(String.format("No %s process role has been found!", importId));
        }
        if (processRoles.size() > 1) {
            throw new IllegalStateException(String.format("More than 1 %s process role exists!", importId));
        }
        return processRoles.stream().findFirst().orElse(null);
    }

    /**
     * todo javadoc
     * */
    private <T> AccessPermissions<T> createRolesAndBuildPermissions(Case useCase, AccessPermissions<T> userRefPermissions,
                                                                    boolean saveUseCase) {
        List<Role> rolesToSave = new ArrayList<>();
        AccessPermissions<T> resultPermissions = new AccessPermissions<>();

        userRefPermissions.forEach((userListId, permissions) -> {
            CaseRole caseRole = new CaseRole(userListId, useCase.getStringId());
            Field<?> userListField = useCase.getDataSet().getFields().get(userListId);
            if (userListField != null) {
                ((FieldWithAllowedRoles<?>) userListField).getCaseRoleIds().add(caseRole.getStringId());
            } else {
                throw new IllegalStateException(String.format("Case role [%s} in process [%s] references non existing dataField in case [%s]",
                        userListId, useCase.getPetriNetId(), useCase.getStringId()));
            }
            rolesToSave.add(caseRole);
            resultPermissions.put(caseRole.getStringId(), new HashMap<>(permissions));
        });

        if (!userRefPermissions.isEmpty() && saveUseCase) {
            workflowService.save(useCase);
        }
        saveAll(rolesToSave);
        return resultPermissions;
    }

    private List<Role> filterAssignedRoles(String userId, List<Role> rolesToBeNotAssigned) {
        return filterRoles(userId, rolesToBeNotAssigned, true);
    }

    private List<Role> filterNotAssignedRoles(String userId, List<Role> rolesToBeNotAssigned) {
        return filterRoles(userId, rolesToBeNotAssigned, false);
    }

    private List<Role> filterRoles(String userId, List<Role> roles, boolean filterAssigned) {
        Set<String> roleIds = roles.stream().map(Role::getStringId).collect(Collectors.toSet());
        List<RoleAssignment> assignments = roleAssignmentService.findAllByUserIdAndRoleIdIn(userId, roleIds);

        if (!assignments.isEmpty()) {
            Set<String> assignedRoleIds = assignments.stream().map(RoleAssignment::getRoleId).collect(Collectors.toSet());
            return roles.stream().filter(role -> filterAssigned == assignedRoleIds.contains(role.getStringId()))
                    .collect(Collectors.toList());
        }
        return roles;
    }

    private void runAllSuitableActionsOnRoles(List<Role> roles, RoleEventType requiredEventType,
                                              EventPhaseType requiredPhase, Map<String, String> params) {
        roles.forEach(role -> runAllSuitableActionsOnOneRole(role.getEvents(), requiredEventType, requiredPhase, params));
    }

    private void runAllSuitableActionsOnOneRole(Map<RoleEventType, RoleEvent> eventMap, RoleEventType requiredEventType, EventPhaseType requiredPhase, Map<String, String> params) {
        if (eventMap == null) {
            return;
        }
        eventMap.forEach((eventType, event) -> {
            if (eventType != requiredEventType) {
                return;
            }

            runActionsBasedOnPhase(event, requiredPhase, params);
        });
    }

    private void runActionsBasedOnPhase(RoleEvent event, EventPhaseType requiredPhase, Map<String, String> params) {
        switch (requiredPhase) {
            case PRE:
                runActions(event.getPreActions(), params);
                break;
            case POST:
                runActions(event.getPostActions(), params);
                break;
        }
    }

    private void runActions(List<Action> actions, Map<String, String> params) {
        actions.forEach(action -> actionRunner.run(action, null, params));
    }
}
