package com.netgrif.application.engine.orgstructure.web;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.dto.request.group.CreateGroupRequestDto;
import com.netgrif.application.engine.objects.dto.request.group.GroupSearchRequestDto;
import com.netgrif.application.engine.objects.dto.response.group.GroupDto;
import com.netgrif.application.engine.objects.dto.response.user.UserDto;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.orgstructure.web.responsebodies.GroupsResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/group")
@ConditionalOnProperty(
        value = "netgrif.engine.security.web.group-enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService service;
    private final UserService userService;
    private final ProcessRoleService processRoleService;
    private final RealmService realmService;

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Get all groups in the system",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/all", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public GroupsResource getAllGroups(Locale locale) {
        List<com.netgrif.application.engine.objects.auth.domain.Group> groups = service.findAll(Pageable.unpaged()).getContent();
        Set<GroupDto> groupDtoResponse = groups.stream()
                .map(g -> GroupDto.fromGroup(g, locale))
                .collect(Collectors.toCollection(HashSet::new));
        return new GroupsResource(groupDtoResponse);
    }


    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
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
    public ResponseEntity<GroupDto> createGroup(@RequestBody CreateGroupRequestDto request, Locale locale) {
        if (!realmExists(request.realmId())) {
            String message = "Cannot create group, realm with id [" + request.realmId() + "] does not exist";
            log.error(message);
            return ResponseEntity.badRequest().build();
        }
        try {
            AbstractUser user = userService.findById(request.ownerId(), null);
            return ResponseEntity.status(HttpStatus.CREATED).body(GroupDto.fromGroup(service.create(request.identifier(), request.displayName(), user), locale));
        } catch (IllegalArgumentException e) {
            log.error("Failed to create new group", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Delete group defined by id",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New group created successfully"),
            @ApiResponse(responseCode = "400", description = "Request data invalid"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable("id") String groupId) {
        try {
            Group group = service.findById(groupId);
            service.delete(group);
            return ResponseEntity.ok("Group with id [" + groupId + "] deleted successfully");
        } catch (IllegalArgumentException e) {
            String message = "Failed to delete group with id [" + groupId + "]";
            log.error(message, e);
            return ResponseEntity.badRequest().body(message);
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
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
            Group group = service.findById(groupId);
            return ResponseEntity.ok(GroupDto.fromGroup(group, locale));
        } catch (IllegalArgumentException e) {
            log.error("Cannot get group with id [{}]", groupId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Get paged of group members",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group members retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Group with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/members")
    public ResponseEntity<Page<UserDto>> getMembersOfGroup(@PathVariable("id") String groupId, Pageable pageable) {
        try {
            Group group = service.findById(groupId);
//            todo use UserFactory to get users with roles?
            Page<UserDto> groupMembers = userService.findAllByIds(group.getMemberIds(), group.getRealmId(), pageable).map(UserDto::fromAbstractUser);
            return ResponseEntity.ok(groupMembers);
        } catch (IllegalArgumentException e) {
            log.error("Cannot get members of group with id [{}]", groupId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Add user to group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to group successfully"),
            @ApiResponse(responseCode = "400", description = "Group with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/members/{userId}")
    public ResponseEntity<String> addMemberToGroup(@PathVariable("id") String groupId, @PathVariable("userId") String userId) {
        try {
            Group group = service.findById(groupId);
            AbstractUser user = userService.findById(userId, group.getRealmId());
            service.addUser(user, group);
            return ResponseEntity.ok("Added user [" + userId + "] to group [" + groupId + "]");
        } catch (IllegalArgumentException e) {
            String message = "Failed to add member [%s] to group [%s]".formatted(userId, groupId);
            log.error(message, e);
            return ResponseEntity.badRequest().body(message);
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Remove user from group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User removed from group successfully"),
            @ApiResponse(responseCode = "400", description = "Group or user with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<String> removeMemberFromGroup(@PathVariable("id") String groupId, @PathVariable("userId") String userId) {
        try {
            Group group = service.findById(groupId);
            AbstractUser user = userService.findById(userId, group.getRealmId());
            service.removeUser(user, group);
            return ResponseEntity.ok("User [" + userId + "] removed from group [" + groupId + "]");
        } catch (IllegalArgumentException e) {
            String message = "Failed to remove member [%s] from group [%s]".formatted(userId, groupId);
            log.error(message, e);
            return ResponseEntity.badRequest().body("Failed to remove member from group: " + e.getMessage());
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Assign roles to group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected roles assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Requested roles or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/roles")
    public ResponseEntity<String> assignRolesToGroup(@PathVariable("id") String groupId, @RequestBody Set<String> roleIds) {
        try {
            Group group = service.findById(groupId);
            processRoleService.assignRolesToGroup(group, roleIds.stream().map(ProcessResourceId::new).collect(Collectors.toSet()));
            log.info("Process roles {} assigned to group [{}]", roleIds, groupId);
            return ResponseEntity.ok("Selected roles assigned to group  " + groupId);
        } catch (IllegalArgumentException e) {
            String message = "Assigning roles to group [%s] has failed".formatted(groupId);
            log.error(message, e);
            return ResponseEntity.badRequest().body(message);
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Assign authority to group",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PostMapping(value = "/{id}/authority", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authority assigned to group successfully"),
            @ApiResponse(responseCode = "400", description = "Group or authority with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    public ResponseEntity<String> assignAuthorityToGroup(@PathVariable("id") String groupId, @RequestBody String authorityId) {
        try {
            service.assignAuthority(groupId, authorityId);
        } catch (IllegalArgumentException e) {
            String message = "Group with id [%s] or authority with id [%s] does not exist".formatted(groupId, authorityId);
            log.error(message, e);
            return ResponseEntity.badRequest().body(message);
        }
        return ResponseEntity.ok("Authority " + authorityId + " assigned to group " + groupId);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Generic group search", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<GroupDto>> search(@RequestBody GroupSearchRequestDto query, Pageable pageable, Locale locale) {
        List<Group> groups = service.search(query, pageable).getContent();
        return ResponseEntity.ok(transformPageContent(groups, pageable, locale));
    }

    private Page<GroupDto> transformPageContent(List<Group> groups, Pageable pageable, Locale locale) {
        return new PageImpl<>(groups.stream().map(group -> GroupDto.fromGroup(group, locale)).toList(), pageable, groups.size());
    }

    private boolean realmExists(String realmId) {
        Optional<Realm> realm = realmService.getRealmById(realmId);
        return realm.isPresent();
    }
}
