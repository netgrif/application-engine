package com.netgrif.application.engine.manager.web;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.manager.web.responsebodies.AllLoggedUsersResponse;
import com.netgrif.application.engine.manager.web.responsebodies.MessageLogoutResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController()
@RequestMapping("/api/manager/session")
@ConditionalOnProperty(
        value = "nae.session.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Session Manager")
public class SessionManagerController {

    @Autowired
    private ISessionManagerService sessionManagerService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All logged users",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/all", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public AllLoggedUsersResponse getAllSessions() {
        Collection<LoggedUser> loggedUsers = sessionManagerService.getAllLoggedUsers();
        return new AllLoggedUsersResponse(loggedUsers);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Logout current user",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/logout", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageLogoutResponse logoutCurrentSession() {

        return new MessageLogoutResponse(sessionManagerService.logoutSession(""));
    }

}
