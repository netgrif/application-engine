package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.adapter.spring.common.web.responsebodies.ResponseMessage;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Group;
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
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/groups")
@Tag(name = "GroupController")
@ConditionalOnProperty(
        value = "netgrif.engine.group.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class GroupController {
    
    private final GroupService groupService;

    private final UserService userService;

    private final ProcessRoleService processRoleService;

    @Operation(summary = "Retrieves group by its ID", description = "The endpoint receives ID of group and returns it the caller", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found"),
            @ApiResponse(responseCode = "400", description = "Invalid group ID"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getGroup(@PathVariable String id, Locale locale) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Group group = groupService.findById(id);
            return ResponseEntity.ok(GroupDto.fromGroup(group, locale));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Search page of groups", description = "Retrieves a page of groups according to search params and pageable objects", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of groups found"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @GetMapping("/search")
    public ResponseEntity<Page<GroupDto>> searchGroups(GroupSearchRequestDto searchDto, Pageable pageable, Locale locale) {
        try {
            Page<Group> groups = groupService.search(searchDto, pageable);
            return ResponseEntity.ok(groups.map(group -> GroupDto.fromGroup(group, locale)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Creates new group", description = "Creates new group according to parameters in request body", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ResponseMessage> createGroup(@RequestBody CreateGroupRequestDto newGroupRequest) {
        if (newGroupRequest == null) {
            return ResponseEntity.badRequest().build();
        }
        AbstractUser user = userService.findById(newGroupRequest.ownerId(), null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseMessage.createErrorMessage("User with id [%s] not found".formatted(newGroupRequest.ownerId())));
        }
        groupService.create(newGroupRequest.identifier(), newGroupRequest.displayName(), user);
        return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Group created successfully"));
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Deletes group", description = "Deletes group according to incoming ID", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group ID"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteGroup(@PathVariable @Valid @Size(min = 24, max = 24) String id) {
        try {
            Group group = groupService.findById(id);
            groupService.delete(group);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Group with id [%s] deleted successfully".formatted(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseMessage.createErrorMessage("Failed to delete group with id [%s]".formatted(id)));
        }
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Updates group", description = "Updates group according to incoming parameters", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group parameters"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping
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
    @Operation(summary = "Assign roles to the group", description = "Assigns roles based on request body to group based on roleIds", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PatchMapping(value = "/{id}/roles/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected roles assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Requested roles or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @PatchMapping(value = "/{id}/roles/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected roles added successfully"),
            @ApiResponse(responseCode = "400", description = "Requested roles or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @PatchMapping(value = "/{id}/roles/revoke", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected roles revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Requested roles or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @PatchMapping(value = "/{id}/authorities/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected authorities added successfully"),
            @ApiResponse(responseCode = "400", description = "Requested authorities or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @PatchMapping(value = "/{id}/authorities/revoke", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected authorities revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Requested authorities or group with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseMessage> revokeAuthorityFromUser(@PathVariable("id") String groupId, @RequestBody Set<String> authorityIds) {
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

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Assigns subgroups to group", description = "Removes existing subgroups and assigns new ones to group based on path param and request body", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PatchMapping(value = "/{id}/groups/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected subgroups was successfully assigned to group"),
            @ApiResponse(responseCode = "400", description = "Requested group or groups with defined id do not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseMessage> assignSubgroupsToGroup(@PathVariable("id") String groupId, @RequestBody Set<String> subgroupIds) {

    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Adds subgroups to group", description = "Add subgroups to group based on path param and request body", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PatchMapping(value = "/{id}/groups/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected subgroups was successfully added to group"),
            @ApiResponse(responseCode = "400", description = "Requested group or groups with defined id do not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @PatchMapping(value = "/{id}/groups/remove", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected subgroups was successfully removed from group"),
            @ApiResponse(responseCode = "400", description = "Requested group or groups with defined id do not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseMessage> removeSubgroupsFromGroup(@PathVariable("id") String groupId, @RequestBody Set<String> subgroupIds) {
        try {
            log.info("Subgroups {} removed from group with id [{}]", subgroupIds, groupId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected subgroups was successfully removed from group"));
        } catch (IllegalArgumentException e) {
            String message = "Removing subgroups from group [" + groupId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Removing subgroups from group " + groupId + " has failed!"));
        }
    }
}
