package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.service.PreferencesService;
import com.netgrif.application.engine.auth.web.requestbodies.PreferencesRequest;
import com.netgrif.application.engine.auth.web.responsebodies.*;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.preferences.Preferences;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import com.netgrif.application.engine.workflow.web.responsebodies.ResourceLinkAssembler;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.web.requestbodies.UserSearchRequestBody;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestController
@ConditionalOnProperty(
        value = "netgrif.engine.public.web.user-enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Public User Controller")
@RequestMapping("/api/public/user")
public class PublicUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PreferencesService preferencesService;

    @Operation(summary = "Get logged user", description = "Retrieves information of currently logged user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getLoggedUser(Authentication auth) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        IUser user;
        try {
            user = userService.findById(loggedUser.getId(), loggedUser.getRealmId());
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (IllegalArgumentException e) {
            log.error("Could not find user with id [{}]", loggedUser.getId(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(User.createUser(user));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Generic user search", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<User>> search(@RequestBody UserSearchRequestBody query, Pageable pageable, Authentication auth) {
        List<ProcessResourceId> roles = query.getRoles() == null ? null : query.getRoles().stream().map(ProcessResourceId::new).toList();
        List<ProcessResourceId> negativeRoles = query.getNegativeRoles() == null ? null : query.getNegativeRoles().stream().map(ProcessResourceId::new).toList();
        Page<IUser> users = userService.searchAllCoMembers(query.getFulltext(),
                roles,
                negativeRoles,
                (LoggedUser) auth.getPrincipal(), pageable);
        return ResponseEntity.ok(changeToResponse(users, pageable));
    }

    @Operation(summary = "Get logged user's preferences", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns preferences of logged user"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PreferencesResource> preferences(Authentication auth) {
        String userId = ((LoggedUser) auth.getPrincipal()).getId();
        Preferences preferences = preferencesService.get(userId);

        if (preferences == null) {
            preferences = new com.netgrif.application.engine.adapter.spring.preferences.Preferences(userId);
        }
        PreferencesResource preferencesResource = PreferencesResource.withPreferences(preferences);

        return ResponseEntity.ok(preferencesResource);
    }


    @Operation(summary = "Set preferences of logged user", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saves preferences of logged user"),
            @ApiResponse(responseCode = "400", description = "Preferences data are invalid"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/preferences", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> savePreferences(@RequestBody PreferencesRequest preferences, Authentication auth) {
        try {
            String userId = ((LoggedUser) auth.getPrincipal()).getId();
            preferences.setUserId(userId);
            preferencesService.save(preferences.toPreferences());
            return ResponseEntity.ok("User preferences saved");
        } catch (Exception e) {
            log.error("Saving user preferences failed", e);
            return ResponseEntity.badRequest().body("Saving user preferences failed");
        }
    }

    private Page<User> changeToResponse(Page<IUser> users, Pageable pageable) {
        return new PageImpl<>(changeType(users.getContent()), pageable, users.getTotalElements());
    }

    public List<User> changeType(List<IUser> users) {
        return users.stream().map(User::createUser).toList();
    }

}
