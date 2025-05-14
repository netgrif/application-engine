package com.netgrif.application.engine.authorization.web;

import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "nae.rbac.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequestMapping("/api/authorization")
@Tag(name = "RBAC", description = "With this API you can manage roles and assignments")
public class RBACController {
    private final IRoleService roleService;

    @PreAuthorize("@applicationAuthorizationService.hasApplicationRole('admin')")
    @Operation(summary = "Assign roles to the actor", description = "Caller must have the ADMIN role", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{actorId}/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public CollectionModel<Role> assignRolesToActor(@PathVariable("actorId") String actorId, @RequestBody Set<String> roleIds) {
        try {
            List<Role> assignedRoles = roleService.assignRolesToActor(actorId, roleIds);
            Set<String> assignedRoleIds = assignedRoles.stream().map(Role::getStringId).collect(Collectors.toSet());
            log.info("Roles [{}] assigned to actor [{}]", assignedRoleIds, actorId);
            return CollectionModel.of(assignedRoles);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Roles weren't assigned: %s", e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Something went wrong while assigning roles to actor [%s]", actorId));
        }
    }

    @PreAuthorize("@applicationAuthorizationService.hasApplicationRole('admin')")
    @Operation(summary = "Remove roles from the actor", description = "Caller must have the ADMIN role", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{actorId}/remove", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public CollectionModel<Role> removeRolesFromActor(@PathVariable("actorId") String actorId, @RequestBody Set<String> roleIds) {
        try {
            List<Role> removedRoles = roleService.removeRolesFromActor(actorId, roleIds);
            Set<String> removedRoleIds = removedRoles.stream().map(Role::getStringId).collect(Collectors.toSet());
            log.info("Roles [{}] removed from actor [{}]", removedRoleIds, actorId);
            return CollectionModel.of(removedRoles);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Roles weren't removed: %s", e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Something went wrong while removing roles from actor [%s]", actorId));
        }
    }

    @Operation(summary = "Finds role ids assigned to provided actor", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{actorId}/roles", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public Set<String> findRoleIdsByActorAndGroups(@PathVariable("actorId") String actorId) {
        try {
            return roleService.findAllRoleIdsByActorAndGroups(actorId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something unexpected happened");
        }
    }
}
