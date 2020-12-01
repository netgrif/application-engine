package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.workflow.auth.service.UserDetailsServiceImpl;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.UserSearchRequestBody;
import com.netgrif.workflow.auth.web.responsebodies.AuthoritiesResources;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import com.netgrif.workflow.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.workflow.configuration.properties.ServerAuthProperties;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.settings.domain.Preferences;
import com.netgrif.workflow.settings.service.IPreferencesService;
import com.netgrif.workflow.settings.web.PreferencesResource;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import com.netgrif.workflow.workflow.web.responsebodies.ResourceLinkAssembler;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
@ConditionalOnProperty(
        value = "nae.user.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"User"})
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private IPreferencesService preferencesService;

    @Autowired
    private ServerAuthProperties serverAuthProperties;

    @ApiOperation(value = "Get all users", authorizations = @Authorization("BasicAuth"))
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<UserResource> getAll(@RequestParam(value = "small", required = false) Boolean small, Pageable pageable, PagedResourcesAssembler<User> assembler, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        Page<User> page = userService.findAllCoMembers(((LoggedUser) auth.getPrincipal()), small, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .getAll(small, pageable, assembler, auth, locale)).withRel("all");
        PagedResources<UserResource> resources = assembler.toResource(page, new UserResourceAssembler(locale, small, "all"), selfLink);
        ResourceLinkAssembler.addLinks(resources, User.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Generic user search", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<UserResource> search(@RequestParam(value = "small", required = false) Boolean small, @RequestBody UserSearchRequestBody query, Pageable pageable, PagedResourcesAssembler<User> assembler, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        Page<User> page = userService.searchAllCoMembers(query.getFulltext(), query.getRoles(), ((LoggedUser) auth.getPrincipal()), small, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .search(small, query, pageable, assembler, auth, locale)).withRel("search");
        PagedResources<UserResource> resources = assembler.toResource(page, new UserResourceAssembler(locale, small, "search"), selfLink);
        ResourceLinkAssembler.addLinks(resources, User.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Get user by id", authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource getUser(@PathVariable("id") Long userId, @RequestParam(value = "small", required = false) Boolean small, Locale locale) {
        small = small == null ? false : small;
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!loggedUser.isAdmin() && !Objects.equals(loggedUser.getId(), userId)){
            log.info("User " + loggedUser.getUsername() + " trying to get another user with ID "+userId);
            throw new IllegalArgumentException("Could not find user with id ["+userId+"]");
        }
        return new UserResource(userService.findById(userId, small), "profile", locale, small);
    }

    @ApiOperation(value = "Get logged user", authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/me", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource getLoggedUser(@RequestParam(value = "small", required = false) Boolean small, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        if (!small)
            return new UserResource(userService.findById(((LoggedUser) auth.getPrincipal()).getId(), false), "profile", locale);
        else
            return new UserResource(((LoggedUser) auth.getPrincipal()).transformToUser(), "profile", locale);
    }

    @ApiOperation(value = "Update user", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource updateUser(@PathVariable("id") Long userId, @RequestBody UpdateUserRequest updates, Authentication auth, Locale locale) throws UnauthorisedRequestException {
        if (!serverAuthProperties.isEnableProfileEdit()) return null;

        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        User user = userService.findById(userId, false);
        if (user == null || (!loggedUser.isAdmin() && !Objects.equals(loggedUser.getId(), userId)))
            throw new UnauthorisedRequestException("User " + loggedUser.getUsername() + " doesn't have permission to modify profile of " + user.transformToLoggedUser().getUsername());

        user = userService.update(user, updates);
        if (Objects.equals(loggedUser.getId(), userId)) {
            loggedUser.setFullName(user.getFullName());
            userDetailsService.reloadSecurityContext(loggedUser);
        }
        log.info("Updating user " + user.getEmail() + " with data " + updates.toString());
        return new UserResource(user, "profile", locale);
    }

    @ApiOperation(value = "Get all users with specified roles", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/role", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<UserResource> getAllWithRole(@RequestBody Set<String> roleIds, @RequestParam(value = "small", required = false) Boolean small, Pageable pageable, PagedResourcesAssembler<User> assembler, Locale locale) {
        small = small == null ? false : small;
        Page<User> page = userService.findAllActiveByProcessRoles(roleIds, small, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .getAllWithRole(roleIds, small, pageable, assembler, locale)).withRel("role");
        PagedResources<UserResource> resources = assembler.toResource(page, new UserResourceAssembler(locale, small, "role"), selfLink);
        ResourceLinkAssembler.addLinks(resources, User.class, selfLink.getRel());
        return resources;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Assign role to the user",
            notes = "Caller must have the ADMIN role",
            authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/{id}/role/assign", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource assignRolesToUser(@PathVariable("id") Long userId, @RequestBody Set<String> roleIds, Authentication auth) {
        try {
            processRoleService.assignRolesToUser(userId, roleIds, (LoggedUser) auth.getPrincipal());
            log.info("Process roles " + roleIds + " assigned to user " + userId);
            return MessageResource.successMessage("Selected roles assigned to user " + userId);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Assigning roles to user " + userId + " has failed!");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Get all authorities of the system",
            notes = "Caller must have the ADMIN role",
            authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/authority", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public AuthoritiesResources getAllAuthorities(Authentication auth) {
        return new AuthoritiesResources(authorityService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Assign authority to the user",
            notes = "Caller must have the ADMIN role",
            authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/{id}/authority/assign", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource assignAuthorityToUser(@PathVariable("id") Long userId, @RequestBody String authorityId) {
        Long authority = authorityId != null ? Long.parseLong(authorityId) : null;
        userService.assignAuthority(userId, authority);
        return MessageResource.successMessage("Authority " + authority + " assigned to user " + userId);
    }

    @ApiOperation(value = "Get user's preferences", authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/preferences", produces = MediaTypes.HAL_JSON_VALUE)
    public PreferencesResource preferences(Authentication auth) {
        Long userId = ((LoggedUser) auth.getPrincipal()).getId();
        Preferences preferences = preferencesService.get(userId);

        if (preferences == null) {
            preferences = new Preferences(userId);
        }

        return new PreferencesResource(preferences);
    }

    @ApiOperation(value = "Set user's preferences", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/preferences", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource savePreferences(@RequestBody Preferences preferences, Authentication auth) {
        try {
            Long userId = ((LoggedUser) auth.getPrincipal()).getId();
            preferences.setUserId(userId);
            preferencesService.save(preferences);
            return MessageResource.successMessage("User preferences saved");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MessageResource.errorMessage("Saving user preferences failed");
        }
    }
}