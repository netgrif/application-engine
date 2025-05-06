package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.provider.CollectionNameProvider;
import com.netgrif.application.engine.auth.repository.GroupRepository;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.common.ResourceNotFoundException;
import com.netgrif.application.engine.objects.common.ResourceNotFoundExceptionCode;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.objects.auth.constants.SearchConstants.MAX_PAGE_SIZE;


@Slf4j
@Getter
public class GroupServiceImpl implements GroupService {

    private CollectionNameProvider collectionNameProvider;

    private UserService userService;

    private GroupRepository groupRepository;

    private AuthorityService authorityService;

    private GroupConfigurationProperties groupConfigurationProperties;

    private Group defaultSystemGroup;

    @Autowired
    public void setCollectionNameProvider(CollectionNameProvider collectionNameProvider) {
        this.collectionNameProvider = collectionNameProvider;
    }

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

    @Override
    public void delete(Group group) {
        if (!groupRepository.existsById(group.getStringId())) {
            log.error("Group [{}] does not exist", group.getStringId());
            throw new IllegalArgumentException("Group " + group.getStringId() + " does not exist");
        }
        if (group.getMemberIds() != null) {
            Set<AbstractUser> members = userService.findAllByIds(group.getMemberIds(), group.getRealmId());
            log.debug("Removing group [{}] from members [{}]", group.getStringId(), members);
            members.forEach(user -> {
                user.removeGroupId(group.getStringId());
                userService.saveUser(user);
            });
        }
        if (group.getSubgroupIds() != null) {
            log.debug("Removing group [{}] from child groups [{}]", group.getStringId(), group.getSubgroupIds());
            Set<Group> subGroups = findAllByIds(group.getSubgroupIds(), Pageable.ofSize(MAX_PAGE_SIZE)).toSet();
            subGroups.forEach(subgroup -> {
                subgroup.removeGroupId(group.getStringId());
                save(subgroup);
            });
        }
        if (group.getGroupIds() != null) {
            log.debug("Removing group [{}] from parent groups [{}]", group.getStringId(), group.getGroupIds());
            Set<Group> groups = findAllByIds(group.getGroupIds(), Pageable.ofSize(MAX_PAGE_SIZE)).toSet();
            groups.forEach(grup -> {
                grup.removeSubgroupId(group.getStringId());
                save(grup);
            });
        }
        log.debug("Deleting group: [{}]", group.getStringId());
        groupRepository.delete(group);
    }

    @Override
    public Group save(Group group) {
        log.debug("Saving group: [{}]", group.getStringId());
        group.setModifiedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    @Override
    public void removeAllByRealmId(String realmId) {
        this.groupRepository.removeAllByRealmId(realmId);
    }

    @Override
    public void removeAllByRealmIdInSet(Set<String> realmIds) {
        this.groupRepository.removeAllByRealmIdIn(realmIds);
    }

    @Override
    public Group findById(String id) {
        return groupRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Group [" + id + "] does not exist"));
    }

    @Override
    public Page<Group> findAllByIds(Set<String> ids, Pageable pageable) {
        return groupRepository.findAllByIdIn(ids, pageable);
    }

    @Override
    public Optional<Group> findByIdentifier(String identifier) {
        return groupRepository.findByIdentifier(identifier);
    }

    @Override
    public Group getDefaultSystemGroup() {
        if (defaultSystemGroup == null) {
            defaultSystemGroup = create(groupConfigurationProperties.getDefaultGroupIdentifier(), groupConfigurationProperties.getDefaultGroupTitle(), userService.getSystem());
        }
        return defaultSystemGroup;
    }

    @Override
    public Group create(AbstractUser user) {
        Optional<Group> groupOptional = groupRepository.findByOwnerId(user.getStringId());
        if (groupOptional.isPresent()) {
            throw new IllegalArgumentException("Default group for user [%s] already exists.".formatted(user.getUsername()));
        }
        return create(user.getUsername(), user.getName(), user);
    }

    @Override
    public Group create(String identifier, String title, AbstractUser user) {
        log.info("Creating default group for user: [{}]", user.getStringId());
        Group group = new Group(identifier, user.getRealmId());
        group.setOwnerId(user.getStringId());
        group.setOwnerUsername(user.getUsername());
        group.setDisplayName(title);
        group.addMemberId(user.getStringId());
        user.addGroupId(group.getStringId());
        userService.saveUser(user, user.getRealmId());
        return save(group);
    }

    @Override
    public Group getDefaultUserGroup(AbstractUser user) {
        Optional<Group> groupOptional = groupRepository.findByOwnerId(user.getStringId());
        return groupOptional.orElseThrow(() ->  new ResourceNotFoundException(ResourceNotFoundExceptionCode.DEFAULT_USER_GROUP_NOT_FOUND, "Default user group for user [" + user.getStringId() + "] does not exist"));
    }

    @Override
    public void addUserToDefaultSystemGroup(AbstractUser user) {
        log.info("Adding user [{}] to default group", user.getStringId());
        addUser(user, getDefaultSystemGroup());
    }

    @Override
    public Group addUser(String userId, String groupId, String realmId) {
        AbstractUser user = userService.findById(userId, realmId);
        return addUser(user, groupId);
    }

    @Override
    public Group addUser(String userId, Group group, String realmId) {
        AbstractUser user = userService.findById(userId, realmId);
        return addUser(user, group);
    }

    @Override
    public Group addUser(AbstractUser user, String groupIdentifier) {
        Group group = findByIdentifier(groupIdentifier).orElseThrow(() -> new IllegalArgumentException("Group with identifier [%s] not found. ".formatted(groupIdentifier)));
        return addUser(user, group);
    }

    @Override
    public Group addUser(AbstractUser user, Group group) {
        log.info("Adding user [{}] to group [{}]", user.getStringId(), group.getStringId());
        user.addGroupId(group.getStringId());
        group.addMemberId(user.getStringId());
        userService.saveUser(user, user.getRealmId());
        return save(group);
    }

    @Override
    public Group removeUser(AbstractUser user, String groupIdentifier) {
        Group group = findByIdentifier(groupIdentifier).orElseThrow(() -> new IllegalArgumentException("Group with identifier [%s] not found. ".formatted(groupIdentifier)));
        return removeUser(user, group);
    }

    @Override
    public Group removeUser(AbstractUser user, Group group) {
        log.info("Removing user [{}] from group [{}]", user.getStringId(), group.getStringId());
        user.removeGroupId(group.getStringId());
        group.removeMemberId(user.getStringId());
        userService.saveUser(user, user.getRealmId());
        return save(group);
    }

    @Override
    public Set<AbstractUser> getGroupMembersById(String groupId) {
        Group group = findById(groupId);
        return this.getGroupMembers(group);
    }

    @Override
    public Set<AbstractUser> getGroupMembers(Group group) {
        return this.userService.findAllByIds(group.getMemberIds(), group.getRealmId());
    }

    @Override
    public Set<String> getAllCoMembers(AbstractUser user) {
        Set<Group> userMembershipGroups = groupRepository.findAllByMemberIdsContains(user.getStringId());
        AbstractUser system = userService.getSystem();
        return userMembershipGroups.stream().map(Group::getMemberIds).flatMap(Set::stream)
                .filter(id -> !id.equals(user.getStringId()))
                .filter(id -> !id.equals(system.getStringId()))
                .collect(Collectors.toSet());
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
        group.addAuthority(authorityService.getOne(authorityId));
        return save(group);
    }

    @Override
    public Pair<Group, Group> addSubgroup(String parentGroupId, String childGroupId) {
        if (parentGroupId.equals(childGroupId)) {
            throw new IllegalArgumentException("Trying to add group to itself [%s]!".formatted(parentGroupId));
        }
        Group parentGroup = this.findById(parentGroupId);
        return this.addSubgroup(parentGroup, childGroupId);
    }

    @Override
    public Pair<Group, Group> addSubgroup(Group parentGroup, String childGroupId) {
        if (parentGroup.getStringId().equals(childGroupId)) {
            throw new IllegalArgumentException("Trying to add group to itself [%s]!".formatted(parentGroup.getStringId()));
        }
        Group childGroup = this.findById(childGroupId);
        return this.addSubgroup(parentGroup, childGroup);
    }

    @Override
    public Pair<Group, Group> addSubgroup(String parentGroupId, Group childGroup) {
        if (parentGroupId.equals(childGroup.getStringId())) {
            throw new IllegalArgumentException("Trying to add group to itself [%s]!".formatted(parentGroupId));
        }
        Group parentGroup = this.findById(parentGroupId);
        return this.addSubgroup(parentGroup, childGroup);
    }

    @Override
    public Pair<Group, Group> addSubgroup(Group parentGroup, Group childGroup) {
        // TODO: maybe handle groups cycles here?
        if (parentGroup.getStringId().equals(childGroup.getStringId())) {
            throw new IllegalArgumentException("Trying to add group to itself [%s]!".formatted(parentGroup.getStringId()));
        }
        parentGroup.addSubGroupId(childGroup.getStringId());
        childGroup.addGroupId(parentGroup.getStringId());
        log.info("Adding group [{}] to parent group [{}]", childGroup.getStringId(), parentGroup.getStringId());
        this.save(parentGroup);
        this.save(childGroup);
        return Pair.of(parentGroup, childGroup);
    }

    @Override
    public Set<Group> getGroupParentGroupsById(String groupId) {
        Group group = this.findById(groupId);
        return this.getGroupParentGroups(group);
    }

    @Override
    public Set<Group> getGroupParentGroups(Group group) {
        if (group.getGroupIds() == null || group.getGroupIds().isEmpty()) {
            return new HashSet<>();
        }
        return this.findAllByIds(group.getGroupIds(), Pageable.ofSize(MAX_PAGE_SIZE)).toSet();
    }

    @Override
    public Set<Group> getGroupSubgroupsById(String groupId) {
        Group group = this.findById(groupId);
        return this.getGroupSubgroups(group);
    }

    @Override
    public Set<Group> getGroupSubgroups(Group group) {
        if (group.getSubgroupIds() == null || group.getSubgroupIds().isEmpty()) {
            return new HashSet<>();
        }
        return this.findAllByIds(group.getSubgroupIds(), Pageable.ofSize(MAX_PAGE_SIZE)).toSet();
    }
}
