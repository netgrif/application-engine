package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.adapter.spring.common.web.responsebodies.ResponseMessage;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.auth.service.UserFactory;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.web.responsebodies.UserDto;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.dto.request.group.CreateGroupRequestDto;
import com.netgrif.application.engine.objects.dto.request.group.GroupSearchRequestDto;
import com.netgrif.application.engine.objects.dto.request.group.UpdateGroupRequestDto;
import com.netgrif.application.engine.objects.dto.response.group.GroupDto;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/groups")
@ConditionalOnProperty(
        value = "netgrif.engine.group.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;
    private final ProcessRoleService processRoleService;
    private final RealmService realmService;
    private final UserFactory userFactory;

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Get page of groups from defined realm",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieve page of groups from defined realm"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(path = "/{realmId}/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GroupDto>> getAllGroupsOfRealm(@PathVariable("realmId") String realmId, Pageable pageable, Locale locale) {
        Page<Group> groups = groupService.findAllFromRealm(realmId, pageable);
        return ResponseEntity.ok(transformPageContent(groups.getContent(), pageable, locale));
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Create new group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New group created successfully"),
            @ApiResponse(responseCode = "400", description = "Request data invalid"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> createGroup(@Valid @RequestBody CreateGroupRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!realmExists(request.realmId())) {
            String message = "Cannot create group, realm with id [" + request.realmId() + "] does not exist";
            log.error(message);
            return ResponseEntity.badRequest().build();
        }
        AbstractUser user = userService.findById(request.ownerId(), null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseMessage.createErrorMessage("User with id [%s] not found".formatted(request.ownerId())));
        }
        try {
            groupService.create(request.identifier(), request.displayName(), user);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Group created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseMessage.createErrorMessage("Failed to create group with identifier [%s]".formatted(request.identifier())));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Delete group defined by id",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New group deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Request data invalid"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable("id") String groupId) {
        try {
            Group group = groupService.findById(groupId);
            groupService.delete(group);
            return ResponseEntity.ok("Group with id [" + groupId + "] deleted successfully");
        } catch (IllegalArgumentException e) {
            String message = "Failed to delete group with id [" + groupId + "]";
            log.error(message, e);
            return ResponseEntity.badRequest().body(message);
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Get group by id",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Group with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getGroup(@PathVariable("id") String groupId, Locale locale) {
        try {
            Group group = groupService.findById(groupId);
            return ResponseEntity.ok(GroupDto.fromGroup(group, locale));
        } catch (IllegalArgumentException e) {
            log.error("Cannot get group with id [{}]", groupId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Get paged of group members",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group members retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Group with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/users")
    public ResponseEntity<Page<UserDto>> getMembersOfGroup(@PathVariable("id") String groupId, Pageable pageable, Locale locale) {
        try {
            Group group = groupService.findById(groupId);
            Page<UserDto> groupMembers = userService.findAllByIds(group.getMemberIds(), group.getRealmId(), pageable).map(u -> userFactory.getUser(u, locale));
            return ResponseEntity.ok(groupMembers);
        } catch (IllegalArgumentException e) {
            log.error("Cannot get members of group with id [{}]", groupId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Assigns multiple users to group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users assigned to group successfully"),
            @ApiResponse(responseCode = "400", description = "Group with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/users/assign")
    public ResponseEntity<ResponseMessage> assignUsersToGroup(@PathVariable("id") String groupId, @RequestBody Set<String> userIds) {
        try {
            groupService.assignUsersToGroup(groupId, userIds);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected users assigned to group " + groupId));
        } catch (IllegalArgumentException e) {
            String message = "Failed to assign members to group [%s]".formatted(groupId);
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Assigning members to group " + groupId + " has failed!"));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Add user to group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to group successfully"),
            @ApiResponse(responseCode = "400", description = "Group with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/users/add/{userId}")
    public ResponseEntity<String> addUserToGroup(@PathVariable("id") String groupId, @PathVariable("userId") String userId) {
        try {
            Group group = groupService.findById(groupId);
            AbstractUser user = userService.findById(userId, group.getRealmId());
            groupService.addUser(group, user);
            return ResponseEntity.ok("Added user [" + userId + "] to group [" + groupId + "]");
        } catch (IllegalArgumentException e) {
            String message = "Failed to add member [%s] to group [%s]".formatted(userId, groupId);
            log.error(message, e);
            return ResponseEntity.badRequest().body(message);
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Remove user from group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User removed from group successfully"),
            @ApiResponse(responseCode = "400", description = "Group or user with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/users/remove/{userId}")
    public ResponseEntity<String> removeUserFromGroup(@PathVariable("id") String groupId, @PathVariable("userId") String userId) {
        try {
            Group group = groupService.findById(groupId);
            AbstractUser user = userService.findById(userId, group.getRealmId());
            groupService.removeUser(group, user);
            return ResponseEntity.ok("User [" + userId + "] removed from group [" + groupId + "]");
        } catch (IllegalArgumentException e) {
            String message = "Failed to remove member [%s] from group [%s]".formatted(userId, groupId);
            log.error(message, e);
            return ResponseEntity.badRequest().body("Failed to remove member from group: " + e.getMessage());
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Assign roles to the group", description = "Assigns roles based on request body to group based on roleIds", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected roles assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Requested roles or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(value = "/{id}/roles/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> assignRolesToGroup(@PathVariable("id") String groupId, @RequestBody Set<String> roleIds) {
        try {
            Group group = groupService.findById(groupId);
            processRoleService.assignRolesToGroup(group, roleIds.stream().map(ProcessResourceId::new).collect(Collectors.toSet()));
            log.info("Process roles {} assigned to group with id [{}]", roleIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected roles assigned to group " + groupId));
        } catch (IllegalArgumentException e) {
            String message = "Assigning roles to group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Assigning roles to group " + groupId + " has failed!"));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Adds roles to the group", description = "Adds roles based on request body to group based on roleIds", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected roles added successfully"),
            @ApiResponse(responseCode = "400", description = "Requested roles or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(value = "/{id}/roles/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> addRolesToGroup(@PathVariable("id") String groupId, @RequestBody Set<String> roleIds) {
        try {
            roleIds.forEach(roleId -> groupService.addRole(groupId, roleId));
            log.info("Process roles {} added to group with id [{}]", roleIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected roles added to group " + groupId));
        } catch (IllegalArgumentException e) {
            String message = "Adding roles to group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Adding roles to group " + groupId + " has failed!"));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Revokes roles to the group", description = "Revokes roles based on request body from group based on id", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected roles revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Requested roles or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(value = "/{id}/roles/revoke", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> revokeRolesFromGroup(@PathVariable("id") String groupId, @RequestBody Set<String> roleIds) {
        try {
            roleIds.forEach(roleId -> groupService.removeRole(groupId, roleId));
            log.info("Process roles {} revoked from group with id [{}]", roleIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected roles revoked from group " + groupId));
        } catch (IllegalArgumentException e) {
            String message = "Revoking roles from group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Revoking roles from group " + groupId + " has failed!"));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Adds authority to the group", description = "Adds authority based on request body to group based on id", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected authorities added successfully"),
            @ApiResponse(responseCode = "400", description = "Requested authorities or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(value = "/{id}/authorities/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> addAuthorityToGroup(@PathVariable("id") String groupId, @RequestBody Set<String> authorityIds) {
        try {
            authorityIds.forEach(authorityId -> groupService.addAuthority(groupId, authorityId));
            log.info("Authorities {} added to group with id [{}]", authorityIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected authorities added to group " + groupId));
        } catch (IllegalArgumentException e) {
            String message = "Adding authorities to group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Adding authorities to group " + groupId + " has failed!"));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Revokes authority from the group", description = "Revokes authority based on request body from group based on roleIds", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected authorities revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Requested authorities or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(value = "/{id}/authorities/revoke", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> revokeAuthorityFromGroup(@PathVariable("id") String groupId, @RequestBody Set<String> authorityIds) {
        try {
            authorityIds.forEach(authorityId -> groupService.removeAuthority(groupId, authorityId));
            log.info("Authorities {} revoked from group with id [{}]", authorityIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected authorities revoked from group " + groupId));
        } catch (IllegalArgumentException e) {
            String message = "Revoking authorities to group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Revoking authorities to group " + groupId + " has failed!"));
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Generic group search", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GroupDto>> search(GroupSearchRequestDto query, Pageable pageable, Locale locale) {
        List<Group> groups = groupService.search(query, pageable).getContent();
        return ResponseEntity.ok(transformPageContent(groups, pageable, locale));
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Updates group", description = "Updates group according to incoming parameters", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group parameters"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> updateGroup(@RequestBody UpdateGroupRequestDto groupUpdate) {
        try {
            Group group = groupService.findById(groupUpdate.id());
            if (groupUpdate.identifier() != null) {
                group.setIdentifier(groupUpdate.identifier());
            }
            if (groupUpdate.displayName() != null) {
                group.setDisplayName(groupUpdate.displayName());
            }
            groupService.save(group);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Group with id [%s] updated successfully".formatted(groupUpdate.id())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseMessage.createErrorMessage("Failed to update group with id [%s]".formatted(groupUpdate.id())));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Assigns subgroups to group", description = "Removes existing subgroups and assigns new ones to group based on path param and request body", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected subgroups was successfully assigned to group"),
            @ApiResponse(responseCode = "400", description = "Requested group or groups with defined id do not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(value = "/{id}/groups/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> assignSubgroupsToGroup(@PathVariable("id") String groupId, @RequestBody Set<String> subgroupIds) {
        try {
            groupService.assignSubgroups(groupId, subgroupIds);
            log.info("Subgroups {} assigned to group with id [{}]", subgroupIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected subgroups was successfully assigned to group"));
        } catch (IllegalArgumentException e) {
            String message = "Adding subgroups to group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Adding subgroups to group " + groupId + " has failed!"));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Adds subgroups to group", description = "Add subgroups to group based on path param and request body", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected subgroups was successfully added to group"),
            @ApiResponse(responseCode = "400", description = "Requested group or groups with defined id do not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(value = "/{id}/groups/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> addSubgroupsToGroup(@PathVariable("id") String groupId, @RequestBody Set<String> subgroupIds) {
        try {
            subgroupIds.forEach(subgroupId -> groupService.addSubgroup(groupId, subgroupId));
            log.info("Subgroups {} added to group with id [{}]", subgroupIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected subgroups was successfully added to group"));
        } catch (IllegalArgumentException e) {
            String message = "Adding subgroups to group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Adding subgroups to group " + groupId + " has failed!"));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Removes subgroups from group", description = "Removes subgroups from group based on path param and request body", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected subgroups was successfully removed from group"),
            @ApiResponse(responseCode = "400", description = "Requested group or groups with defined id do not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(value = "/{id}/groups/remove", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> removeSubgroupsFromGroup(@PathVariable("id") String groupId, @RequestBody Set<String> subgroupIds) {
        try {
            subgroupIds.forEach(subgroupId -> groupService.removeSubgroup(groupId, subgroupId));
            log.info("Subgroups {} removed from group with id [{}]", subgroupIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected subgroups was successfully removed from group"));
        } catch (IllegalArgumentException e) {
            String message = "Removing subgroups from group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Removing subgroups from group " + groupId + " has failed!"));
        }
    }

    private Page<GroupDto> transformPageContent(List<Group> groups, Pageable pageable, Locale locale) {
        return new PageImpl<>(groups.stream().map(group -> GroupDto.fromGroup(group, locale)).toList(), pageable, groups.size());
    }

    private boolean realmExists(String realmId) {
        if (realmId == null) {
            return false;
        }
        Optional<Realm> realm = realmService.getRealmById(realmId);
        return realm.isPresent();
    }
}
