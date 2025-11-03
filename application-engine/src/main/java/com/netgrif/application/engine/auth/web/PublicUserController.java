package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.service.PreferencesService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.web.responsebodies.PreferencesResource;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.dto.PreferencesDto;
import com.netgrif.application.engine.objects.dto.request.user.UserSearchRequestBody;
import com.netgrif.application.engine.objects.dto.response.user.UserDto;
import com.netgrif.application.engine.objects.preferences.Preferences;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

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
    public ResponseEntity<UserDto> getLoggedUser(Authentication auth) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        AbstractUser user;
        try {
            user = userService.findById(loggedUser.getStringId(), loggedUser.getRealmId());
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (IllegalArgumentException e) {
            log.error("Could not find user with id [{}]", loggedUser.getId(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(UserDto.fromAbstractUser(user));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Generic user search", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserDto>> search(@RequestBody UserSearchRequestBody query, Pageable pageable, Authentication auth, Locale locale) {
        List<ProcessResourceId> roles = query.roles() == null ? null : query.roles().stream().map(ProcessResourceId::new).toList();
        Page<AbstractUser> users = userService.searchAllCoMembers(query.fulltext(),
                roles,
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
        String userId = ((LoggedUser) auth.getPrincipal()).getStringId();
        Preferences preferences = preferencesService.get(userId);

        if (preferences == null) {
            preferences = new com.netgrif.application.engine.adapter.spring.preferences.Preferences(userId);
        }
        PreferencesResource preferencesResource = PreferencesResource.withPreferences(PreferencesDto.fromPreferences(preferences));

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
    public ResponseEntity<String> savePreferences(@RequestBody PreferencesDto preferences, Authentication auth) {
        try {
            String userId = ((LoggedUser) auth.getPrincipal()).getStringId();
            Preferences domainPreferences = com.netgrif.application.engine.adapter.spring.preferences.Preferences.fromDto(preferences, userId);
            preferencesService.save(domainPreferences);
            return ResponseEntity.ok("User preferences saved");
        } catch (Exception e) {
            log.error("Saving user preferences failed", e);
            return ResponseEntity.badRequest().body("Saving user preferences failed");
        }
    }

    private Page<UserDto> changeToResponse(Page<AbstractUser> users, Pageable pageable) {
        return new PageImpl<>(changeType(users.getContent()), pageable, users.getTotalElements());
    }

    public List<UserDto> changeType(List<AbstractUser> users) {
        return users.stream().map(UserDto::fromAbstractUser).toList();
    }

}
