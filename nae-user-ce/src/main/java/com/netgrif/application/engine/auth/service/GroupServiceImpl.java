package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.repository.GroupRepository;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.common.ResourceNotFoundException;
import com.netgrif.application.engine.objects.common.ResourceNotFoundExceptionCode;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

@Slf4j
@Getter
public class GroupServiceImpl implements GroupService {

    private UserService userService;

    private GroupRepository groupRepository;

    private AuthorityService authorityService;

    private GroupConfigurationProperties groupConfigurationProperties;

    private Group defaultSystemGroup;

    private PaginationProperties paginationProperties;

    @Autowired
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Autowired
    public void setGroupConfigurationProperties(GroupConfigurationProperties groupConfigurationProperties) {
        this.groupConfigurationProperties = groupConfigurationProperties;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Autowired
    public void setPaginationProperties(PaginationProperties paginationProperties) {
        this.paginationProperties = paginationProperties;
    }

    @Override
    public void delete(Group group) {
        if (!groupRepository.existsById(group.getStringId())) {
            log.error("Group [{}] does not exist", group.getStringId());
            throw new IllegalArgumentException("Group " + group.getStringId() + " does not exist");
        }
        log.debug("Deleting group: [{}]", group.getStringId());
        groupRepository.delete(group);
    }

    @Override
    public Group save(Group group) {
        log.debug("Saving group: [{}]", group.getStringId());
        return groupRepository.save(group);
    }

    @Override
    public Group findById(String id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Group [" + id + "] does not exist"));
        populateMembers(group);
        return group;
    }

    @Override
    public Page<Group> findAllByIds(Collection<String> ids, Pageable pageable) {
        Page<Group> groups = groupRepository.findAllByIdIn(ids, pageable);
        groups.getContent().forEach(this::populateMembers);
        return groups;
    }

    @Override
    public Optional<Group> findByIdentifier(String identifier) {
        Optional<Group> groupOptional = groupRepository.findByIdentifier(identifier);
        groupOptional.ifPresent(this::populateMembers);
        return groupOptional;
    }

    @Override
    public Group getDefaultSystemGroup() {
        if (defaultSystemGroup == null) {
            defaultSystemGroup = create(groupConfigurationProperties.getDefaultGroupIdentifier(), groupConfigurationProperties.getDefaultGroupTitle(), userService.getSystem());
        }
        return defaultSystemGroup;
    }

    @Override
    public Group create(IUser user) {
        log.info("Creating default group for user: [{}]", user.getStringId());
        Page<Group> userGroups = groupRepository.findByOwnerId(user.getStringId(), Pageable.ofSize(1));
        if (!userGroups.isEmpty() && !Objects.equals(user.getStringId(), userService.getSystem().getStringId())) {
            throw new IllegalArgumentException("Default group for user [%s] already exists.".formatted(user.getUsername()));
        }
        return create(user.getUsername(), user.getName(), user);
    }

    @Override
    public Group create(String identifier, String title, IUser user) {
        Group group = new com.netgrif.application.engine.adapter.spring.auth.domain.Group(identifier, user.getRealmId());
        group.setOwnerId(user.getStringId());
        group.setOwnerUsername(user.getUsername());
        group.setDisplayName(title);
        group.addMemberId(user.getStringId());
        group.getMembers().add(user);
        user.addGroupId(group.getStringId());
        user.getGroups().add(group);
        userService.saveUser(user, user.getRealmId());
        return save(group);
    }

    @Override
    public Group getDefaultUserGroup(IUser user) {
        String errorMessage = "Default user group for user [%s] does not exist.".formatted(user.getUsername());
        Pageable pageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Page<Group> userGroups;
        do {
            userGroups = groupRepository.findByOwnerId(user.getStringId(), pageable);

            Optional<Group> group = userGroups.stream().filter(g -> g.getIdentifier().equals(user.getUsername())).findFirst();
            if (group.isPresent()) {
                return group.get();
            }

            pageable = pageable.next();
        } while (userGroups.hasNext());
        throw new ResourceNotFoundException(ResourceNotFoundExceptionCode.DEFAULT_USER_GROUP_NOT_FOUND, errorMessage);
    }

    @Override
    public void addUserToDefaultSystemGroup(IUser user) {
        log.info("Adding user [{}] to default group", user.getStringId());
        addUser(user, getDefaultSystemGroup());
    }

    @Override
    public Group addUser(String userId, String groupId, String realmId) {
        IUser user = userService.findById(userId, realmId);
        return addUser(user, groupId);
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
//            todo realmId rovnaké ako user realmId?
            group.getMembers().add(userService.findById(id, group.getRealmId()));
        });
    }

    @Override
    public Page<Group> findByPredicate(Predicate predicate, Pageable pageable) {
        return groupRepository.findAll(predicate, pageable);
    }

    @Override
    public Page<Group> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable);
    }

    @Override
    public Page<Group> findAllFromRealm(String realmId, Pageable pageable) {
        return groupRepository.findAllByRealmId(realmId, pageable);
    }

    @Override
    public Group assignAuthority(String groupId, String authorityId) {
        Group group = findById(groupId);
        Authority authority = authorityService.getOne(authorityId);
        group.addAuthority(authority);
        authority.addGroup(group);
        return save(group);
    }

    @Override
    public Page<Group> findByIds(Collection<String> ids, Pageable pageable) {
        return groupRepository.findAllByIdIn(ids, pageable);
    }

    @Override
    public Page<String> getGroupsOwnerEmails(Collection<String> groupIds, Pageable pageable) {
        return this.findByIds(groupIds, pageable).map(this::getGroupOwnerEmail);
    }

    @Override
    public String getGroupOwnerEmail(String groupId) {
        return this.getGroupOwnerEmail(findById(groupId));
    }

    protected String getGroupOwnerEmail(Group groupCase) {
        return userService.findById(groupCase.getOwnerId(), groupCase.getRealmId()).getEmail();
    }
}
