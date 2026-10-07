package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.Group;
import com.netgrif.application.engine.adapter.spring.auth.domain.User;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@SpringBootTest
class DefaultLoggedUserFactoryTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private DefaultLoggedUserFactory factory;

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
        factory = new DefaultLoggedUserFactory(groupService, processRoleService, authorityService);
    }

    @Test
    void testCreateReturnsLoggedUser() {
        LoggedUser loggedUser = factory.create();
        assertNotNull(loggedUser);
    }

    @Test
    void testResolveProcessRolesDirectAndNestedWithCycles() {
        User user = new User();
        String userRoleId = new ProcessResourceId().getFullId();
        user.setProcessRoleIds(Set.of(userRoleId));

        String group1Id = new ObjectId().toString();
        String group2Id = new ObjectId().toString();
        user.setGroupIds(Set.of(group1Id));

        Group group1 = new Group(new ObjectId(group1Id));
        group1.setRealmId("realm");
        group1.setIdentifier(group1Id);
        String group1RoleId = new ProcessResourceId().getFullId();
        group1.setProcessRoleIds(Set.of(group1RoleId));
        group1.setGroupIds(Set.of(group2Id));

        Group group2 = new Group(new ObjectId(group2Id));
        group2.setRealmId("realm");
        group2.setIdentifier(group2Id);
        String group2RoleId = new ProcessResourceId().getFullId();
        group2.setProcessRoleIds(Set.of(group2RoleId));
        // Cycle: group2 references group1
        group2.setGroupIds(Set.of(group1Id));

        when(groupService.findById(group1Id)).thenReturn(group1);
        when(groupService.findById(group2Id)).thenReturn(group2);

        TestProcessRole userRole = new TestProcessRole(userRoleId);
        TestProcessRole group1Role = new TestProcessRole(group1RoleId);
        when(processRoleService.findById(userRoleId)).thenReturn(userRole);
        when(processRoleService.findById(group1RoleId)).thenReturn(group1Role);
        when(processRoleService.findById(group2RoleId)).thenReturn(null); // tests null branch

        factory.resolveProcessRoles(user);

        assertTrue(user.getProcessRoleIds().contains(userRoleId));
        assertTrue(user.getProcessRoleIds().contains(group1RoleId));
        assertTrue(user.getProcessRoleIds().contains(group2RoleId));

        assertEquals(2, user.getProcessRoles().size());
        assertTrue(user.getProcessRoles().contains(userRole));
        assertTrue(user.getProcessRoles().contains(group1Role));
    }

    @Test
    void testResolveProcessRolesEmptyGroups() {
        User user = new User();
        String userRoleId = new ProcessResourceId().getFullId();
        user.setProcessRoleIds(Set.of(userRoleId));
        TestProcessRole userRole = new TestProcessRole(userRoleId);
        when(processRoleService.findById(userRoleId)).thenReturn(userRole);

        factory.resolveProcessRoles(user);

        assertEquals(1, user.getProcessRoles().size());
        assertTrue(user.getProcessRoles().contains(userRole));
        verifyNoInteractions(groupService);
    }

    @Test
    void testResolveAuthoritiesDirectAndNestedWithCycles() {
        User user = new User();
        String userAuthId = new ObjectId().toString();
        user.setAuthorityIds(Set.of(userAuthId));

        String group1Id = new ObjectId().toString();
        String group2Id = new ObjectId().toString();
        user.setGroupIds(Set.of(group1Id));

        Group group1 = new Group(new ObjectId(group1Id));
        group1.setRealmId("realm");
        group1.setIdentifier(group1Id);
        String group1AuthId = new ObjectId().toString();
        group1.setAuthorityIds(Set.of(group1AuthId));
        group1.setGroupIds(Set.of(group2Id));

        Group group2 = new Group(new ObjectId(group2Id));
        group2.setRealmId("realm");
        group2.setIdentifier(group2Id);
        String group2AuthId = new ObjectId().toString();
        group2.setAuthorityIds(Set.of(group2AuthId));
        // Cycle: group2 references group1
        group2.setGroupIds(Set.of(group1Id));

        when(groupService.findById(group1Id)).thenReturn(group1);
        when(groupService.findById(group2Id)).thenReturn(group2);

        TestAuthority userAuth = new TestAuthority(userAuthId, "ROLE_USER");
        TestAuthority group1Auth = new TestAuthority(group1AuthId, "ROLE_GROUP1");
        TestAuthority group2Auth = new TestAuthority(group2AuthId, "ROLE_GROUP2");

        when(authorityService.getOne(userAuthId)).thenReturn(userAuth);
        when(authorityService.getOne(group1AuthId)).thenReturn(group1Auth);
        when(authorityService.getOne(group2AuthId)).thenReturn(group2Auth);

        factory.resolveAuthorities(user);

        assertTrue(user.getAuthorityIds().contains(userAuthId));
        assertTrue(user.getAuthorityIds().contains(group1AuthId));
        assertTrue(user.getAuthorityIds().contains(group2AuthId));

        assertEquals(3, user.getAuthoritySet().size());
        assertTrue(user.getAuthoritySet().contains(userAuth));
        assertTrue(user.getAuthoritySet().contains(group1Auth));
        assertTrue(user.getAuthoritySet().contains(group2Auth));
    }

    @Test
    void testResolveAuthoritiesEmptyGroups() {
        User user = new User();
        String userAuthId = new ObjectId().toString();
        user.setAuthorityIds(Set.of(userAuthId));

        TestAuthority userAuth = new TestAuthority(userAuthId, "ROLE_USER");
        when(authorityService.getOne(userAuthId)).thenReturn(userAuth);

        factory.resolveAuthorities(user);

        assertEquals(1, user.getAuthoritySet().size());
        assertTrue(user.getAuthoritySet().contains(userAuth));
        verifyNoInteractions(groupService);
    }
}
