package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.Group;
import com.netgrif.application.engine.adapter.spring.auth.domain.User;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.provider.CollectionNameProvider;
import com.netgrif.application.engine.auth.repository.GroupRepository;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.common.ResourceNotFoundException;
import com.netgrif.application.engine.objects.dto.request.group.GroupSearchRequestDto;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.querydsl.core.types.Predicate;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.Pair;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private CollectionNameProvider collectionNameProvider;

    @Mock
    private UserService userService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private GroupConfigurationProperties groupConfigurationProperties;

    @Mock
    private PaginationProperties paginationProperties;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ProcessRoleService processRoleService;

    private GroupServiceImpl service;

    static class TestAuthority extends Authority {
        private final ObjectId id;

        public TestAuthority(String name) {
            super(name);
            this.id = new ObjectId();
        }

        public TestAuthority(String id, String name) {
            super(name);
            this.id = new ObjectId(id);
        }

        @Override
        public ObjectId get_id() {
            return id;
        }

        @Override
        public String getStringId() {
            return id.toString();
        }
    }

    static class TestProcessRole extends ProcessRole {
        public TestProcessRole(String id) {
            super(id);
        }
    }

    @BeforeEach
    void setUp() {
        service = new GroupServiceImpl();
        service.setCollectionNameProvider(collectionNameProvider);
        service.setUserService(userService);
        service.setGroupRepository(groupRepository);
        service.setAuthorityService(authorityService);
        service.setGroupConfigurationProperties(groupConfigurationProperties);
        service.setPaginationProperties(paginationProperties);
        service.setMongoTemplate(mongoTemplate);
        service.setProcessRoleService(processRoleService);
    }

    @Test
    void testGetters() {
        assertSame(collectionNameProvider, service.getCollectionNameProvider());
        assertSame(userService, service.getUserService());
        assertSame(groupRepository, service.getGroupRepository());
        assertSame(authorityService, service.getAuthorityService());
        assertSame(groupConfigurationProperties, service.getGroupConfigurationProperties());
        assertSame(paginationProperties, service.getPaginationProperties());
        assertSame(mongoTemplate, service.getMongoTemplate());
        assertSame(processRoleService, service.getProcessRoleService());
    }

    @Test
    void testSave() {
        Group group = new Group(new ObjectId());
        group.setIdentifier("grp1");

        when(groupRepository.existsById(group.getStringId())).thenReturn(false);
        when(groupRepository.save(group)).thenReturn(group);

        com.netgrif.application.engine.objects.auth.domain.Group saved = service.save(group);
        assertSame(group, saved);
        assertNotNull(group.getModifiedAt());

        when(groupRepository.existsById(group.getStringId())).thenReturn(true);
        saved = service.save(group);
        assertSame(group, saved);
    }

    @Test
    void testDeleteThrowsWhenGroupDoesNotExist() {
        Group group = new Group(new ObjectId());
        when(groupRepository.existsById(group.getStringId())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.delete(group));
    }

    @Test
    void testDeleteCascadesMembersSubgroupsAndParentGroups() {
        Group group = new Group(new ObjectId());
        group.setRealmId("realm-1");
        group.setIdentifier("main-grp");

        String memberId = new ObjectId().toString();
        group.addMemberId(memberId);

        String subgroupId = new ObjectId().toString();
        group.addSubGroupId(subgroupId);

        String parentGroupId = new ObjectId().toString();
        group.addGroupId(parentGroupId);

        when(groupRepository.existsById(group.getStringId())).thenReturn(true);
        when(paginationProperties.getBackendPageSize()).thenReturn(10);

        User member = new User();
        member.addGroupId(group.getStringId());
        Page<AbstractUser> memberPage = new PageImpl<>(List.of(member), PageRequest.of(0, 10), 1);
        when(userService.findAllByIds(eq(group.getMemberIds()), eq("realm-1"), any(Pageable.class))).thenReturn(memberPage);

        Group subgroup = new Group(new ObjectId(subgroupId));
        subgroup.addGroupId(group.getStringId());
        when(groupRepository.findAllByIdIn(eq(group.getSubgroupIds()), eq(Pageable.unpaged()))).thenReturn(new PageImpl<>(List.of(subgroup)));

        Group parentGroup = new Group(new ObjectId(parentGroupId));
        parentGroup.addSubGroupId(group.getStringId());
        when(groupRepository.findAllByIdIn(eq(group.getGroupIds()), eq(Pageable.unpaged()))).thenReturn(new PageImpl<>(List.of(parentGroup)));

        service.delete(group);

        verify(userService).saveUser(member);
        assertFalse(member.getGroupIds().contains(group.getStringId()));

        verify(groupRepository).save(subgroup);
        assertFalse(subgroup.getGroupIds().contains(group.getStringId()));

        verify(groupRepository).save(parentGroup);
        assertFalse(parentGroup.getSubgroupIds().contains(group.getStringId()));

        verify(groupRepository).delete(group);
    }

    @Test
    void testDeleteWithEmptyRelations() {
        Group group = new Group(new ObjectId());
        when(groupRepository.existsById(group.getStringId())).thenReturn(true);

        service.delete(group);

        verify(groupRepository).delete(group);
        verifyNoInteractions(userService);
    }

    @Test
    void testRemoveAllByRealmId() {
        when(paginationProperties.getBackendPageSize()).thenReturn(10);
        Group group = new Group(new ObjectId());
        when(groupRepository.existsById(group.getStringId())).thenReturn(true);

        Page<com.netgrif.application.engine.objects.auth.domain.Group> page1 = new PageImpl<>(List.of(group));
        Page<com.netgrif.application.engine.objects.auth.domain.Group> page2 = new PageImpl<>(List.of());
        when(groupRepository.findAllByRealmId("realm-1", PageRequest.of(0, 10)))
                .thenReturn(page1)
                .thenReturn(page2);

        service.removeAllByRealmId("realm-1");

        verify(groupRepository).delete(group);
    }

    @Test
    void testRemoveAllByRealmIdIn() {
        when(paginationProperties.getBackendPageSize()).thenReturn(10);
        Group group = new Group(new ObjectId());
        when(groupRepository.existsById(group.getStringId())).thenReturn(true);

        // When realmIds is null or empty, delegates to removeAllGroups()
        Page<com.netgrif.application.engine.objects.auth.domain.Group> pageAll1 = new PageImpl<>(List.of(group));
        Page<com.netgrif.application.engine.objects.auth.domain.Group> pageAll2 = new PageImpl<>(List.of());
        when(groupRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(pageAll1)
                .thenReturn(pageAll2);

        service.removeAllByRealmIdIn(null);
        verify(groupRepository).delete(group);

        // When realmIds has items
        Page<com.netgrif.application.engine.objects.auth.domain.Group> pageIn1 = new PageImpl<>(List.of(group));
        Page<com.netgrif.application.engine.objects.auth.domain.Group> pageIn2 = new PageImpl<>(List.of());
        when(groupRepository.findAllByRealmIdIn(eq(Set.of("realm-1")), eq(PageRequest.of(0, 10))))
                .thenReturn(pageIn1)
                .thenReturn(pageIn2);

        service.removeAllByRealmIdIn(Set.of("realm-1"));
        verify(groupRepository, times(2)).delete(group);
    }

    @Test
    void testFindById() {
        Group group = new Group(new ObjectId());
        when(groupRepository.findById(group.getStringId())).thenReturn(Optional.of(group));

        com.netgrif.application.engine.objects.auth.domain.Group found = service.findById(group.getStringId());
        assertSame(group, found);

        when(groupRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.findById("unknown"));
    }

    @Test
    void testFindByIdentifier() {
        Group group = new Group(new ObjectId());
        when(groupRepository.findByIdentifier("ident")).thenReturn(Optional.of(group));

        Optional<com.netgrif.application.engine.objects.auth.domain.Group> result = service.findByIdentifier("ident");
        assertTrue(result.isPresent());
        assertSame(group, result.get());
    }

    @Test
    void testGetDefaultSystemGroup() {
        when(groupConfigurationProperties.getDefaultGroupIdentifier()).thenReturn("DEFAULT_SYSTEM_GRP");
        when(groupConfigurationProperties.getDefaultGroupTitle()).thenReturn("Default System Group");

        User systemUser = new User();
        systemUser.setUsername("system");
        systemUser.setRealmId("default");
        when(userService.getSystem()).thenReturn(systemUser);

        when(groupRepository.existsByIdentifier("DEFAULT_SYSTEM_GRP")).thenReturn(false, false, true);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.netgrif.application.engine.objects.auth.domain.Group group = service.getDefaultSystemGroup();
        assertNotNull(group);
        assertEquals("DEFAULT_SYSTEM_GRP", group.getIdentifier());

        // When already exists and cached
        assertSame(group, service.getDefaultSystemGroup());

        // When exists in repo but defaultSystemGroup is not yet cached in field
        GroupServiceImpl freshService = new GroupServiceImpl();
        freshService.setGroupRepository(groupRepository);
        freshService.setGroupConfigurationProperties(groupConfigurationProperties);

        Group existingGroup = new Group("DEFAULT_SYSTEM_GRP", "default");
        when(groupRepository.existsByIdentifier("DEFAULT_SYSTEM_GRP")).thenReturn(true);
        when(groupRepository.findByIdentifier("DEFAULT_SYSTEM_GRP")).thenReturn(Optional.of(existingGroup));

        com.netgrif.application.engine.objects.auth.domain.Group resolved = freshService.getDefaultSystemGroup();
        assertSame(existingGroup, resolved);

        // When existsByIdentifier is true but findByIdentifier returns empty
        GroupServiceImpl errorService = new GroupServiceImpl();
        errorService.setGroupRepository(groupRepository);
        errorService.setGroupConfigurationProperties(groupConfigurationProperties);
        when(groupRepository.findByIdentifier("DEFAULT_SYSTEM_GRP")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, errorService::getDefaultSystemGroup);
    }

    @Test
    void testCreateWithGroupOwner() {
        User owner = new User();
        owner.setUsername("owner1");
        owner.setRealmId("realm-1");

        User systemUser = new User();
        systemUser.setUsername("system");
        when(userService.getSystem()).thenReturn(systemUser);

        when(groupRepository.findByOwnerId(owner.getStringId(), Pageable.ofSize(1)))
                .thenReturn(new PageImpl<>(List.of(new Group("grp1", "realm-1"))));

        assertThrows(IllegalArgumentException.class, () -> service.create(owner));

        // When no existing groups for owner
        when(groupRepository.findByOwnerId(owner.getStringId(), Pageable.ofSize(1)))
                .thenReturn(new PageImpl<>(List.of()));
        when(groupRepository.existsByIdentifier(owner.getUsername())).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.netgrif.application.engine.objects.auth.domain.Group created = service.create(owner);
        assertNotNull(created);
        assertEquals("owner1", created.getIdentifier());
        verify(userService).saveUser(owner, "realm-1");
    }

    @Test
    void testCreateWithIdentifierTitleAndOwner() {
        User owner = new User();
        owner.setUsername("owner1");
        owner.setRealmId("realm-1");

        assertThrows(IllegalArgumentException.class, () -> service.create(null, "Title", owner));
        assertThrows(IllegalArgumentException.class, () -> service.create("   ", "Title", owner));

        when(groupRepository.existsByIdentifier("IDENT")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create("IDENT", "Title", owner));

        when(groupRepository.existsByIdentifier("IDENT")).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.netgrif.application.engine.objects.auth.domain.Group created = service.create("IDENT", "Title", owner);
        assertEquals("IDENT", created.getIdentifier());
        assertEquals("Title", created.getDisplayName());
        assertEquals(owner.getStringId(), created.getOwnerId());
        assertTrue(created.getMemberIds().contains(owner.getStringId()));
        assertTrue(owner.getGroupIds().contains(created.getStringId()));
    }

    @Test
    void testGetDefaultUserGroup() {
        User user = new User();
        user.setUsername("john");

        when(paginationProperties.getBackendPageSize()).thenReturn(10);

        Group matchGroup = new Group("john", "realm-1");
        Group otherGroup = new Group("other", "realm-1");

        Page<com.netgrif.application.engine.objects.auth.domain.Group> page = new PageImpl<>(List.of(otherGroup, matchGroup));
        when(groupRepository.findByOwnerId(user.getStringId(), PageRequest.of(0, 10))).thenReturn(page);

        com.netgrif.application.engine.objects.auth.domain.Group result = service.getDefaultUserGroup(user);
        assertSame(matchGroup, result);

        // When not found
        Page<com.netgrif.application.engine.objects.auth.domain.Group> emptyPage = new PageImpl<>(List.of(otherGroup));
        when(groupRepository.findByOwnerId(user.getStringId(), PageRequest.of(0, 10))).thenReturn(emptyPage);

        assertThrows(ResourceNotFoundException.class, () -> service.getDefaultUserGroup(user));
    }

    @Test
    void testAddUserToDefaultSystemGroup() {
        when(groupConfigurationProperties.getDefaultGroupIdentifier()).thenReturn("DEFAULT_SYSTEM_GRP");
        Group defaultGroup = new Group("DEFAULT_SYSTEM_GRP", "default");
        when(groupRepository.existsByIdentifier("DEFAULT_SYSTEM_GRP")).thenReturn(true);
        when(groupRepository.findByIdentifier("DEFAULT_SYSTEM_GRP")).thenReturn(Optional.of(defaultGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = new User();
        user.setRealmId("default");

        service.addUserToDefaultSystemGroup(user);

        assertTrue(defaultGroup.getMemberIds().contains(user.getStringId()));
        assertTrue(user.getGroupIds().contains(defaultGroup.getStringId()));
        verify(userService).saveUser(user, "default");
    }

    @Test
    void testAssignUsersToGroup() {
        Group group = new Group(new ObjectId());
        group.setRealmId("realm-1");
        String currentUserId = new ObjectId().toString();
        group.addMemberId(currentUserId);

        String newUserId = new ObjectId().toString();

        when(groupRepository.findById(group.getStringId())).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        User currentUser = new User(new ObjectId(currentUserId));
        currentUser.setRealmId("realm-1");
        currentUser.addGroupId(group.getStringId());

        User newUser = new User(new ObjectId(newUserId));
        newUser.setRealmId("realm-1");

        when(userService.findById(currentUserId, "realm-1")).thenReturn(currentUser);
        when(userService.findById(newUserId, "realm-1")).thenReturn(newUser);

        com.netgrif.application.engine.objects.auth.domain.Group updated = service.assignUsersToGroup(group.getStringId(), Set.of(newUserId));

        assertFalse(updated.getMemberIds().contains(currentUserId));
        assertTrue(updated.getMemberIds().contains(newUserId));

        // Test with null userIds
        service.assignUsersToGroup(group.getStringId(), null);
    }

    @Test
    void testAddUserAndRemoveUserOverloads() {
        Group group = new Group(new ObjectId());
        group.setRealmId("realm-1");
        when(groupRepository.findById(group.getStringId())).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        User user = new User();
        user.setRealmId("realm-1");
        when(userService.findById(user.getStringId(), "realm-1")).thenReturn(user);

        // addUser overloads
        service.addUser(group.getStringId(), user.getStringId(), "realm-1");
        assertTrue(group.getMemberIds().contains(user.getStringId()));

        service.addUser(group, user.getStringId(), "realm-1");
        service.addUser(group.getStringId(), user);
        service.addUser(group, user);

        // removeUser overloads
        service.removeUser(group.getStringId(), user.getStringId(), "realm-1");
        service.removeUser(group.getStringId(), user);
        service.removeUser(group, user);
        assertFalse(group.getMemberIds().contains(user.getStringId()));
    }

    @Test
    void testFindDelegates() {
        Pageable pageable = PageRequest.of(0, 10);
        Predicate predicate = mock(Predicate.class);
        Query query = new Query();

        Page<com.netgrif.application.engine.objects.auth.domain.Group> page = new PageImpl<>(List.of());

        when(groupRepository.findAll(predicate, pageable)).thenReturn(page);
        assertSame(page, service.findByPredicate(predicate, pageable));

        when(groupRepository.findAll(query, mongoTemplate, pageable)).thenReturn(page);
        assertSame(page, service.findByQuery(query, pageable));

        when(groupRepository.findAllByIdIn(Set.of("g1"), pageable)).thenReturn(page);
        assertSame(page, service.findAllByIds(Set.of("g1"), pageable));

        when(groupRepository.findAll(pageable)).thenReturn(page);
        assertSame(page, service.findAll(pageable));

        when(groupRepository.findAllByRealmId("r1", pageable)).thenReturn(page);
        assertSame(page, service.findAllFromRealm("r1", pageable));

        when(groupRepository.findAllByRealmIdIn(Set.of("r1"), pageable)).thenReturn(page);
        assertSame(page, service.findAllFromRealmIn(Set.of("r1"), pageable));

        ProcessResourceId roleId = new ProcessResourceId();
        when(groupRepository.findAllByProcessRoles__idIn(Set.of(roleId), pageable)).thenReturn(page);
        assertSame(page, service.findAllByProcessRoles(Set.of(roleId), pageable));
    }

    @Test
    void testAuthorityOperations() {
        Group group = new Group(new ObjectId());
        when(groupRepository.findById(group.getStringId())).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        String currentAuthId = new ObjectId().toString();
        TestAuthority currentAuth = new TestAuthority(currentAuthId, "CURRENT");
        group.addAuthority(currentAuth);

        String newAuthId = new ObjectId().toString();
        TestAuthority newAuth = new TestAuthority(newAuthId, "NEW");

        when(authorityService.getOne(currentAuthId)).thenReturn(currentAuth);
        when(authorityService.getOne(newAuthId)).thenReturn(newAuth);

        // assignAuthorities null branch
        assertSame(group, service.assignAuthorities(group.getStringId(), null));

        // assignAuthorities diff
        com.netgrif.application.engine.objects.auth.domain.Group updated = service.assignAuthorities(group.getStringId(), Set.of(newAuthId));
        assertTrue(updated.getAuthorityIds().contains(newAuthId));
        assertFalse(updated.getAuthorityIds().contains(currentAuthId));

        // addAuthority / removeAuthority overloads
        service.addAuthority(group.getStringId(), currentAuthId);
        assertTrue(group.getAuthorityIds().contains(currentAuthId));

        service.removeAuthority(group.getStringId(), currentAuthId);
        assertFalse(group.getAuthorityIds().contains(currentAuthId));
    }

    @Test
    void testSubgroupOperations() {
        Group parent = new Group(new ObjectId());
        parent.setRealmId("realm-1");
        Group child = new Group(new ObjectId());
        child.setRealmId("realm-1");

        when(groupRepository.findById(parent.getStringId())).thenReturn(Optional.of(parent));
        when(groupRepository.findById(child.getStringId())).thenReturn(Optional.of(child));
        when(groupRepository.save(parent)).thenReturn(parent);
        when(groupRepository.save(child)).thenReturn(child);

        // Self addition throws
        assertThrows(IllegalArgumentException.class, () -> service.addSubgroup(parent.getStringId(), parent.getStringId()));
        assertThrows(IllegalArgumentException.class, () -> service.addSubgroup(parent, parent.getStringId()));
        assertThrows(IllegalArgumentException.class, () -> service.addSubgroup(parent.getStringId(), parent));
        assertThrows(IllegalArgumentException.class, () -> service.addSubgroup(parent, parent));

        // Realm mismatch throws
        Group diffRealmChild = new Group(new ObjectId());
        diffRealmChild.setRealmId("realm-2");
        assertThrows(IllegalArgumentException.class, () -> service.addSubgroup(parent, diffRealmChild));

        // Successful addSubgroup overloads
        Pair<com.netgrif.application.engine.objects.auth.domain.Group, com.netgrif.application.engine.objects.auth.domain.Group> pair =
                service.addSubgroup(parent.getStringId(), child.getStringId());
        assertNotNull(pair);
        assertTrue(parent.getSubgroupIds().contains(child.getStringId()));
        assertTrue(child.getGroupIds().contains(parent.getStringId()));

        service.addSubgroup(parent, child.getStringId());
        service.addSubgroup(parent.getStringId(), child);

        // assignSubgroups
        assertSame(parent, service.assignSubgroups(parent.getStringId(), null));
        String newChildId = new ObjectId().toString();
        Group newChild = new Group(new ObjectId(newChildId));
        newChild.setRealmId("realm-1");
        when(groupRepository.findById(newChildId)).thenReturn(Optional.of(newChild));

        service.assignSubgroups(parent.getStringId(), Set.of(newChildId));
        assertTrue(parent.getSubgroupIds().contains(newChildId));
        assertFalse(parent.getSubgroupIds().contains(child.getStringId()));

        // removeSubgroup self removal throws
        assertThrows(IllegalArgumentException.class, () -> service.removeSubgroup(parent.getStringId(), parent.getStringId()));
        assertThrows(IllegalArgumentException.class, () -> service.removeSubgroup(parent, parent.getStringId()));
        assertThrows(IllegalArgumentException.class, () -> service.removeSubgroup(parent.getStringId(), parent));
        assertThrows(IllegalArgumentException.class, () -> service.removeSubgroup(parent, parent));

        // Successful removeSubgroup overloads
        service.removeSubgroup(parent.getStringId(), newChildId);
        assertFalse(parent.getSubgroupIds().contains(newChildId));

        parent.addSubGroupId(newChildId);
        newChild.addGroupId(parent.getStringId());
        service.removeSubgroup(parent, newChildId);

        parent.addSubGroupId(newChildId);
        newChild.addGroupId(parent.getStringId());
        service.removeSubgroup(parent.getStringId(), newChild);
    }

    @Test
    void testHierarchyAndEmailQueries() {
        Group group = new Group(new ObjectId());
        group.setRealmId("realm-1");
        group.setOwnerId(new ObjectId().toString());

        String parentId = new ObjectId().toString();
        group.addGroupId(parentId);
        Group parent = new Group(new ObjectId(parentId));

        String childId = new ObjectId().toString();
        group.addSubGroupId(childId);
        Group child = new Group(new ObjectId(childId));

        when(groupRepository.findById(group.getStringId())).thenReturn(Optional.of(group));
        when(groupRepository.findAllByIdIn(eq(Set.of(parentId)), eq(Pageable.unpaged()))).thenReturn(new PageImpl<>(List.of(parent)));
        when(groupRepository.findAllByIdIn(eq(Set.of(childId)), eq(Pageable.unpaged()))).thenReturn(new PageImpl<>(List.of(child)));

        assertEquals(List.of(parent), service.getGroupParentGroupsById(group.getStringId()));
        assertEquals(List.of(child), service.getGroupSubgroupsById(group.getStringId()));

        // empty relations
        Group isolated = new Group(new ObjectId());
        assertTrue(service.getGroupParentGroups(isolated).isEmpty());
        assertTrue(service.getGroupSubgroups(isolated).isEmpty());

        // emails
        User owner = new User();
        owner.setEmail("owner@example.com");
        when(userService.findById(group.getOwnerId(), "realm-1")).thenReturn(owner);

        assertEquals("owner@example.com", service.getGroupOwnerEmail(group.getStringId()));
        when(groupRepository.findAllByIdIn(eq(Set.of(group.getStringId())), eq(Pageable.unpaged()))).thenReturn(new PageImpl<>(List.of(group)));
        assertEquals(List.of("owner@example.com"), service.getGroupsOwnerEmails(Set.of(group.getStringId())));
    }

    @Test
    void testProcessRoleOperations() {
        Group group = new Group(new ObjectId());
        when(groupRepository.findById(group.getStringId())).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        String roleCompositeId = new ProcessResourceId().getFullId();
        TestProcessRole role = new TestProcessRole(roleCompositeId);
        when(processRoleService.findById(any(ProcessResourceId.class))).thenReturn(role);

        service.addRole(group.getStringId(), roleCompositeId);
        assertTrue(group.getProcessRoleIds().contains(roleCompositeId));

        service.removeRole(group.getStringId(), roleCompositeId);
        assertFalse(group.getProcessRoleIds().contains(roleCompositeId));
    }

    @Test
    void testSearch() {
        GroupSearchRequestDto searchDto = new GroupSearchRequestDto();
        searchDto.setIds(Set.of("g1"));
        searchDto.setFullText("test");
        searchDto.setRealmId("realm-1");

        Pageable pageable = PageRequest.of(0, 10);
        when(mongoTemplate.count(any(Query.class), eq(com.netgrif.application.engine.objects.auth.domain.Group.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(com.netgrif.application.engine.objects.auth.domain.Group.class))).thenReturn(List.of(new Group("g1", "realm-1")));

        Page<com.netgrif.application.engine.objects.auth.domain.Group> result = service.search(searchDto, pageable);
        assertEquals(1, result.getTotalElements());

        // empty searchDto
        Page<com.netgrif.application.engine.objects.auth.domain.Group> emptyResult = service.search(null, pageable);
        assertNotNull(emptyResult);
    }
}

