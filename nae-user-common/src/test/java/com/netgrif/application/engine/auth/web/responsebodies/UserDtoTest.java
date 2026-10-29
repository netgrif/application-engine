package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.adapter.spring.petrinet.web.responsebodies.ProcessRole;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.dto.response.group.GroupDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    static class DomainUser extends User {
        public DomainUser() {
            super();
            this.id = new ObjectId();
            this.firstName = "John";
            this.lastName = "Doe";
            this.username = "johndoe";
            this.email = "john@example.com";
            this.realmId = "realm-1";
            this.avatar = "http://avatar.url";
        }
    }

    static class NonDomainUser extends AbstractUser {
        public NonDomainUser() {
            super();
            this.id = new ObjectId();
            this.firstName = "Jane";
            this.lastName = "Smith";
            this.username = "janesmith";
            this.email = "jane@example.com";
            this.realmId = "realm-2";
            this.avatar = "http://avatar2.url";
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public void setPassword(String password) {
        }
    }

    static class TestAuthority extends Authority {
        private final ObjectId id = new ObjectId();

        public TestAuthority(String name) {
            super(name);
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

    @Test
    void testConstructorWithDomainUserAndCredentials() {
        DomainUser user = new DomainUser();
        user.setState(UserState.ACTIVE);
        user.setEmailVerified(true);
        user.setAttribute("customAttr", "customValue", false);

        // Add credentials
        user.setCredential("password", new PasswordCredential("secret", 0, true));
        user.setCredential("token", new TokenCredential("token-123", 1, true));
        user.setCredential("disabled-cred", new StringCredential("disabled-type", "val", 2, false));
        user.setCredential("custom-cred", new Credential<String>("custom-type", "val2", 3, true) {});

        UserDto dto = new UserDto(user);

        assertEquals(user.getStringId(), dto.getId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getRealmId(), dto.getRealmId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getAvatar(), dto.getAvatar());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals(user.getFullName(), dto.getFullName());
        assertTrue(dto.isEnabled());
        assertTrue(dto.isEmailVerified());
        assertEquals(UserState.ACTIVE, dto.getState());
        assertEquals(user.getCreatedAt(), dto.getCreatedAt());

        assertNotNull(dto.getAttributes());
        assertTrue(dto.getAttributes().containsKey("customAttr"));
        assertTrue(dto.getAttributes().containsKey(UserDto.ATTR_ENABLED_CREDENTIALS));

        @SuppressWarnings("unchecked")
        Attribute<Set<String>> credAttr = (Attribute<Set<String>>) dto.getAttributes().get(UserDto.ATTR_ENABLED_CREDENTIALS);
        assertTrue(credAttr.isRequired());
        Set<String> enabledTypes = credAttr.getValue();
        assertTrue(enabledTypes.contains("password"));
        assertTrue(enabledTypes.contains("token"));
        assertFalse(enabledTypes.contains("disabled-type"));
    }

    @Test
    void testConstructorWithDomainUserNullCredentialsAndNullAttributes() {
        DomainUser user = new DomainUser();
        user.setCredentials(null);
        user.setAttributes(null);
        user.setState(UserState.INACTIVE);
        user.setEmailVerified(false);

        UserDto dto = new UserDto(user);

        assertEquals(user.getStringId(), dto.getId());
        assertFalse(dto.isEnabled());
        assertFalse(dto.isEmailVerified());
        assertEquals(UserState.INACTIVE, dto.getState());
        assertNotNull(dto.getAttributes());
        assertFalse(dto.getAttributes().containsKey(UserDto.ATTR_ENABLED_CREDENTIALS));
    }

    @Test
    void testConstructorWithNonDomainUser() {
        NonDomainUser user = new NonDomainUser();
        user.setAttribute("attr1", "val1", true);

        UserDto dto = new UserDto(user);

        assertEquals(user.getStringId(), dto.getId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getRealmId(), dto.getRealmId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getAvatar(), dto.getAvatar());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals(user.getFullName(), dto.getFullName());
        assertNull(dto.getState());
        assertFalse(dto.isEnabled());
        assertFalse(dto.isEmailVerified());
        assertNull(dto.getCreatedAt());
        assertNotNull(dto.getAttributes());
        assertFalse(dto.getAttributes().containsKey(UserDto.ATTR_ENABLED_CREDENTIALS));
        assertTrue(dto.getAttributes().containsKey("attr1"));
    }

    @Test
    void testCreateUserFactoryMethods() {
        DomainUser user = new DomainUser();
        Authority auth1 = new TestAuthority("ADMIN");
        user.addAuthority(auth1);
        user.addGroupId("group-1");

        UserDto dto = UserDto.createUser(user);
        assertNotNull(dto.getAuthorities());
        assertEquals(1, dto.getAuthorities().size());
        assertTrue(dto.getAuthorities().contains(auth1));
        assertNotNull(dto.getGroupIds());
        assertTrue(dto.getGroupIds().contains("group-1"));
        assertNull(dto.getGroups());

        // createUser with null groups
        UserDto dtoNullGroups = UserDto.createUser(user, null);
        assertNull(dtoNullGroups.getGroups());

        // createUser with groups list
        GroupDto groupDto1 = new GroupDto("group-1", "Group 1", "grp-1", "owner1", Set.of(), Set.of(), Set.of(), Set.of());
        UserDto dtoWithGroups = UserDto.createUser(user, List.of(groupDto1));
        assertNotNull(dtoWithGroups.getGroups());
        assertEquals(1, dtoWithGroups.getGroups().size());
        assertTrue(dtoWithGroups.getGroups().contains(groupDto1));
    }

    static class TestDomainProcessRole extends com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole {
        public TestDomainProcessRole(String id) {
            super(id);
        }
    }

    @Test
    void testGettersSettersAndLombokMethods() {
        UserDto dto1 = new UserDto(new DomainUser());
        UserDto dto2 = new UserDto(new DomainUser());

        dto1.setId("id-1");
        dto2.setId("id-1");
        dto1.setUsername("user1");
        dto2.setUsername("user1");
        dto1.setRealmId("realm-1");
        dto2.setRealmId("realm-1");
        dto1.setEmail("u1@test.com");
        dto2.setEmail("u1@test.com");
        dto1.setAvatar("av1");
        dto2.setAvatar("av1");
        dto1.setFirstName("First");
        dto2.setFirstName("First");
        dto1.setLastName("Last");
        dto2.setLastName("Last");
        dto1.setFullName("First Last");
        dto2.setFullName("First Last");

        Set<Authority> authorities = Set.of(new TestAuthority("USER"));
        dto1.setAuthorities(authorities);
        dto2.setAuthorities(authorities);

        String roleCompositeId = new com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId().getFullId();
        ProcessRole role1 = new ProcessRole(new TestDomainProcessRole(roleCompositeId), Locale.ENGLISH);
        Set<ProcessRole> roles = Set.of(role1);
        dto1.setProcessRoles(roles);
        dto2.setProcessRoles(roles);
        dto1.setNegativeProcessRoles(roles);
        dto2.setNegativeProcessRoles(roles);

        Set<String> groupIds = Set.of("g1");
        dto1.setGroupIds(groupIds);
        dto2.setGroupIds(groupIds);

        GroupDto groupDto = new GroupDto("g1", "Group", "grp", "owner", Set.of(), Set.of(), Set.of(), Set.of());
        Set<GroupDto> groups = Set.of(groupDto);
        dto1.setGroups(groups);
        dto2.setGroups(groups);

        UserDto impersonated = new UserDto(new DomainUser());
        dto1.setImpersonated(impersonated);
        dto2.setImpersonated(impersonated);

        LocalDateTime now = LocalDateTime.now();
        dto1.setCreatedAt(now);
        dto2.setCreatedAt(now);

        Map<String, Attribute<?>> attributes = Map.of("k", new Attribute<>("v", false));
        dto1.setAttributes(attributes);
        dto2.setAttributes(attributes);

        dto1.setEnabled(true);
        dto2.setEnabled(true);
        dto1.setEmailVerified(true);
        dto2.setEmailVerified(true);
        dto1.setState(UserState.ACTIVE);
        dto2.setState(UserState.ACTIVE);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertEquals("id-1", dto1.getId());
        assertEquals("user1", dto1.getUsername());
        assertEquals("realm-1", dto1.getRealmId());
        assertEquals("u1@test.com", dto1.getEmail());
        assertEquals("av1", dto1.getAvatar());
        assertEquals("First", dto1.getFirstName());
        assertEquals("Last", dto1.getLastName());
        assertEquals("First Last", dto1.getFullName());
        assertEquals(authorities, dto1.getAuthorities());
        assertEquals(roles, dto1.getProcessRoles());
        assertEquals(roles, dto1.getNegativeProcessRoles());
        assertEquals(groupIds, dto1.getGroupIds());
        assertEquals(groups, dto1.getGroups());
        assertEquals(impersonated, dto1.getImpersonated());
        assertEquals(now, dto1.getCreatedAt());
        assertEquals(attributes, dto1.getAttributes());
        assertTrue(dto1.isEnabled());
        assertTrue(dto1.isEmailVerified());
        assertEquals(UserState.ACTIVE, dto1.getState());
    }
}
