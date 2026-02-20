package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.adapter.spring.common.web.responsebodies.ResponseMessage;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.*;
import com.netgrif.application.engine.auth.web.requestbodies.PreferencesRequest;
import com.netgrif.application.engine.auth.web.requestbodies.UserCreateRequest;
import com.netgrif.application.engine.auth.web.requestbodies.UserSearchRequestBody;
import com.netgrif.application.engine.auth.web.responsebodies.PreferencesResource;
import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.preferences.Preferences;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/users")
@Tag(name = "UserController")
@ConditionalOnProperty(
        value = "netgrif.engine.user.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProcessRoleService processRoleService;
    private final PreferencesService preferencesService;
    private final AuthorityService authorityService;
    private final RealmService realmService;
    private final UserFactory userFactory;

    @Operation(summary = "Create a new user", description = "Creates a new user in the realm specified by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "409", description = "Conflict – user already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{realmId}")
    public ResponseEntity<User> createUser(@PathVariable String realmId, @RequestBody UserCreateRequest request, Locale locale) {
        try {
            if (!realmExists(realmId)) {
                log.error("Realm with id [{}] not found", realmId);
                return ResponseEntity.badRequest().build();
            }
            if (userService.findUserByUsername(request.getUsername(), realmId).isPresent()) {
                log.error("User with username [{}] already exists in realm [{}]", request.getUsername(), realmId);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            AbstractUser user = userService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPassword(),
                    realmId
            );
            log.info("New user with username [{}] has been created in realm [{}]", request.getUsername(), realmId);
            return ResponseEntity.status(HttpStatus.CREATED).body(userFactory.getUser(user, locale));
        } catch (Exception e) {
            log.error("Failed to create user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get page of users from realm", description = "Retrieves page of users from defined realm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{realmId}/all")
    public ResponseEntity<Page<User>> getAllUsers(@PathVariable String realmId, Pageable pageable, Locale locale) {
        if (!realmExists(realmId)) {
            log.error("Realm with id [{}] not found", realmId);
            return ResponseEntity.badRequest().build();
        }
        Page<AbstractUser> users = userService.findAllUsers(realmId, pageable);
        return ResponseEntity.ok(changeToResponse(users, pageable, locale));
    }

    @Operation(summary = "Get logged user", description = "Retrieves information of currently logged user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getLoggedUser(Authentication auth, Locale locale) {
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

        return ResponseEntity.ok(userFactory.getUser(user, locale));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Generic user search", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<User>> search(@RequestBody UserSearchRequestBody query, Pageable pageable, Authentication auth, Locale locale) {
        List<ProcessResourceId> roles = query.getRoles() == null ? null : query.getRoles().stream().map(ProcessResourceId::new).toList();
        List<ProcessResourceId> negativeRoles = query.getNegativeRoles() == null ? null : query.getNegativeRoles().stream().map(ProcessResourceId::new).toList();
        Page<AbstractUser> users = userService.searchAllCoMembers(query.getFulltext(),
                roles,
                negativeRoles,
                (LoggedUser) auth.getPrincipal(), pageable);
        return ResponseEntity.ok(changeToResponse(users, pageable, locale));
    }

    @Operation(summary = "Get user by id", description = "Retrieves information of user defined by given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "User with given id does not exist in given realm"),
            @ApiResponse(responseCode = "401", description = "User trying to retrieved information is not admin"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/{realmId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUser(@PathVariable("realmId") String realmId, @PathVariable("id") String userId, Locale locale) {
        LoggedUser actualUser = userService.getLoggedUserFromContext();
        // TODO: impersonation
//        LoggedUser loggedUser = actualUser.getSelfOrImpersonated();
        LoggedUser loggedUser = actualUser;
        if (!loggedUser.isAdmin() && !Objects.equals(loggedUser.getId(), userId)) {
            log.info("User [{}] trying to get another user with ID [{}]", actualUser.getUsername(), userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AbstractUser user;
        try {
            user = userService.findById(userId, realmId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(userFactory.getUser(user, locale));
    }

//    todo step 2, only used in test on frontend
//    @Operation(summary = "Update user", security = {@SecurityRequirement(name = "X-Auth-Token")})
//    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<User> updateUser(@RequestBody UpdateUserRequest updates, Authentication auth, Locale locale) {

    /// /        todo should this be kept? not relevant anymore?
//        if (!serverAuthProperties.isEnableProfileEdit()) {
//            return null;
//        }
//        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
//        String actorId = updates.getStringId();
//        IUser user;
//        try {
//            user = userService.findById(actorId, updatedUser.getRealmId());
//        } catch (IllegalArgumentException e) {
//            log.error("Could not find user with id [{}]", actorId, e);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//        }
//        user = userService.update(user, updates.getUpdatedUser());
//        securityContextService.saveToken(actorId);
//        if (Objects.equals(loggedUser.getId(), actorId)) {
//            loggedUser.setFirstName(user.getFirstName());
//            loggedUser.setLastName(user.getLastName());
//            securityContextService.reloadSecurityContext(loggedUser);
//        }
//        log.info("Updating user " + user.getEmail() + " with data " + updatedUser);
//        return ResponseEntity.ok(User.createUser(user));
//    }

//    todo not used on front, is it needed?
//    @Operation(summary = "Get all users with specified roles", security = {@SecurityRequirement(name = "X-Auth-Token")})
//    @PostMapping(value = "/role", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<Page<IUser>> getAllWithRole(@RequestBody Set<String> roleIds, Pageable pageable, Locale locale) {
//        Set<ProcessResourceId> roleResourceIds = roleIds == null ? null : roleIds.stream().map(ProcessResourceId::new).collect(Collectors.toSet());
//        Page<IUser> page = userService.findAllActiveByProcessRoles(roleResourceIds, pageable);
//        return ResponseEntity.ok();
//    }
    @Operation(summary = "Assign roles to the user", description = "Caller must have the ADMIN role", security = {@SecurityRequirement(name = "X-Auth-Token")})
    @PutMapping(value = "/{realmId}/{id}/roles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Selected roles assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Requested roles or user with defined id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseMessage> assignRolesToUser(@PathVariable("realmId") String realmId, @PathVariable("id") String userId, @RequestBody Set<String> roleIds, Authentication auth) {
        try {
            AbstractUser user = userService.findById(userId, realmId);
            processRoleService.assignRolesToUser(user, roleIds.stream().map(ProcessResourceId::new).collect(Collectors.toSet()), (LoggedUser) auth.getPrincipal());
            log.info("Process roles {} assigned to user with id [{}]", roleIds, userId);
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected roles assigned to user " + userId));
        } catch (IllegalArgumentException e) {
            String message = "Assigning roles to user [" + userId + "] has failed!";
            log.error(message, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Assigning roles to user " + userId + " has failed!"));
        }
    }

//
//    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
//    @Operation(summary = "Assign negative roles to the user", description = "Caller must have the ADMIN role", security = {@SecurityRequirement(name = "X-Auth-Token")})
//    @PutMapping(value = "/{realmId}/{id}/negativeRole", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Selected negative roles assigned successfully"),
//            @ApiResponse(responseCode = "400", description = "Requested roles or user with defined id does not exist"),
//            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
//            @ApiResponse(responseCode = "500", description = "Internal server error")
//    })
//    public ResponseEntity<ResponseMessage> assignNegativeRolesToUser(@PathVariable("realmId") String realmId, @PathVariable("id") String actorId, @RequestBody Set<String> roleIds, Authentication auth) {
//        try {
//            AbstractUser user = userService.findById(actorId, realmId);
//            processRoleService.assignNegativeRolesToUser(user, roleIds.stream().map(ProcessResourceId::new).collect(Collectors.toSet()), (LoggedUser) auth.getPrincipal());
//            log.info("Negative process roles {} assigned to user [{}]", roleIds, actorId);
//            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("Selected negative roles assigned to user " + actorId));
//        } catch (IllegalArgumentException e) {
//            log.error("Assigning negative roles to user with id [{}] has failed!", actorId, e);
//            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Assigning negative roles to user " + actorId + " has failed!"));
//        }
//    }
//
    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Get all authorities of the system",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @GetMapping(value = "/authority", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Authority>> getAllAuthorities() {
        return ResponseEntity.ok(authorityService.findAll(Pageable.unpaged()).stream().toList());
    }

    @PreAuthorize("@authorizationServiceImpl.hasAuthority('ADMIN')")
    @Operation(summary = "Assign authority to the user",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "X-Auth-Token")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authority was assigned to user successfully"),
            @ApiResponse(responseCode = "400", description = "Authority with given id or user with given id does not exist"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/{realmId}/{id}/authority", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> assignAuthorityToUser(@PathVariable("realmId") String realmId, @PathVariable("id") String userId, @RequestBody String authorityId) {
        try {
            userService.assignAuthority(userId, realmId, authorityId);
        } catch (IllegalArgumentException e) {
            log.error("Assigning authority to user [{}] has failed!", userId, e);
            return ResponseEntity.badRequest().body(ResponseMessage.createSuccessMessage("Assigning authority to user " + userId + " has failed!"));
        }
        return ResponseEntity.ok(ResponseMessage.createErrorMessage("Authority was assigned to user successfully"));
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
    public ResponseEntity<ResponseMessage> savePreferences(@RequestBody PreferencesRequest preferences, Authentication auth) {
        try {
            String userId = ((LoggedUser) auth.getPrincipal()).getStringId();
            preferences.setUserId(userId);
            preferencesService.save(preferences.toPreferences());
            return ResponseEntity.ok(ResponseMessage.createSuccessMessage("User preferences saved"));
        } catch (Exception e) {
            log.error("Saving user preferences failed", e);
            return ResponseEntity.badRequest().body(ResponseMessage.createErrorMessage("Saving user preferences failed"));
        }
    }

    private Page<User> changeToResponse(Page<AbstractUser> users, Pageable pageable, Locale locale) {
        return new PageImpl<>(changeType(users.getContent(), locale), pageable, users.getTotalElements());
    }

    public List<User> changeType(List<AbstractUser> users, Locale locale) {
        return users.stream().map(u -> userFactory.getUser(u, locale)).toList();
    }

    private boolean realmExists(String realmId) {
        Optional<Realm> realm = realmService.getRealmById(realmId);
        return realm.isPresent();
    }
}