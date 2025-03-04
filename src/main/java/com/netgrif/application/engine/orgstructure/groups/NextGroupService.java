package com.netgrif.application.engine.orgstructure.groups;

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.auth.config.GroupConfigurationProperties;
import com.netgrif.auth.repository.GroupRepository;
import com.netgrif.auth.service.GroupService;
import com.netgrif.auth.service.UserService;
import com.netgrif.core.auth.domain.Group;
import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.common.ResourceNotFoundException;
import com.netgrif.core.common.ResourceNotFoundExceptionCode;
import com.netgrif.core.petrinet.domain.I18nString;
import com.netgrif.core.petrinet.domain.PetriNet;
import com.netgrif.core.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.core.workflow.domain.Task;
import com.netgrif.core.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Primary
public class NextGroupService implements GroupService {

    @Autowired
    protected IWorkflowService workflowService;
    @Autowired
    protected UserService userService;

    @Autowired
    protected IDataService dataService;

    @Autowired
    protected IPetriNetService petriNetService;
//
    @Autowired
    protected ITaskService taskService;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected GroupConfigurationProperties groupConfigurationProperties;

    protected final static String GROUP_NET_IDENTIFIER = "org_group";
    protected final static String GROUP_INIT_TASK_ID = "2";

    protected final static String GROUP_CASE_IDENTIFIER = "org_group";
    protected final static String GROUP_MEMBERS_FIELD = "members";
    protected final static String GROUP_AUTHOR_FIELD = "author";
    protected final static String GROUP_TITLE_FIELD = "group_name";

    @Override
    public void delete(Group group) {
        if (!groupRepository.existsById(group.getStringId())) {
            log.error("Group {} does not exist", group.getStringId());
            throw new IllegalArgumentException("Group " + group.getStringId() + " does not exist");
        }
        log.info("Deleting group: {}", group.getStringId());
        groupRepository.delete(group);
    }

    @Override
    public Group save(Group group) {
        log.info("Saving group: {}", group.getStringId());
        return groupRepository.save(group);
    }

    @Override
    public Group findById(String id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Group [" + id + "] does not exist"));
        populateMembers(group);
        return group;
    }

    @Override
    public Set<Group> findAllByIds(Set<String> ids) {
        Set<Group> groups = groupRepository.findAllByIdIn(ids);
        groups.forEach(this::populateMembers);
        return groups;
    }

    @Override
    public Set<Group> findAll() {
        return new HashSet<>(groupRepository.findAll());
    }

    @Override
    public Optional<Group> findByIdentifier(String identifier) {
        Optional<Group> groupOptional = groupRepository.findByIdentifier(identifier);
        groupOptional.ifPresent(this::populateMembers);
        return groupOptional;
    }

    @Override
    public Group create(IUser user) {
        log.info("Creating default group for user: {}", user.getStringId());
        Optional<Group> groupOptional = groupRepository.findByOwnerId(user.getStringId());
        if (groupOptional.isPresent()) {
            throw new IllegalArgumentException("Default group for user [%s] already exists.".formatted(user.getUsername()));
        }
        return create(user.getUsername(), user.getName(), user);
    }

    @Override
    public Group create(String identifier, String title, IUser user) {
        try {
            Group userDefaultGroup = getDefaultUserGroup(user);
            if (userDefaultGroup != null && userDefaultGroup.getIdentifier().equals(identifier)) {
                return null;
            }
        } catch (ResourceNotFoundException e) {
            log.warn(e.getMessage());
        }

        Group group = new com.netgrif.adapter.auth.domain.Group(identifier);
        group.setOwnerId(user.getStringId());
        group.setOwnerUsername(user.getUsername());
        group.setDisplayName(title);
        user.addGroupId(group.getStringId());
        user.getGroups().add(group);
        userService.saveUser(user, user.getRealmId());

        PetriNet orgGroupNet = petriNetService.getNewestVersionByIdentifier(GROUP_NET_IDENTIFIER);
        CreateCaseEventOutcome outcome = workflowService.createCase(orgGroupNet.getStringId(), identifier, "", userService.transformToLoggedUser(user));
        outcome.getCase().getDataField("group_id").setValue(group.getStringId());

        Map<String, Map<String, String>> taskData = getInitialGroupData(user, title, outcome.getCase());
        Task initTask = getGroupInitTask(outcome.getCase());
        dataService.setData(initTask.getStringId(), ImportHelper.populateDataset(taskData));

        try {
            taskService.assignTask(userService.transformToLoggedUser(user), initTask.getStringId());
            taskService.finishTask(userService.transformToLoggedUser(user), initTask.getStringId());
        } catch (TransitionNotExecutableException e) {
            log.error(e.getMessage());
        }
        userService.saveUser(user, null);
        return save(group);
    }

    @Override
    public Group getDefaultUserGroup(IUser user) {
        Optional<Group> groupOptional = groupRepository.findByOwnerId(user.getStringId());
        return groupOptional.orElseThrow(() ->  new ResourceNotFoundException(ResourceNotFoundExceptionCode.DEFAULT_USER_GROUP_NOT_FOUND, "Default user group [" + user.getStringId() + "] does not exist"));
    }

    @Override
    public void addUserToDefaultSystemGroup(IUser user) {
        log.info("Adding user to default group: {}", user.getStringId());
        Optional<Group> optionalDefaultGroup = groupRepository.findByIdentifier(groupConfigurationProperties.getDefaultGroupIdentifier());
        if (optionalDefaultGroup.isEmpty()) {
            create(groupConfigurationProperties.getDefaultGroupIdentifier(), groupConfigurationProperties.getDefaultGroupTitle(), user);
        }
        addUser(user, getDefaultSystemGroup());
    }

    @Override
    public Group getDefaultSystemGroup() {
        return findByIdentifier(groupConfigurationProperties.getDefaultGroupIdentifier()).orElseThrow(() -> new ResourceNotFoundException(ResourceNotFoundExceptionCode.DEFAULT_SYSTEM_GROUP_NOT_FOUND, "Default system group not found."));
    }

    @Override
    public Group addUser(String userId, String groupId, String realmId) {
        IUser user = userService.findById(userId, realmId);
        Group group = findById(groupId);
        return addUser(user, group);
    }

    @Override
    public Group addUser(IUser user, String groupIdentifier) {
        Group group = findByIdentifier(groupIdentifier).orElseThrow(() -> new IllegalArgumentException("Group with identifier [%s] not found. ".formatted(groupIdentifier)));
        return addUser(user, group);
    }

    @Override
    public Group addUser(IUser user, Group group) {
        log.info("Adding user [{}] to group [{}]", user.getStringId(), group.getDisplayName());
        user.addGroupId(group.getStringId());
        user.getGroups().add(group);
        group.addMemberId(user.getStringId());
        group.getMembers().add(user);
        userService.saveUser(user, user.getRealmId());
        return save(group);
    }

    @Override
    public Group removeUser(IUser user, String groupIdentifier) {
        Group group = findByIdentifier(groupIdentifier).orElseThrow(() -> new IllegalArgumentException("Group with identifier [%s] not found. ".formatted(groupIdentifier)));
        return removeUser(user, group);
    }

    @Override
    public Group removeUser(IUser user, Group group) {
        log.info("Removing user [{}] from group [{}]", user.getStringId(), group.getDisplayName());
        user.removeGroupId(group.getStringId());
        user.getGroups().remove(group);
        group.removeMemberId(user.getStringId());
        group.getMembers().remove(user);
        userService.saveUser(user, user.getRealmId());
        return save(group);
    }

    @Override
    public void populateMembers(Group group) {
        group.getMemberIds().forEach(id -> {
            group.getMembers().add(userService.findById(id, null));
        });
    }

    @Override
    public Set<String> getAllCoMembers(IUser user) {
        Set<Group> userMembershipGroups = groupRepository.findAllByMemberIdsContains(user.getStringId());
        IUser system = userService.getSystem();
        return userMembershipGroups.stream().map(Group::getMemberIds).flatMap(Set::stream)
                .filter(id -> !id.equals(user.getStringId()))
                .filter(id -> !id.equals(system.getStringId()))
                .collect(Collectors.toSet());
    }

    @Override
    public Page<Group> findByPredicate(Predicate predicate, Pageable pageable) {
        return groupRepository.findAll(predicate, pageable);
    }

    public Map<String, I18nString> addUser(IUser user, Map<String, I18nString> existingUsers) {
        existingUsers.put(user.getStringId(), new I18nString(user.getEmail()));
        return existingUsers;
    }

    protected Task getGroupInitTask(Case groupCase) {
        List<TaskReference> taskList = taskService.findAllByCase(groupCase.getStringId(), LocaleContextHolder.getLocale());
        Optional<TaskReference> initTaskReference = taskList.stream().filter(taskReference ->
                        taskReference.getTransitionId().equals(GROUP_INIT_TASK_ID))
                .findFirst();

        if (initTaskReference.isEmpty()) {
            log.error("Initial task of group case is not present!");
            return null;
        }

        String initTaskId = initTaskReference.get().getStringId();
        return taskService.findById(initTaskId);
    }
//
    protected Map<String, Map<String, String>> getInitialGroupData(IUser author, String title, Case groupCase) {
        Map<String, Map<String, String>> taskData = new HashMap<>();

        groupCase.getDataField(GROUP_MEMBERS_FIELD).setOptions(addUser(author, new HashMap<>()));
        workflowService.save(groupCase);

        Map<String, String> authorData = new HashMap<>();
        authorData.put("type", "user");
        authorData.put("value", author.getStringId());

        Map<String, String> titleData = new HashMap<>();
        titleData.put("type", "text");
        titleData.put("value", title);

        taskData.put(GROUP_TITLE_FIELD, titleData);
        taskData.put(GROUP_AUTHOR_FIELD, authorData);
        return taskData;
    }
}
