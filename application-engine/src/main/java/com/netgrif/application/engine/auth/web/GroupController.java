package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.adapter.spring.common.web.responsebodies.ResponseMessage;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.web.requestbodies.NewGroupRequest;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.dto.GroupDto;
import com.netgrif.application.engine.objects.auth.dto.SearchGroupDto;
import com.netgrif.application.engine.orgstructure.web.responsebodies.Group;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Retrieves group by its ID", description = "The endpoint receives ID of group and returns it the caller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found"),
            @ApiResponse(responseCode = "400", description = "Invalid group ID"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            com.netgrif.application.engine.objects.auth.domain.Group group = groupService.findById(id);
            return ResponseEntity.ok(new Group(group.getStringId(), group.getDisplayName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Search page of groups", description = "Retrieves a page of groups according to search params and pageable objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of groups found"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @GetMapping("/search")
    public ResponseEntity<Page<Group>> searchGroups(SearchGroupDto searchDto, Pageable pageable) {
        try {
            Page<com.netgrif.application.engine.objects.auth.domain.Group> groups = groupService.search(searchDto, pageable);
            return ResponseEntity.ok(groups.map(group -> new Group(group.getStringId(), group.getDisplayName())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Creates new group", description = "Creates new group according to parameters in request body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ResponseMessage> createGroup(@RequestBody NewGroupRequest newGroupRequest) {
        if (newGroupRequest == null) {
            return ResponseEntity.badRequest().build();
        }
        AbstractUser user = userService.findById(newGroupRequest.getOwnerId(), null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseMessage.createErrorMessage("User with id [%s] not found".formatted(newGroupRequest.getOwnerId())));
        }
        groupService.create(newGroupRequest.getIdentifier(), newGroupRequest.getTitle(), user);
        return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Group created successfully"));
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Deletes group", description = "Deletes group according to incoming ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group ID"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteGroup(@PathVariable @Size(min = 24, max = 24) String id) {
        try {
            com.netgrif.application.engine.objects.auth.domain.Group group = groupService.findById(id);
            groupService.delete(group);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Group with id [%s] deleted successfully".formatted(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseMessage.createErrorMessage("Failed to delete group with id [%s]".formatted(id)));
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Updates group", description = "Updates group according to incoming parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group parameters"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping
    public ResponseEntity<GroupDto> updateGroup(@RequestBody GroupDto group) {

        return ResponseEntity.ok(group);
    }
}
