package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.IUserResourceHelperService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.UserSearchRequestBody;
import com.netgrif.application.engine.auth.web.responsebodies.AuthoritiesResources;
import com.netgrif.application.engine.auth.web.responsebodies.IUserFactory;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.settings.domain.Preferences;
import com.netgrif.application.engine.settings.service.IPreferencesService;
import com.netgrif.application.engine.settings.web.PreferencesResource;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import com.netgrif.application.engine.workflow.web.responsebodies.ResourceLinkAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.inject.Provider;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
@ConditionalOnProperty(
        value = "nae.user.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "User")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserResourceHelperService userResourceHelperService;

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private IPreferencesService preferencesService;

    @Autowired
    private ServerAuthProperties serverAuthProperties;

    @Autowired
    private IUserFactory userResponseFactory;

    @Autowired
    private Provider<UserResourceAssembler> userResourceAssemblerProvider;

    @Autowired
    private ISecurityContextService securityContextService;

    protected UserResourceAssembler getUserResourceAssembler(Locale locale, boolean small, String selfRel) {
        UserResourceAssembler result = userResourceAssemblerProvider.get();
        result.initialize(locale, small, selfRel);
        return result;
    }

    @Operation(summary = "Get all users", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<UserResource> getAll(@RequestParam(value = "small", required = false) Boolean small, Pageable pageable, PagedResourcesAssembler<IUser> assembler, Authentication auth, Locale locale) {
        small = small != null && small;
        Page<IUser> page = userService.findAllCoMembers((LoggedUser) auth.getPrincipal(), small, pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getAll(small, pageable, assembler, auth, locale)).withRel("all");
        PagedModel<UserResource> resources = assembler.toModel(page, getUserResourceAssembler(locale, small, "all"), selfLink);
        ResourceLinkAssembler.addLinks(resources, IUser.class, selfLink.getRel().toString());
        return resources;
    }


    @Operation(summary = "Generic user search", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<UserResource> search(@RequestParam(value = "small", required = false) Boolean small, @RequestBody UserSearchRequestBody query, Pageable pageable, PagedResourcesAssembler<IUser> assembler, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        List<ObjectId> roles = query.getRoles() == null ? null : query.getRoles().stream().map(ObjectId::new).collect(Collectors.toList());
        List<ObjectId> negativeRoles = query.getNegativeRoles() == null ? null : query.getNegativeRoles().stream().map(ObjectId::new).collect(Collectors.toList());
        Page<IUser> page = userService.searchAllCoMembers(query.getFulltext(),
                roles,
                negativeRoles,
                (LoggedUser) auth.getPrincipal(), small, pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .search(small, query, pageable, assembler, auth, locale)).withRel("search");
        PagedModel<UserResource> resources = assembler.toModel(page, getUserResourceAssembler(locale, small, "search"), selfLink);
        ResourceLinkAssembler.addLinks(resources, IUser.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Get user by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource getUser(@PathVariable("id") String userId, @RequestParam(value = "small", required = false) Boolean small, Locale locale) {
        small = small != null && small;
        LoggedUser actualUser = userService.getLoggedUserFromContext();
        LoggedUser loggedUser = actualUser.getSelfOrImpersonated();
        if (!loggedUser.isAdmin() && !Objects.equals(loggedUser.getId(), userId)) {
            log.info("User " + actualUser.getUsername() + " trying to get another user with ID " + userId);
            throw new IllegalArgumentException("Could not find user with id [" + userId + "]");
        }
        IUser user = userService.resolveById(userId, small);
        return new UserResource(small ? userResponseFactory.getSmallUser(user) : userResponseFactory.getUser(user, locale), "profile");
    }

    @Operation(summary = "Get logged user", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/me", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource getLoggedUser(@RequestParam(value = "small", required = false) Boolean small, Authentication auth, Locale locale) {
        small = small != null && small;
        return userResourceHelperService.getResource((LoggedUser) auth.getPrincipal(), locale, small);
    }

    @Operation(summary = "Update user", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource updateUser(@PathVariable("id") String userId, @RequestBody UpdateUserRequest updates, Authentication auth, Locale locale) throws UnauthorisedRequestException {
        if (!serverAuthProperties.isEnableProfileEdit()) return null;

        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        IUser user = userService.resolveById(userId, false);
        if (user == null || (!loggedUser.isAdmin() && !Objects.equals(loggedUser.getId(), userId)))
            throw new UnauthorisedRequestException("User " + loggedUser.getUsername() + " doesn't have permission to modify profile of " + user.transformToLoggedUser().getUsername());

        user = userService.update(user, updates);
        securityContextService.saveToken(userId);
        if (Objects.equals(loggedUser.getId(), userId)) {
            loggedUser.setFullName(user.getFullName());
            securityContextService.reloadSecurityContext(loggedUser);
        }
        log.info("Updating user " + user.getEmail() + " with data " + updates.toString());
        return new UserResource(userResponseFactory.getUser(user, locale), "profile");
    }

    @Operation(summary = "Get all users with specified roles", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/role", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<UserResource> getAllWithRole(@RequestBody Set<String> roleIds, @RequestParam(value = "small", required = false) Boolean small, Pageable pageable, PagedResourcesAssembler<IUser> assembler, Locale locale) {
        small = small == null ? false : small;
        Page<IUser> page = userService.findAllActiveByProcessRoles(roleIds, small, pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getAllWithRole(roleIds, small, pageable, assembler, locale)).withRel("role");
        PagedModel<UserResource> resources = assembler.toModel(page, getUserResourceAssembler(locale, small, "role"), selfLink);
        ResourceLinkAssembler.addLinks(resources, IUser.class, selfLink.getRel().toString());
        return resources;
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Assign role to the user", description = "Caller must have the ADMIN role", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{id}/role/assign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource assignRolesToUser(@PathVariable("id") String userId, @RequestBody Set<String> roleIds, Authentication auth) {
        try {
            processRoleService.assignRolesToUser(userId, roleIds, (LoggedUser) auth.getPrincipal());
            log.info("Process roles " + roleIds + " assigned to user " + userId);
            return MessageResource.successMessage("Selected roles assigned to user " + userId);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Assigning roles to user " + userId + " has failed!");
        }
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Get all authorities of the system",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/authority", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public AuthoritiesResources getAllAuthorities(Authentication auth) {
        return new AuthoritiesResources(authorityService.findAll());
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Assign authority to the user",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{id}/authority/assign", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource assignAuthorityToUser(@PathVariable("id") String userId, @RequestBody String authorityId) {
        userService.assignAuthority(userId, authorityId);
        return MessageResource.successMessage("Authority " + authorityId + " assigned to user " + userId);
    }

    @Operation(summary = "Get user's preferences", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/preferences", produces = MediaTypes.HAL_JSON_VALUE)
    public PreferencesResource preferences(Authentication auth) {
        String userId = ((LoggedUser) auth.getPrincipal()).getId();
        Preferences preferences = preferencesService.get(userId);

        if (preferences == null) {
            preferences = new Preferences(userId);
        }

        return new PreferencesResource(preferences);
    }

    @Operation(summary = "Set user's preferences", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/preferences", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource savePreferences(@RequestBody Preferences preferences, Authentication auth) {
        try {
            String userId = ((LoggedUser) auth.getPrincipal()).getId();
            preferences.setUserId(userId);
            preferencesService.save(preferences);
            return MessageResource.successMessage("User preferences saved");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MessageResource.errorMessage("Saving user preferences failed");
        }
    }
}
