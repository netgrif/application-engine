package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.RoleNotFoundException;
import com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.RoleNotGlobalException;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "ProcessRoles")
@RequestMapping("/api/roles")
public class ProcessRoleController {

    private final ProcessRoleService processRoleService;

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Delete global role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @Parameter(name = "id", description = "Id of the global role to be deleted", required = true, example = "GcdIZcAPUc6jh7i2-68d683f80dc9384aa6791a64")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Global role was deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role id"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteGlobalRole(@PathVariable("id") String id, Authentication auth) {
        try {
            LoggedUser user = (LoggedUser) auth.getPrincipal();
            processRoleService.deleteGlobalRole(id, user);
        } catch (RoleNotGlobalException | RoleNotFoundException | IllegalArgumentException e) {
            String message = "Error when deleting global role [%s]".formatted(id);
            log.error(message, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Global role was deleted successfully");
    }

}
