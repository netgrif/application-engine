package com.netgrif.application.engine.manager.web;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.manager.web.body.request.LogoutRequest;
import com.netgrif.application.engine.manager.web.body.response.AllLoggedIdentitiesResponse;
import com.netgrif.application.engine.manager.web.body.response.MessageLogoutResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/manager/session")
@ConditionalOnProperty(
        value = "nae.session.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Session Manager")
public class SessionManagerController {

    private final ISessionManagerService sessionManagerService;

    @PreAuthorize("@applicationAuthorizationService.hasApplicationRole('admin')")
    @Operation(summary = "Get All logged identities",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/all", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public AllLoggedIdentitiesResponse getAllSessions() {
        Collection<LoggedIdentity> identities = sessionManagerService.getAllLoggedIdentities();
        return new AllLoggedIdentitiesResponse(identities);
    }

    @PreAuthorize("@applicationAuthorizationService.hasApplicationRole('admin')")
    @Operation(summary = "Logout current identity",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/logout", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageLogoutResponse logoutCurrentSession(@RequestBody LogoutRequest requestBody) {

        requestBody.getIdentities().forEach(sessionManagerService::logoutSessionByUsername);
        return new MessageLogoutResponse(true);
    }

    @PreAuthorize("@applicationAuthorizationService.hasApplicationRole('admin')")
    @Operation(summary = "Logout all identities",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/logout/all", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageLogoutResponse logoutAllSession() {
        sessionManagerService.logoutAllSession();
        return new MessageLogoutResponse(true);
    }

}
