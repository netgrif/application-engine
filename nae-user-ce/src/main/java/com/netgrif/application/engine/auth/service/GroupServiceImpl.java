package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.provider.CollectionNameProvider;
import com.netgrif.application.engine.auth.repository.GroupRepository;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.common.ResourceNotFoundException;
import com.netgrif.application.engine.objects.common.ResourceNotFoundExceptionCode;
import com.netgrif.application.engine.objects.dto.request.group.GroupSearchRequestDto;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    private MongoTemplate mongoTemplate;

    private ProcessRoleService processRoleService;

    @Autowired
    public void setCollectionNameProvider(CollectionNameProvider collectionNameProvider) {
        this.collectionNameProvider = collectionNameProvider;
    }

    @Autowired
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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

    @Lazy
    @Autowired
    public void setProcessRoleService(ProcessRoleService processRoleService) {
        this.processRoleService = processRoleService;
    }

    @Override
    public void delete(Group group) {
        if (!groupRepository.existsById(group.getStringId())) {
            log.error("Group [{}] does not exist", group.getStringId());
            throw new IllegalArgumentException("Group " + group.getStringId() + " does not exist");
        }
        if (group.getMemberIds() != null) {
            Pageable pageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
            Page<AbstractUser> members;
            do {
                members = userService.findAllByIds(group.getMemberIds(), group.getRealmId(), pageable);
                log.debug("Removing group [{}] from members [{}]", group.getStringId(), members);
                members.forEach(user -> {
                    user.removeGroupId(group.getStringId());
                    userService.saveUser(user);
                });
                pageable = pageable.next();
            } while (members.hasNext());
        }
        if (group.getSubgroupIds() != null) {
            log.debug("Removing group [{}] from child groups [{}]", group.getStringId(), group.getSubgroupIds());
            List<Group> subGroups = findAllByIds(group.getSubgroupIds(), Pageable.unpaged()).stream().toList();
            subGroups.forEach(subgroup -> {
                subgroup.removeGroupId(group.getStringId());
                save(subgroup);
            });
        }
        if (group.getGroupIds() != null) {
            log.debug("Removing group [{}] from parent groups [{}]", group.getStringId(), group.getGroupIds());
            List<Group> groups = findAllByIds(group.getGroupIds(), Pageable.unpaged()).stream().toList();
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
        if (groupRepository.existsById(group.getStringId())) {
            log.info("Updating group: [{}]", group.getIdentifier());
        } else {
            log.info("Saving new group: [{}]", group.getIdentifier());
        }
        group.setModifiedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    @Override
    public void removeAllByRealmId(String realmId) {
        this.groupRepository.removeAllByRealmId(realmId);
    }

    @Override
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
    public Group create(AbstractUser groupOwner) {
        log.info("Creating default group for owner: [{}]", groupOwner.getStringId());
        Page<Group> userGroups = groupRepository.findByOwnerId(groupOwner.getStringId(), Pageable.ofSize(1));
        if (!userGroups.isEmpty() && !Objects.equals(groupOwner.getStringId(), userService.getSystem().getStringId())) {
            throw new IllegalArgumentException("Default group for owner [%s] already exists.".formatted(groupOwner.getUsername()));
        }
        return create(groupOwner.getUsername(), groupOwner.getName(), groupOwner);
    }

    @Override
    public Group create(String identifier, String title, AbstractUser groupOwner) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Group identifier cannot be null or blank.");
        }
        if (groupRepository.existsByIdentifier(identifier)) {
            throw new IllegalArgumentException("Group with identifier [%s] already exists.".formatted(identifier));
        }
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
    public Group getDefaultUserGroup(AbstractUser user) {
        String errorMessage = "Default user group for user [%s] does not exist.".formatted(user.getUsername());
        // TODO: optimize - use ownerId + groupIdentifier == username (no need for iteration)
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
    public void addUserToDefaultSystemGroup(AbstractUser user) {
        log.info("Adding user [{}] to default group", user.getStringId());
        addUser(getDefaultSystemGroup(), user);
    }

    @Override
    public Group assignUsersToGroup(String groupId, Set<String> userIds) {
        Group group = this.findById(groupId);
        Set<String> currentGroupMemberIds = group.getMemberIds();

        Set<String> removableMemberIds = new HashSet<>(currentGroupMemberIds);
        removableMemberIds.removeAll(userIds);

        Set<String> newMemberIds = new HashSet<>(userIds);
        newMemberIds.removeAll(currentGroupMemberIds);

        removableMemberIds.forEach(toBeRemovedId -> removeUser(group, userService.findById(toBeRemovedId, group.getRealmId())));
        newMemberIds.forEach(toBeAddedId -> addUser(group, userService.findById(toBeAddedId, group.getRealmId())));
        return group;
    }

    @Override
    public Group addUser(String groupId, String userId, String realmId) {
        return addUser(groupId, userService.findById(userId, realmId));
    }

    @Override
    public Group addUser(Group group, String userId, String realmId) {
        AbstractUser user = userService.findById(userId, realmId);
        return addUser(group, user);
    }

    @Override
    public Group addUser(String groupId, AbstractUser user) {
        Group group = findById(groupId);
        return addUser(group, user);
    }

    @Override
    public Group addUser(Group group, AbstractUser user) {
        Assert.notNull(user, "User cannot be null");
        Assert.notNull(group, "Group cannot be null");

        log.info("Adding user [{}] to group [{}]", user.getStringId(), group.getStringId());
        user.addGroupId(group.getStringId());
        group.addMemberId(user.getStringId());
        userService.saveUser(user, user.getRealmId());
        return save(group);
    }

    @Override
    public Group removeUser(String userId, String groupId, String realmId) {
        return removeUser(groupId, userService.findById(userId, realmId));
    }

    @Override
    public Group removeUser(String groupId, AbstractUser user) {
        Group group = findById(groupId);
        return removeUser(group, user);
    }

    @Override
    public Group removeUser(Group group, AbstractUser user) {
        Assert.notNull(user, "User cannot be null");
        Assert.notNull(group, "Group cannot be null");

        log.info("Removing user [{}] from group [{}]", user.getStringId(), group.getStringId());
        user.removeGroupId(group.getStringId());
        group.removeMemberId(user.getStringId());
        userService.saveUser(user, user.getRealmId());
        return save(group);
    }

    @Override
    public Page<Group> findByPredicate(Predicate predicate, Pageable pageable) {
        return groupRepository.findAll(predicate, pageable);
    }

    @Override
    public Page<Group> findByQuery(Query query, Pageable pageable) {
        return groupRepository.findAll(query, mongoTemplate, pageable);
    }

    @Override
    public Page<Group> findAllByIds(Collection<String> ids, Pageable pageable) {
        return groupRepository.findAllByIdIn(ids, pageable);
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
    public Group addAuthority(String groupId, String authorityId) {
        Group group = findById(groupId);
        Authority authority = authorityService.getOne(authorityId);
        return addAuthority(group, authority);
    }

    @Override
    public Group addAuthority(Group group, Authority authority) {
        Assert.notNull(group, "Group cannot be null");
        Assert.notNull(authority, "Authority cannot be null");
        group.addAuthority(authority);
        return save(group);
    }

    @Override
    public Group removeAuthority(String groupId, String authorityId) {
        Group group = findById(groupId);
        Authority authority = authorityService.getOne(authorityId);
        return removeAuthority(group, authority);
    }

    @Override
    public Group removeAuthority(Group group, Authority authority) {
        Assert.notNull(group, "Group cannot be null");
        Assert.notNull(authority, "Authority cannot be null");
        group.removeAuthority(authority);
        return save(group);
    }

    @Override
    public Group assignSubgroups(String parentGroupId, Set<String> childGroupIds) {
        Group parentGroup = this.findById(parentGroupId);
        Set<String> currentSubgroupIds = parentGroup.getSubgroupIds();

        Set<String> removableGroupIds = new HashSet<>(currentSubgroupIds);
        removableGroupIds.removeAll(childGroupIds);

        Set<String> newSubgroupIds = new HashSet<>(childGroupIds);
        newSubgroupIds.removeAll(currentSubgroupIds);

        removableGroupIds.forEach(toBeRemovedId -> removeSubgroup(parentGroupId, toBeRemovedId));
        newSubgroupIds.forEach(toBeAddedId -> addSubgroup(parentGroupId, toBeAddedId));
        return parentGroup;
    }

    @Override
    public Pair<Group, Group> addSubgroup(String parentGroupId, String childGroupId) {
        if (parentGroupId.equals(childGroupId)) {
            throw new IllegalArgumentException("Trying to add group to itself [%s]!".formatted(parentGroupId));
        }
        Group parentGroup = this.findById(parentGroupId);
        Group childGroup = this.findById(childGroupId);
        return this.addSubgroup(parentGroup, childGroup);
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
        Assert.notNull(parentGroup, "Parent group cannot be null");
        Assert.notNull(childGroup, "Child group cannot be null");

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
    public Pair<Group, Group> removeSubgroup(String parentGroupId, String childGroupId) {
        if (parentGroupId.equals(childGroupId)) {
            throw new IllegalArgumentException("Trying to remove group from itself [%s]!".formatted(parentGroupId));
        }
        Group parentGroup = this.findById(parentGroupId);
        Group childGroup = this.findById(childGroupId);
        return this.removeSubgroup(parentGroup, childGroup);
    }

    @Override
    public Pair<Group, Group> removeSubgroup(Group parentGroup, String childGroupId) {
        if (parentGroup.getStringId().equals(childGroupId)) {
            throw new IllegalArgumentException("Trying to remove group from itself [%s]!".formatted(parentGroup.getStringId()));
        }
        Group childGroup = this.findById(childGroupId);
        return this.removeSubgroup(parentGroup, childGroup);
    }

    @Override
    public Pair<Group, Group> removeSubgroup(String parentGroupId, Group childGroup) {
        if (childGroup.getStringId().equals(parentGroupId)) {
            throw new IllegalArgumentException("Trying to remove group from itself [%s]!".formatted(childGroup.getStringId()));
        }
        Group parentGroup = this.findById(parentGroupId);
        return this.removeSubgroup(parentGroup, childGroup);
    }

    @Override
    public Pair<Group, Group> removeSubgroup(Group parentGroup, Group childGroup) {
        Assert.notNull(parentGroup, "Parent group cannot be null");
        Assert.notNull(childGroup, "Child group cannot be null");

        if (parentGroup.getStringId().equals(childGroup.getStringId())) {
            throw new IllegalArgumentException("Trying to remove group from itself [%s]!".formatted(parentGroup.getStringId()));
        }
        parentGroup.removeSubgroupId(childGroup.getStringId());
        childGroup.removeGroupId(parentGroup.getStringId());
        log.info("Removing group [{}] from parent group [{}]", childGroup.getStringId(), parentGroup.getStringId());
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
        return this.findAllByIds(group.getGroupIds(), Pageable.unpaged()).toList();
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
        return this.findAllByIds(group.getSubgroupIds(), Pageable.unpaged()).toList();
    }

    @Override
    public List<String> getGroupsOwnerEmails(Collection<String> groupIds) {
        return this.findAllByIds(groupIds, Pageable.unpaged())
                .stream()
                .map(this::getGroupOwnerEmail)
                .collect(Collectors.toList());
    }

    @Override
    public String getGroupOwnerEmail(String groupId) {
        return this.getGroupOwnerEmail(findById(groupId));
    }

    @Override
    public Page<Group> search(GroupSearchRequestDto searchDto, Pageable pageable) {
        List<Criteria> filters = new ArrayList<>();
        if (searchDto != null && searchDto.ids() != null) {
            Criteria criteria = Criteria.where("_id").in(searchDto.ids());
            filters.add(criteria);
        }
        if (searchDto != null && searchDto.fullText() != null && !searchDto.fullText().isBlank()) {
            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("identifier").regex(searchDto.fullText(), "i"),
                    Criteria.where("displayName").regex(searchDto.fullText(), "i"),
                    Criteria.where("ownerUsername").regex(searchDto.fullText(), "i")
            );
            filters.add(criteria);
        }
        if (searchDto != null && searchDto.realmId() != null && !searchDto.realmId().isBlank())  {
            filters.add(Criteria.where("realmId").regex(searchDto.realmId(), "i"));
        }
        Query query = Query.query(filters.isEmpty() ? new Criteria() : new Criteria().andOperator(filters.toArray(new Criteria[0])));
        long count = mongoTemplate.count(query, Group.class);
        List<Group> groups = mongoTemplate.find(query.with(pageable), Group.class);
        return new PageImpl<>(groups, pageable, count);
    }

    @Override
    public Group addRole(String groupId, String roleId) {
        Group group = findById(groupId);
        ProcessRole role = processRoleService.findById(new ProcessResourceId(roleId));
        return addRole(group, role);
    }

    @Override
    public Group addRole(Group group, ProcessRole processRole) {
        Assert.notNull(group, "Group cannot be null");
        Assert.notNull(processRole, "Process role cannot be null");
        group.addProcessRole(processRole);
        return save(group);
    }

    @Override
    public Group removeRole(String groupId, String roleId) {
        Group group = findById(groupId);
        ProcessRole role = processRoleService.findById(new ProcessResourceId(roleId));
        return removeRole(group, role);
    }

    @Override
    public Group removeRole(Group group, ProcessRole processRole) {
        Assert.notNull(group, "Group cannot be null");
        Assert.notNull(processRole, "Process role cannot be null");

        group.removeProcessRole(processRole);
        return save(group);
    }

    protected String getGroupOwnerEmail(Group groupCase) {
        return userService.findById(groupCase.getOwnerId(), groupCase.getRealmId()).getEmail();
    }
}
