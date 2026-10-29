package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.adapter.spring.common.web.responsebodies.ResponseMessage;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.auth.service.UserFactory;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.web.responsebodies.UserDto;
import com.netgrif.application.engine.objects.dto.request.group.CreateGroupRequestDto;
import com.netgrif.application.engine.objects.dto.request.group.GroupSearchRequestDto;
import com.netgrif.application.engine.objects.dto.request.group.UpdateGroupRequestDto;
import com.netgrif.application.engine.objects.dto.response.group.GroupDto;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @Mock
    private ProcessRoleService processRoleService;

    @Mock
    private RealmService realmService;

    @Mock
    private UserFactory userFactory;

    private GroupController controller;

    static class DomainGroup extends com.netgrif.application.engine.adapter.spring.auth.domain.Group {
        public DomainGroup(String id, String identifier, String displayName, String realmId) {
            super();
            this.id = new ObjectId(id);
            this.setIdentifier(identifier);
            this.setDisplayName(displayName);
            this.realmId = realmId;
        }
    }

    static class DomainUser extends com.netgrif.application.engine.adapter.spring.auth.domain.User {
        public DomainUser(String id, String username, String realmId) {
            super();
            this.id = new ObjectId(id);
            this.username = username;
            this.realmId = realmId;
        }
    }

    static class TestProcessRole extends ProcessRole {
        public TestProcessRole(String id) {
            super(id);
        }
    }

    @BeforeEach
    void setUp() {
        controller = new GroupController(groupService, userService, processRoleService, realmService, userFactory);
    }

    @Test
    void testGetAllGroupsOfRealm() {
        String realmId = "realm-1";
        Pageable pageable = PageRequest.of(0, 10);
        DomainGroup group = new DomainGroup(new ObjectId().toString(), "grp1", "Group 1", realmId);
        when(groupService.findAllFromRealm(realmId, pageable)).thenReturn(new PageImpl<>(List.of(group), pageable, 1));

        ResponseEntity<Page<GroupDto>> response = controller.getAllGroupsOfRealm(realmId, pageable, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("grp1", response.getBody().getContent().get(0).identifier());
    }

    @Test
    void testCreateGroup() {
        // null request
        ResponseEntity<ResponseMessage> nullResponse = controller.createGroup(null);
        assertEquals(HttpStatus.BAD_REQUEST, nullResponse.getStatusCode());

        String ownerId = new ObjectId().toString();
        CreateGroupRequestDto request = new CreateGroupRequestDto("Display Name", "realm-1", "ident-1", ownerId);

        // Realm does not exist
        when(realmService.getRealmById("realm-1")).thenReturn(Optional.empty());
        ResponseEntity<ResponseMessage> noRealmResponse = controller.createGroup(request);
        assertEquals(HttpStatus.BAD_REQUEST, noRealmResponse.getStatusCode());
        assertTrue(noRealmResponse.getBody().getError().contains("does not exist"));

        // Realm with null id branch
        CreateGroupRequestDto nullRealmRequest = new CreateGroupRequestDto("Display Name", null, "ident-1", ownerId);
        ResponseEntity<ResponseMessage> nullRealmResponse = controller.createGroup(nullRealmRequest);
        assertEquals(HttpStatus.BAD_REQUEST, nullRealmResponse.getStatusCode());

        // Realm exists, but user not found
        when(realmService.getRealmById("realm-1")).thenReturn(Optional.of(new com.netgrif.application.engine.adapter.spring.auth.domain.Realm("realm-1")));
        when(userService.findById(ownerId, "realm-1")).thenReturn(null);
        ResponseEntity<ResponseMessage> noUserResponse = controller.createGroup(request);
        assertEquals(HttpStatus.NOT_FOUND, noUserResponse.getStatusCode());

        // User found, but service throws IllegalArgumentException
        DomainUser owner = new DomainUser(ownerId, "john", "realm-1");
        when(userService.findById(ownerId, "realm-1")).thenReturn(owner);
        doThrow(new IllegalArgumentException("Already exists")).when(groupService).create("ident-1", "Display Name", owner);
        ResponseEntity<ResponseMessage> errorResponse = controller.createGroup(request);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());

        // Service throws general Exception
        doThrow(new RuntimeException("DB error")).when(groupService).create("ident-1", "Display Name", owner);
        ResponseEntity<ResponseMessage> serverErrorResponse = controller.createGroup(request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, serverErrorResponse.getStatusCode());

        // Success
        DomainGroup createdGroup = new DomainGroup(new ObjectId().toString(), "ident-1", "Display Name", "realm-1");
        doReturn(createdGroup).when(groupService).create("ident-1", "Display Name", owner);
        ResponseEntity<ResponseMessage> successResponse = controller.createGroup(request);
        assertEquals(HttpStatus.CREATED, successResponse.getStatusCode());
        assertNotNull(successResponse.getHeaders().getLocation());
        assertTrue(successResponse.getHeaders().getLocation().toString().contains(createdGroup.getStringId()));
    }

    @Test
    void testDeleteGroup() {
        String groupId = new ObjectId().toString();
        DomainGroup group = new DomainGroup(groupId, "ident", "Group", "realm-1");

        when(groupService.findById(groupId)).thenReturn(group);
        doNothing().when(groupService).delete(group);

        ResponseEntity<String> response = controller.deleteGroup(groupId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("deleted successfully"));

        // Throws IllegalArgumentException
        when(groupService.findById("unknown")).thenThrow(new IllegalArgumentException("Not found"));
        ResponseEntity<String> errorResponse = controller.deleteGroup("unknown");
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testGetGroup() {
        String groupId = new ObjectId().toString();
        DomainGroup group = new DomainGroup(groupId, "ident", "Group", "realm-1");

        when(groupService.findById(groupId)).thenReturn(group);
        ResponseEntity<GroupDto> response = controller.getGroup(groupId, Locale.ENGLISH);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ident", response.getBody().identifier());

        when(groupService.findById("unknown")).thenThrow(new IllegalArgumentException("Not found"));
        ResponseEntity<GroupDto> errorResponse = controller.getGroup("unknown", Locale.ENGLISH);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testGetMembersOfGroup() {
        String groupId = new ObjectId().toString();
        DomainGroup group = new DomainGroup(groupId, "ident", "Group", "realm-1");
        String memberId = new ObjectId().toString();
        group.addMemberId(memberId);

        Pageable pageable = PageRequest.of(0, 10);
        when(groupService.findById(groupId)).thenReturn(group);

        DomainUser member = new DomainUser(memberId, "member", "realm-1");
        when(userService.findAllByIds(group.getMemberIds(), "realm-1", pageable)).thenReturn(new PageImpl<>(List.of(member)));
        UserDto userDto = new UserDto(member);
        when(userFactory.getUser(member, Locale.ENGLISH)).thenReturn(userDto);

        ResponseEntity<Page<UserDto>> response = controller.getMembersOfGroup(groupId, pageable, Locale.ENGLISH);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        // Group not found
        when(groupService.findById("unknown")).thenThrow(new IllegalArgumentException("Not found"));
        ResponseEntity<Page<UserDto>> errorResponse = controller.getMembersOfGroup("unknown", pageable, Locale.ENGLISH);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testAssignUsersToGroup() {
        String groupId = new ObjectId().toString();
        Set<String> userIds = Set.of(new ObjectId().toString());

        ResponseEntity<ResponseMessage> response = controller.assignUsersToGroup(groupId, userIds);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).assignUsersToGroup(groupId, userIds);

        doThrow(new IllegalArgumentException("Invalid user")).when(groupService).assignUsersToGroup("bad-id", userIds);
        ResponseEntity<ResponseMessage> errorResponse = controller.assignUsersToGroup("bad-id", userIds);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testAddUserToGroup() {
        String groupId = new ObjectId().toString();
        String userId = new ObjectId().toString();

        DomainGroup group = new DomainGroup(groupId, "ident", "Group", "realm-1");
        DomainUser user = new DomainUser(userId, "user1", "realm-1");

        when(groupService.findById(groupId)).thenReturn(group);
        when(userService.findById(userId, "realm-1")).thenReturn(user);

        ResponseEntity<String> response = controller.addUserToGroup(groupId, userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).addUser(group, user);

        when(groupService.findById("unknown")).thenThrow(new IllegalArgumentException("Not found"));
        ResponseEntity<String> errorResponse = controller.addUserToGroup("unknown", userId);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testRemoveUserFromGroup() {
        String groupId = new ObjectId().toString();
        String userId = new ObjectId().toString();

        DomainGroup group = new DomainGroup(groupId, "ident", "Group", "realm-1");
        DomainUser user = new DomainUser(userId, "user1", "realm-1");

        when(groupService.findById(groupId)).thenReturn(group);
        when(userService.findById(userId, "realm-1")).thenReturn(user);

        ResponseEntity<String> response = controller.removeUserFromGroup(groupId, userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).removeUser(group, user);

        when(groupService.findById("unknown")).thenThrow(new IllegalArgumentException("Not found"));
        ResponseEntity<String> errorResponse = controller.removeUserFromGroup("unknown", userId);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testAssignRolesToGroup() {
        String groupId = new ObjectId().toString();
        String roleId = new ProcessResourceId().getFullId();
        Set<String> roleIds = Set.of(roleId);

        DomainGroup group = new DomainGroup(groupId, "ident", "Group", "realm-1");
        when(groupService.findById(groupId)).thenReturn(group);

        ResponseEntity<ResponseMessage> response = controller.assignRolesToGroup(groupId, roleIds);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(processRoleService).assignRolesToGroup(eq(group), any());

        when(groupService.findById("bad-id")).thenThrow(new IllegalArgumentException("Not found"));
        ResponseEntity<ResponseMessage> errorResponse = controller.assignRolesToGroup("bad-id", roleIds);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testAddRolesToGroup() {
        String groupId = new ObjectId().toString();
        String roleId = new ProcessResourceId().getFullId();
        Set<String> roleIds = Set.of(roleId);

        DomainGroup group = new DomainGroup(groupId, "ident", "Group", "realm-1");
        when(groupService.findById(groupId)).thenReturn(group);

        TestProcessRole processRole = new TestProcessRole(roleId);
        when(processRoleService.findById(roleId)).thenReturn(processRole);

        ResponseEntity<ResponseMessage> response = controller.addRolesToGroup(groupId, roleIds);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).addRole(group, processRole);

        // Role not found branch
        when(processRoleService.findById("missing-role")).thenReturn(null);
        ResponseEntity<ResponseMessage> missingRoleResponse = controller.addRolesToGroup(groupId, Set.of("missing-role"));
        assertEquals(HttpStatus.BAD_REQUEST, errorResponseStatusCode(missingRoleResponse));

        // Group not found branch
        when(groupService.findById("unknown")).thenThrow(new IllegalArgumentException("Not found"));
        ResponseEntity<ResponseMessage> errorResponse = controller.addRolesToGroup("unknown", roleIds);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    private HttpStatus errorResponseStatusCode(ResponseEntity<ResponseMessage> response) {
        return (HttpStatus) response.getStatusCode();
    }

    @Test
    void testRevokeRolesFromGroup() {
        String groupId = new ObjectId().toString();
        String roleId = new ProcessResourceId().getFullId();

        ResponseEntity<ResponseMessage> response = controller.revokeRolesFromGroup(groupId, Set.of(roleId));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).removeRole(groupId, roleId);

        doThrow(new IllegalArgumentException("Error")).when(groupService).removeRole("bad", roleId);
        ResponseEntity<ResponseMessage> errorResponse = controller.revokeRolesFromGroup("bad", Set.of(roleId));
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testAddAuthorityToGroup() {
        String groupId = new ObjectId().toString();
        String authId = new ObjectId().toString();

        ResponseEntity<ResponseMessage> response = controller.addAuthorityToGroup(groupId, Set.of(authId));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).addAuthority(groupId, authId);

        doThrow(new IllegalArgumentException("Error")).when(groupService).addAuthority("bad", authId);
        ResponseEntity<ResponseMessage> errorResponse = controller.addAuthorityToGroup("bad", Set.of(authId));
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testRevokeAuthorityFromGroup() {
        String groupId = new ObjectId().toString();
        String authId = new ObjectId().toString();

        ResponseEntity<ResponseMessage> response = controller.revokeAuthorityFromGroup(groupId, Set.of(authId));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).removeAuthority(groupId, authId);

        doThrow(new IllegalArgumentException("Error")).when(groupService).removeAuthority("bad", authId);
        ResponseEntity<ResponseMessage> errorResponse = controller.revokeAuthorityFromGroup("bad", Set.of(authId));
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testSearch() {
        GroupSearchRequestDto query = new GroupSearchRequestDto();
        Pageable pageable = PageRequest.of(0, 10);
        DomainGroup group = new DomainGroup(new ObjectId().toString(), "ident", "Group", "realm-1");

        when(groupService.search(query, pageable)).thenReturn(new PageImpl<>(List.of(group)));

        ResponseEntity<Page<GroupDto>> response = controller.search(query, pageable, Locale.ENGLISH);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testUpdateGroup() {
        String groupId = new ObjectId().toString();
        DomainGroup group = new DomainGroup(groupId, "old-ident", "Old Name", "realm-1");

        when(groupService.findById(groupId)).thenReturn(group);
        when(groupService.findByIdentifier("new-ident")).thenReturn(Optional.empty());

        UpdateGroupRequestDto updateDto = new UpdateGroupRequestDto(groupId, "new-ident", "New Name");
        ResponseEntity<ResponseMessage> response = controller.updateGroup(updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-ident", group.getIdentifier());
        assertEquals("New Name", group.getDisplayName());
        verify(groupService).save(group);

        // When identifier is already taken by another group
        when(groupService.findByIdentifier("taken-ident")).thenReturn(Optional.of(new DomainGroup(new ObjectId().toString(), "taken-ident", "Other", "realm-1")));
        UpdateGroupRequestDto takenDto = new UpdateGroupRequestDto(groupId, "taken-ident", null);
        controller.updateGroup(takenDto);
        assertEquals("new-ident", group.getIdentifier()); // identifier not modified

        // When group not found (IllegalArgumentException)
        when(groupService.findById("missing")).thenThrow(new IllegalArgumentException("Not found"));
        ResponseEntity<ResponseMessage> notFoundResponse = controller.updateGroup(new UpdateGroupRequestDto("missing", "id", "name"));
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());

        // When unexpected Exception occurs
        when(groupService.findById("error")).thenReturn(group);
        when(groupService.save(group)).thenThrow(new RuntimeException("Save failed"));
        ResponseEntity<ResponseMessage> errorResponse = controller.updateGroup(new UpdateGroupRequestDto("error", null, "name"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse.getStatusCode());
    }

    @Test
    void testAssignSubgroupsToGroup() {
        String groupId = new ObjectId().toString();
        Set<String> subgroupIds = Set.of(new ObjectId().toString());

        ResponseEntity<ResponseMessage> response = controller.assignSubgroupsToGroup(groupId, subgroupIds);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).assignSubgroups(groupId, subgroupIds);

        doThrow(new IllegalArgumentException("Error")).when(groupService).assignSubgroups("bad", subgroupIds);
        ResponseEntity<ResponseMessage> errorResponse = controller.assignSubgroupsToGroup("bad", subgroupIds);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testAddSubgroupsToGroup() {
        String groupId = new ObjectId().toString();
        String subgroupId = new ObjectId().toString();

        ResponseEntity<ResponseMessage> response = controller.addSubgroupsToGroup(groupId, Set.of(subgroupId));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).addSubgroup(groupId, subgroupId);

        doThrow(new IllegalArgumentException("Error")).when(groupService).addSubgroup("bad", subgroupId);
        ResponseEntity<ResponseMessage> errorResponse = controller.addSubgroupsToGroup("bad", Set.of(subgroupId));
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void testRemoveSubgroupsFromGroup() {
        String groupId = new ObjectId().toString();
        String subgroupId = new ObjectId().toString();

        ResponseEntity<ResponseMessage> response = controller.removeSubgroupsFromGroup(groupId, Set.of(subgroupId));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).removeSubgroup(groupId, subgroupId);

        doThrow(new IllegalArgumentException("Error")).when(groupService).removeSubgroup("bad", subgroupId);
        ResponseEntity<ResponseMessage> errorResponse = controller.removeSubgroupsFromGroup("bad", Set.of(subgroupId));
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }
}
