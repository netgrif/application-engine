package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.RoleNotFoundException;
import com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.RoleNotGlobalException;
import com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.RoleReferencedException;
import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.event.events.user.UserRoleChangeEvent;
import com.netgrif.application.engine.objects.importer.model.EventPhaseType;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.objects.petrinet.domain.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.context.RoleContext;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.RoleActionsRunner;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessRoleService implements com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService {

    private static final Logger log = LoggerFactory.getLogger(ProcessRoleService.class);

    private final UserService userService;
    @Getter
    private final ProcessRoleRepository processRoleRepository;
    private final PetriNetRepository netRepository;
    private final ApplicationEventPublisher publisher;
    private final RoleActionsRunner roleActionsRunner;
    private final IPetriNetService petriNetService;
    private final ISecurityContextService securityContextService;
    private final GroupService groupService;
    private final RealmService realmService;
    @Getter
    private final PaginationProperties paginationProperties;
    @Getter
    private final IWorkflowService workflowService;
    @Getter
    private final ITaskService taskService;

    private ProcessRole defaultRole;
    private ProcessRole anonymousRole;

    public ProcessRoleService(ProcessRoleRepository processRoleRepository,
                              PetriNetRepository netRepository,
                              ApplicationEventPublisher publisher, RoleActionsRunner roleActionsRunner,
                              @Lazy IPetriNetService petriNetService, @Lazy UserService userService, ISecurityContextService securityContextService, @Lazy GroupService groupService,
                              @Lazy RealmService realmService, @Lazy PaginationProperties paginationProperties, @Lazy IWorkflowService workflowService, @Lazy ITaskService taskService) {
        this.processRoleRepository = processRoleRepository;
        this.netRepository = netRepository;
        this.publisher = publisher;
        this.roleActionsRunner = roleActionsRunner;
        this.petriNetService = petriNetService;
        this.userService = userService;
        this.securityContextService = securityContextService;
        this.groupService = groupService;
        this.realmService = realmService;
        this.paginationProperties = paginationProperties;
        this.workflowService = workflowService;
        this.taskService = taskService;
    }

    @Override
    public ProcessRole save(ProcessRole processRole) {
        return processRoleRepository.save(processRole);
    }

    @Override
    public Page<ProcessRole> getAll(Pageable pageable) {
        return processRoleRepository.findAll(pageable);
    }

    @Override
    public Optional<ProcessRole> get(ProcessResourceId processResourceId) {
        return processRoleRepository.findByCompositeId(processResourceId.getStringId());
    }

    @Override
    public void delete(String compositeId) {
        if (compositeId == null) {
            return;
        }
        Optional<ProcessRole> processRole = processRoleRepository.findByCompositeId(compositeId);
        processRole.ifPresent(processRoleRepository::delete);
    }

    @Override
    public void deleteAll(Collection<String> collection) {
        Set<ProcessRole> processRoles = processRoleRepository.findAllByIdsSet(collection);
        processRoleRepository.deleteAll(processRoles);
    }

    @Override
    public void deleteAll() {
        processRoleRepository.deleteAll();
    }


    @Override
    public void assignRolesToUser(AbstractUser user, Collection<ProcessResourceId> processResourceIds, LoggedUser loggedUser) {
        assignRolesToActor(user.getProcessRoles(), processResourceIds);
        saveUserAndReloadContext(user, loggedUser);
    }

    @Override
    public void assignRolesToGroup(Group group, Collection<ProcessResourceId> requestedRolesIds) {
        assignRolesToActor(group.getProcessRoles(), requestedRolesIds);
        groupService.save(group);
    }

    protected void assignRolesToActor(Collection<ProcessRole> oldActorRoles, Collection<ProcessResourceId> requestedRolesIds) {
        List<ProcessRole> requestedRoles = this.findByIds(requestedRolesIds.stream().map(ProcessResourceId::toString).collect(Collectors.toSet()));
        if (requestedRoles.isEmpty() && !requestedRolesIds.isEmpty())
            throw new IllegalArgumentException("No process roles found.");
        if (requestedRoles.size() != requestedRolesIds.size())
            throw new IllegalArgumentException("Not all process roles were found!");

        Set<ProcessRole> userOldRoles = new HashSet<>(oldActorRoles);
        Set<ProcessRole> rolesNewToUser = getRolesNewToActor(userOldRoles, requestedRoles);
        Set<ProcessRole> rolesRemovedFromUser = getRolesRemovedFromActor(userOldRoles, requestedRoles);


        oldActorRoles.clear();
        oldActorRoles.addAll(updateRequestedRoles(userOldRoles, rolesNewToUser, rolesRemovedFromUser));
    }

    protected void saveUserAndReloadContext(AbstractUser user, LoggedUser loggedUser) {
        userService.saveUser(user);

        String userId = user.getStringId();
        securityContextService.saveToken(userId);
        if (Objects.equals(userId, loggedUser.getSelfOrImpersonatedStringId())) {
            loggedUser.getProcessRoles().clear();
            loggedUser.setProcessRolesToLoggedUser(user.getProcessRoles());
            securityContextService.reloadSecurityContext(loggedUser);
        }
    }

    protected Set<ProcessRole> getRolesNewToActor(Collection<ProcessRole> userOldRoles, Collection<ProcessRole> newRequestedRoles) {
        Set<ProcessRole> rolesNewToUser = new HashSet<>(newRequestedRoles);
        rolesNewToUser.removeAll(userOldRoles);

        return rolesNewToUser;
    }

    protected Set<ProcessRole> getRolesRemovedFromActor(Collection<ProcessRole> userOldRoles, Collection<ProcessRole> newRequestedRoles) {
        Set<ProcessRole> rolesRemovedFromUser = new HashSet<>(userOldRoles);
        rolesRemovedFromUser.removeAll(newRequestedRoles);

        return rolesRemovedFromUser;
    }

    protected Set<ProcessRole> updateRequestedRoles(Set<ProcessRole> userRolesAfterPreActions, Set<ProcessRole> rolesNewToUser, Set<ProcessRole> rolesRemovedFromUser) {
        userRolesAfterPreActions.addAll(rolesNewToUser);
        userRolesAfterPreActions.removeAll(rolesRemovedFromUser);

        return new HashSet<>(userRolesAfterPreActions);
    }

    protected String getProcessIdRoleBelongsTo(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles) {

        if (!newRoles.isEmpty()) {
            return getProcessIdFromFirstRole(newRoles);
        }

        if (!removedRoles.isEmpty()) {
            return getProcessIdFromFirstRole(removedRoles);
        }

        return null;
    }

    protected String getProcessIdFromFirstRole(Set<ProcessRole> newRoles) {
        return newRoles.iterator().next().getProcessId();
    }

    @Override
    public List<ProcessRole> findAllByIds(Collection<ProcessResourceId> collection) {
        return new ArrayList<>(processRoleRepository.findAllByIdsSet(collection.stream().map(ProcessResourceId::getStringId).collect(Collectors.toList())));
    }

    @Override
    public List<ProcessRole> saveAll(Collection<ProcessRole> entities) {
        return entities.stream().map(processRole -> {
            if (!processRole.isGlobal() || findAllByImportId(processRole.getImportId(), Pageable.ofSize(1)).isEmpty()) {
                return processRoleRepository.save(processRole);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<ProcessRole> findByIds(Collection<String> ids) {
        return new ArrayList<>(processRoleRepository.findAllByIdsSet(ids));
    }

    private Set<ProcessRole> updateRequestedRoles(AbstractUser user, Set<ProcessRole> rolesNewToUser, Set<ProcessRole> rolesRemovedFromUser) {
        Set<ProcessRole> userRolesAfterPreActions = user.getProcessRoles();
        userRolesAfterPreActions.addAll(rolesNewToUser);
        userRolesAfterPreActions.removeAll(rolesRemovedFromUser);

        return new HashSet<>(userRolesAfterPreActions);
    }

    private String getPetriNetIdRoleBelongsTo(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles) {

        if (!newRoles.isEmpty()) {
            return getPetriNetIdFromFirstRole(newRoles);
        }

        if (!removedRoles.isEmpty()) {
            return getPetriNetIdFromFirstRole(removedRoles);
        }

        return null;
    }

    private boolean isGlobalFromFirstRole(Set<ProcessRole> roles) {
        if (roles.isEmpty()) {
            return false;
        }
        ProcessRole role = roles.iterator().next();
        return role.isGlobal();
    }

    private String getPetriNetIdFromFirstRole(Set<ProcessRole> newRoles) {
        return newRoles.iterator().next().getProcessId();
    }

    private void replaceUserRolesAndPublishEvent(Set<String> requestedRolesIds, AbstractUser user, Set<ProcessRole> requestedRoles) {
        removeOldAndAssignNewRolesToUser(user, requestedRoles);
        publisher.publishEvent(new UserRoleChangeEvent(user, this.findByIds(requestedRolesIds)));
    }

    private Set<ProcessRole> getRolesNewToUser(Set<ProcessRole> userOldRoles, Set<ProcessRole> newRequestedRoles) {
        Set<ProcessRole> rolesNewToUser = new HashSet<>(newRequestedRoles);
        rolesNewToUser.removeAll(userOldRoles);

        return rolesNewToUser;
    }

    private Set<ProcessRole> getRolesRemovedFromUser(Set<ProcessRole> userOldRoles, Set<ProcessRole> newRequestedRoles) {
        Set<ProcessRole> rolesRemovedFromUser = new HashSet<>(userOldRoles);
        rolesRemovedFromUser.removeAll(newRequestedRoles);

        return rolesRemovedFromUser;
    }

    private Set<String> mapUserRolesToIds(Collection<ProcessRole> processRoles) {
        return processRoles.stream()
                .map(ProcessRole::getStringId)
                .collect(Collectors.toSet());
    }

    // todo remove unused methods

    private void runAllPreActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles, AbstractUser user, PetriNet petriNet, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.PRE, user, petriNet, params);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.PRE, user, petriNet, params);
    }

    private void runAllPostActions(Set<ProcessRole> newRoles, Set<ProcessRole> removedRoles, AbstractUser user, PetriNet petriNet, Map<String, String> params) {
        runAllSuitableActionsOnRoles(newRoles, EventType.ASSIGN, EventPhaseType.POST, user, petriNet, params);
        runAllSuitableActionsOnRoles(removedRoles, EventType.CANCEL, EventPhaseType.POST, user, petriNet, params);
    }

    private void runAllSuitableActionsOnRoles(Set<ProcessRole> roles, EventType requiredEventType, EventPhaseType requiredPhase, AbstractUser user, PetriNet petriNet, Map<String, String> params) {
        roles.forEach(role -> {
            RoleContext roleContext = new RoleContext<>(user, role, petriNet);
            runAllSuitableActionsOnOneRole(role.getEvents(), requiredEventType, requiredPhase, roleContext, params);
        });
    }

    private void runAllSuitableActionsOnOneRole(Map<EventType, Event> eventMap, EventType requiredEventType, EventPhaseType requiredPhase, RoleContext<?> roleContext, Map<String, String> params) {
        if (eventMap == null) {
            return;
        }
        eventMap.forEach((eventType, event) -> {

            if (eventType != requiredEventType) {
                return;
            }

            runActionsBasedOnPhase(event, requiredPhase, roleContext, params);
        });
    }

    private void runActionsBasedOnPhase(Event event, EventPhaseType requiredPhase, RoleContext<?> roleContext, Map<String, String> params) {
        switch (requiredPhase) {
            case PRE:
                runActions(event.getPreActions(), roleContext, params);
                break;
            case POST:
                runActions(event.getPostActions(), roleContext, params);
                break;
        }
    }

    private void runActions(List<Action> actions, RoleContext<?> roleContext, Map<String, String> params) {
        actions.forEach(action -> roleActionsRunner.run(action, roleContext, params));
    }

    private void removeOldAndAssignNewRolesToUser(AbstractUser user, Set<ProcessRole> requestedRoles) {
        user.getProcessRoles().clear();
        user.getProcessRoles().addAll(requestedRoles);

        userService.saveUser(user, null);
    }

    @Override
    public Page<ProcessRole> findAll(Pageable pageable) {
        return processRoleRepository.findAll(pageable);
    }

    @Override
    public Page<ProcessRole> findAllGlobalRoles(Pageable pageable) {
        return processRoleRepository.findAllByGlobalIsTrue(pageable);
    }

    @Override
    public List<ProcessRole> findAllByNetStringId(String netStringId) {
        Optional<PetriNet> netOptional = netRepository.findById(netStringId);
        if (netOptional.isEmpty())
            throw new IllegalArgumentException("Could not find model with id [" + netStringId + "]");
        return new ArrayList<>(netOptional.get().getRoles().values());
    }

    @Override
    public ProcessRole findById(String id) {
        ObjectId objectId = extractObjectId(id);
        return processRoleRepository.findByIdObjectId(objectId).orElse(null);
    }

    @Override
    public ProcessRole findById(ProcessResourceId processResourceId) {
        return processRoleRepository.findByCompositeId(processResourceId.getStringId()).orElse(null);
    }

    @Override
    public ProcessRole getDefaultRole() {
        if (defaultRole == null) {
            Page<ProcessRole> roles = processRoleRepository.findAllByImportId(ProcessRole.DEFAULT_ROLE, Pageable.ofSize(2));
            if (roles.isEmpty())
                throw new IllegalStateException("No default process role has been found!");
            if (roles.getTotalElements() > 1)
                throw new IllegalStateException("More than 1 default process role exists!");
            defaultRole = roles.stream().findFirst().orElse(null);
        }
        return defaultRole;
    }

    @Override
    public ProcessRole getAnonymousRole() {
        if (anonymousRole == null) {
            Page<ProcessRole> roles = processRoleRepository.findAllByImportId(ProcessRole.ANONYMOUS_ROLE, Pageable.ofSize(2));
            if (roles.isEmpty())
                throw new IllegalStateException("No anonymous process role has been found!");
            if (roles.getTotalElements() > 1)
                throw new IllegalStateException("More than 1 anonymous process role exists!");
            anonymousRole = roles.stream().findFirst().orElse(null);
        }
        return anonymousRole;
    }

    /**
     * @param importId id from a process of a role
     * @return a process role object
     * @deprecated use {@link ProcessRoleService#findAllByImportId(String, Pageable)} instead
     */
    @Deprecated(forRemoval = true, since = "6.2.0")
    @Override
    public ProcessRole findByImportId(String importId) {
        return processRoleRepository.findByImportId(importId).orElse(null);
    }

    @Override
    public Page<ProcessRole> findAllByImportId(String importId, Pageable pageable) {
        return processRoleRepository.findAllByImportId(importId, pageable);
    }

    @Override
    public Page<ProcessRole> findAllByDefaultName(String name, Pageable pageable) {
        return processRoleRepository.findAllByName_DefaultValue(name, pageable);
    }

    @Override
    public void deleteRolesOfNet(PetriNet net, LoggedUser loggedUser) {
        log.info("[{}]: Initiating deletion of all roles of Petri net {} version {}", net.getStringId(), net.getIdentifier(),
                net.getVersion().toString());
        List<ProcessResourceId> deletedRoleIds = this.findAllByNetStringId(net.getStringId()).stream()
                .filter(processRole -> !processRole.isGlobal())
                .map(ProcessRole::get_id)
                .collect(Collectors.toList());
        Set<String> deletedRoleStringIds = deletedRoleIds.stream().map(ProcessResourceId::toString).collect(Collectors.toSet());

        Pageable realmPageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Page<Realm> realms;
        do {
            realms = realmService.getSmallRealm(realmPageable);

            realms.forEach(realm -> {
                Pageable usersPageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
                Page<AbstractUser> users;
                do {
                    users = this.userService.findAllByProcessRoles(new HashSet<>(deletedRoleIds), realm.getName(), usersPageable);

                    for (AbstractUser user : users) {
                        log.info("[{}]: Removing deleted roles of Petri net {} version {} from user {} with id {}",
                                net.getStringId(), net.getIdentifier(), net.getVersion().toString(), user.getFullName(), user.getStringId());

                        if (user.getProcessRoles().isEmpty()) {
                            continue;
                        }

                        Set<ProcessResourceId> newRoles = user.getProcessRoles().stream()
                                .filter(role -> !deletedRoleStringIds.contains(role.getStringId()))
                                .map(ProcessRole::get_id)
                                .collect(Collectors.toSet());
                        this.assignRolesToUser(user, newRoles, loggedUser);
                    }

                    usersPageable = usersPageable.next();
                } while (users.hasNext());
            });

            realmPageable = realmPageable.next();
        } while (realms.hasNext());

        log.info("[{}]: Deleting all roles of Petri net {} version {}", net.getStringId(), net.getIdentifier(),
                net.getVersion().toString());
        this.processRoleRepository.deleteAllBy_idIn(deletedRoleIds);
    }

    @Override
    public void clearCache() {
        this.defaultRole = null;
        this.anonymousRole = null;
    }

    @Override
    public void deleteGlobalRole(String roleId, LoggedUser loggedUser) {
        ProcessRole processRole = this.findById(roleId);
        if (processRole == null) {
            throw new RoleNotFoundException("Role with id [%s] not found.".formatted(roleId));
        }
        if (!processRole.isGlobal()) {
            throw new RoleNotGlobalException("Role with id [%s] is not global.".formatted(roleId));
        }
        if (ProcessRole.DEFAULT_ROLE.equals(processRole.getImportId()) || ProcessRole.ANONYMOUS_ROLE.equals(processRole.getImportId())) {
            throw new IllegalArgumentException("Deleting core roles (DEFAULT/ANONYMOUS) is forbidden.");
        }
        if (isRoleReferenced(processRole)) {
            throw new RoleReferencedException("Role with id [%s] is referenced by other processes. Please delete or update the process before deleting.".formatted(roleId));
        }
        log.info("Initiating deletion of global role with import ID [{}] and object ID [{}]", processRole.getImportId(),
                processRole.getStringId());
        Pageable realmPageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Pageable usersPageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Page<Realm> realms;
        do {
            realms = realmService.getSmallRealm(realmPageable);
            realms.forEach(realm -> {
                Page<AbstractUser> users = userService.findAllByProcessRoles(Set.of(processRole.get_id()), realm.getName(), usersPageable);
                while (users.hasContent()) {
                    users.getContent().forEach(u -> removeRoleFromUser(u, processRole, loggedUser));
                    users = userService.findAllByProcessRoles(Set.of(processRole.get_id()), realm.getName(), usersPageable);
                }
            });
            realmPageable = realmPageable.next();
        } while (realms.hasNext());
        log.info("Deleting global role with import ID [{}] and object ID [{}]", processRole.getImportId(), processRole.getStringId());
        this.processRoleRepository.delete(processRole);
    }

    protected boolean isRoleReferenced(ProcessRole processRole) {
        Pageable pageable = PageRequest.of(0, 1);
        Page<PetriNet> petriNetPage = petriNetService.findAllByRoleId(processRole.getStringId(), pageable);
        return petriNetPage.getTotalElements() > 0;
    }

    private void removeRoleFromUser(AbstractUser user, ProcessRole processRole, LoggedUser loggedUser) {
        log.info("Removing global role with import ID [{}] and object ID [{}] from user [{}] with id [{}]",
                processRole.getImportId(), processRole.getStringId(), user.getFullName(), user.getStringId());
        if (user.getProcessRoles().isEmpty()) {
            return;
        }
        Set<ProcessResourceId> newRoles = user.getProcessRoles().stream()
                .filter(role -> !role.getStringId().equals(processRole.getStringId()))
                .map(ProcessRole::get_id)
                .collect(Collectors.toSet());
        this.assignRolesToUser(user, newRoles, loggedUser);
    }

    private ObjectId extractObjectId(String caseId) {
        String[] parts = caseId.split("-");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid NetgrifId format: " + caseId);
        }
        String objectIdPart = parts[1];

        return new ObjectId(objectIdPart);
    }
}
