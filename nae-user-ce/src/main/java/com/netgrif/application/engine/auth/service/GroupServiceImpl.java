package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.provider.CollectionNameProvider;
import com.netgrif.application.engine.auth.repository.GroupRepository;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
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
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Getter
public class GroupServiceImpl implements GroupService {

    private CollectionNameProvider collectionNameProvider;

    private UserService userService;

    private GroupRepository groupRepository;

    private AuthorityService authorityService;

    private GroupConfigurationProperties groupConfigurationProperties;

    private Group defaultSystemGroup;

    private PaginationProperties paginationProperties;

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
        if (group.getMemberIds() != null) {
            List<AbstractUser> members = userService.findAllByIds(group.getMemberIds(), group.getRealmId());
            log.debug("Removing group [{}] from members [{}]", group.getStringId(), members);
            members.forEach(user -> {
                user.removeGroupId(group.getStringId());
                userService.saveUser(user);
            });
        }
        if (group.getSubgroupIds() != null) {
            log.debug("Removing group [{}] from child groups [{}]", group.getStringId(), group.getSubgroupIds());
            List<Group> subGroups = findByIds(group.getSubgroupIds(), Pageable.unpaged()).stream().toList();
            subGroups.forEach(subgroup -> {
                subgroup.removeGroupId(group.getStringId());
                save(subgroup);
            });
        }
        if (group.getGroupIds() != null) {
            log.debug("Removing group [{}] from parent groups [{}]", group.getStringId(), group.getGroupIds());
            List<Group> groups = findByIds(group.getGroupIds(), Pageable.unpaged()).stream().toList();
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
<<<<<<< HEAD
    public void removeAllByRealmIdIn(Collection<String> realmIds) {
        if (realmIds == null || realmIds.isEmpty()) {
            this.removeAllGroups();
        }
        this.groupRepository.removeAllByRealmIdIn(realmIds);
    }

    @Override
    public void removeAllGroups() {
        this.groupRepository.deleteAll();
    }

    @Override
    public Group findById(String id) {
        return groupRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Group [" + id + "] does not exist"));
    }

    @Override
    public Page<Group> findByIds(Collection<String> ids, Pageable pageable) {
        return groupRepository.findAllByIdIn(ids, pageable);
=======
    public Page<Group> findAllByIds(Collection<String> ids, Pageable pageable) {
        Page<Group> groups = groupRepository.findAllByIdIn(ids, pageable);
        groups.getContent().forEach(this::populateMembers);
        return groups;
>>>>>>> NAE-2122
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
<<<<<<< HEAD
    public Group create(AbstractUser groupOwner) {
        log.info("Creating default group for owner: [{}]", groupOwner.getStringId());
        List<Group> userGroups = groupRepository.findByOwnerId(groupOwner.getStringId());
        if (!userGroups.isEmpty() && !Objects.equals(groupOwner.getStringId(), userService.getSystem().getStringId())) {
            throw new IllegalArgumentException("Default group for owner [%s] already exists.".formatted(groupOwner.getUsername()));
=======
    public Group create(IUser user) {
        log.info("Creating default group for user: [{}]", user.getStringId());
        Page<Group> userGroups = groupRepository.findByOwnerId(user.getStringId(), Pageable.ofSize(1));
        if (!userGroups.isEmpty() && !Objects.equals(user.getStringId(), userService.getSystem().getStringId())) {
            throw new IllegalArgumentException("Default group for user [%s] already exists.".formatted(user.getUsername()));
>>>>>>> NAE-2122
        }
        return create(groupOwner.getUsername(), groupOwner.getName(), groupOwner);
    }

    @Override
    public Group create(String identifier, String title, AbstractUser groupOwner) {
        log.info("Creating default group for user: [{}]", groupOwner.getStringId());
        Group group = new Group(identifier, groupOwner.getRealmId());
        group.setOwnerId(groupOwner.getStringId());
        group.setOwnerUsername(groupOwner.getUsername());
        group.setDisplayName(title);
        group.addMemberId(groupOwner.getStringId());
        groupOwner.addGroupId(group.getStringId());
        userService.saveUser(groupOwner, groupOwner.getRealmId());
        return save(group);
    }

    @Override
<<<<<<< HEAD
    public Group getDefaultUserGroup(AbstractUser user) {
        List<Group> userGroup = groupRepository.findByOwnerId(user.getStringId());
=======
    public Group getDefaultUserGroup(IUser user) {
>>>>>>> NAE-2122
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

//    @Override
//    public Group getDefaultUserGroup(AbstractUser user) {
//        Optional<Group> groupOptional = groupRepository.findByOwnerId(user.getStringId());
//        return groupOptional.orElseThrow(() ->  new ResourceNotFoundException(ResourceNotFoundExceptionCode.DEFAULT_USER_GROUP_NOT_FOUND, "Default user group for user [" + user.getStringId() + "] does not exist"));
//    }

    @Override
    public void addUserToDefaultSystemGroup(AbstractUser user) {
        log.info("Adding user [{}] to default group", user.getStringId());
        addUser(user, getDefaultSystemGroup());
    }

    @Override
    public Group addUser(String userId, String groupId, String realmId) {
        return addUser(userService.findById(userId, realmId), groupId);
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
    public List<AbstractUser> getGroupMembersById(String groupId) {
        Group group = findById(groupId);
        return this.getGroupMembers(group);
    }

    @Override
<<<<<<< HEAD
    public List<AbstractUser> getGroupMembers(Group group) {
        return this.userService.findAllByIds(group.getMemberIds(), group.getRealmId());
    }

    @Override
    public List<String> getAllCoMembers(AbstractUser user) {
        List<Group> userMembershipGroups = groupRepository.findAllByMemberIdsContains(user.getStringId());
        AbstractUser system = userService.getSystem();
        return userMembershipGroups.stream().map(Group::getMemberIds).flatMap(Set::stream)
                .filter(id -> !id.equals(user.getStringId()))
                .filter(id -> !id.equals(system.getStringId()))
                .collect(Collectors.toList());
    }

    @Override
=======
>>>>>>> NAE-2122
    public Page<Group> findByPredicate(Predicate predicate, Pageable pageable) {
        return groupRepository.findAll(predicate, pageable);
    }

    @Override
    public List<Group> findAllByIds(Collection<String> ids) {
        return groupRepository.findAllById(ids);
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
    public Page<Group> findAllFromRealmIn(Collection<String> realmIds, Pageable pageable) {
        return groupRepository.findAllByRealmIdIn(realmIds, pageable);
    }

    @Override
    public Group assignAuthority(String groupId, String authorityId) {
        Group group = findById(groupId);
        group.addAuthority(authorityService.getOne(authorityId));
        return save(group);
    }

    @Override
<<<<<<< HEAD
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
    public List<Group> getGroupParentGroupsById(String groupId) {
        Group group = this.findById(groupId);
        return this.getGroupParentGroups(group);
    }

    @Override
    public List<Group> getGroupParentGroups(Group group) {
        if (group.getGroupIds() == null || group.getGroupIds().isEmpty()) {
            return new ArrayList<>();
        }
        return this.findByIds(group.getGroupIds(), Pageable.unpaged()).toList();
    }

    @Override
    public List<Group> getGroupSubgroupsById(String groupId) {
        Group group = this.findById(groupId);
        return this.getGroupSubgroups(group);
    }

    @Override
    public List<Group> getGroupSubgroups(Group group) {
        if (group.getSubgroupIds() == null || group.getSubgroupIds().isEmpty()) {
            return new ArrayList<>();
        }
        return this.findByIds(group.getSubgroupIds(), Pageable.unpaged()).toList();
    }

    @Override
    public List<String> getGroupsOwnerEmails(Collection<String> groupIds) {
        return this.findAllByIds(groupIds).stream().map(this::getGroupOwnerEmail).collect(Collectors.toList());
=======
    public Page<Group> findByIds(Collection<String> ids, Pageable pageable) {
        return groupRepository.findAllByIdIn(ids, pageable);
    }

    @Override
    public Page<String> getGroupsOwnerEmails(Collection<String> groupIds, Pageable pageable) {
        return this.findByIds(groupIds, pageable).map(this::getGroupOwnerEmail);
>>>>>>> NAE-2122
    }

    @Override
    public String getGroupOwnerEmail(String groupId) {
        return this.getGroupOwnerEmail(findById(groupId));
    }

    protected String getGroupOwnerEmail(Group groupCase) {
        return userService.findById(groupCase.getOwnerId(), groupCase.getRealmId()).getEmail();
    }
}
