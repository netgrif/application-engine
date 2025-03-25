package com.netgrif.application.engine.authorization.web;

import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
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
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    private final IRoleAssignmentService roleAssignmentService;
    private final IRoleService roleService;

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Assign roles to the actor", description = "Caller must have the ADMIN role", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{actorId}/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource assignRolesToUser(@PathVariable("actorId") String actorId, @RequestBody Set<String> roleIds) {
        try {
            List<Role> assignedRoles = roleService.assignRolesToActor(actorId, roleIds);
            Set<String> assignedRoleIds = assignedRoles.stream().map(Role::getStringId).collect(Collectors.toSet());
            log.info("Roles [{}] assigned to actor [{}]", assignedRoleIds, actorId);
            return MessageResource.successMessage(String.format("Selected roles assigned to actor [%s]", actorId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MessageResource.errorMessage(String.format("Assigning roles to actor [%s] has failed!", actorId));
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Remove roles from the user", description = "Caller must have the ADMIN role", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{actorId}/remove", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource removeRolesFromUser(@PathVariable("actorId") String actorId, @RequestBody Set<String> roleIds) {
        try {
            List<Role> removedRoles = roleService.removeRolesFromActor(actorId, roleIds);
            Set<String> removedRoleIds = removedRoles.stream().map(Role::getStringId).collect(Collectors.toSet());
            log.info("Roles [{}] removed from actor [{}]", removedRoleIds, actorId);
            return MessageResource.successMessage(String.format("Selected roles removed from actor [%s]", actorId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MessageResource.errorMessage(String.format("Removing roles from actor [%s] has failed!", actorId));
        }
    }
}
