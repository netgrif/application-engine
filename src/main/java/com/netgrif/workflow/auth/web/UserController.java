package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.UserSearchRequestBody;
import com.netgrif.workflow.auth.web.responsebodies.AuthoritiesResources;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import com.netgrif.workflow.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.workflow.auth.web.responsebodies.UsersResource;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.settings.domain.Preferences;
import com.netgrif.workflow.settings.service.IPreferencesService;
import com.netgrif.workflow.settings.web.PreferencesResource;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import com.netgrif.workflow.workflow.web.responsebodies.ResourceLinkAssembler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = Logger.getLogger(UserController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private IPreferencesService preferencesService;

    @GetMapping
    public PagedResources<UserResource> getAll(@RequestParam(value = "small", required = false) Boolean small, Pageable pageable, PagedResourcesAssembler<User> assembler, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        Page<User> page = userService.findAllCoMembers(((LoggedUser) auth.getPrincipal()), small, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .getAll(small, pageable, assembler, auth, locale)).withRel("all");
        PagedResources<UserResource> resources = assembler.toResource(page, new UserResourceAssembler(locale, small, "all"), selfLink);
        ResourceLinkAssembler.addLinks(resources, User.class, selfLink.getRel());
        return resources;
    }

    @PostMapping(value = "/search")
    public PagedResources<UserResource> search(@RequestParam(value = "small", required = false) Boolean small, @RequestBody UserSearchRequestBody query, Pageable pageable, PagedResourcesAssembler<User> assembler, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        Page<User> page = userService.searchAllCoMembers(query.getFulltext(), query.getRoles(), ((LoggedUser) auth.getPrincipal()), small, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
                .search(small, query, pageable, assembler, auth, locale)).withRel("search");
        PagedResources<UserResource> resources = assembler.toResource(page, new UserResourceAssembler(locale, small, "search"), selfLink);
        ResourceLinkAssembler.addLinks(resources, User.class, selfLink.getRel());
        return resources;
    }

    @GetMapping("/{id}")
    public UserResource getUser(@PathVariable("id") Long userId, @RequestParam(value = "small", required = false) Boolean small, Locale locale) {
        small = small == null ? false : small;
        return new UserResource(userService.findById(userId, small), "profile", locale, small);
    }

    @GetMapping("/me")
    public UserResource getLoggedUser(@RequestParam(value = "small", required = false) Boolean small, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        if (!small)
            return new UserResource(userService.findById(((LoggedUser) auth.getPrincipal()).getId(), false), "profile", locale);
        else
            return new UserResource(((LoggedUser) auth.getPrincipal()).transformToUser(), "profile", locale);
    }

    @PostMapping("/{id}")
    public UsersResource updateUser(@PathVariable("id") Long userId, @RequestBody UpdateUserRequest updates, Authentication auth, Locale locale) {
        LoggedUser logged = (LoggedUser) auth.getPrincipal();
        if (!logged.isAdmin() && !Objects.equals(logged.getId(), userId)) {
            return null;
        }
        if (!logged.isAdmin() && Objects.equals(logged.getId(), userId)) {
            return null; //TODO update user
        } else {
            return null; //TODO update user
        }
    }

    @PostMapping("/role")
    public PagedResources<UserResource> getAllWithRole(@RequestBody Set<String> roleIds, @RequestParam(value = "small", required = false) Boolean small, Pageable pageable, PagedResourcesAssembler<User> assembler, Locale locale) {
        small = small == null ? false : small;
        Page<User> page = userService.findAllActiveByProcessRoles(roleIds, small, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(UserController.class)
            .getAllWithRole(roleIds, small, pageable, assembler, locale)).withRel("role");
        PagedResources<UserResource> resources = assembler.toResource(page, new UserResourceAssembler(locale, small, "role"), selfLink);
        ResourceLinkAssembler.addLinks(resources, User.class, selfLink.getRel());
        return resources;
    }

    @PostMapping("/{id}/role/assign")
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
    @GetMapping("/authority")
    public AuthoritiesResources getAllAuthorities(Authentication auth) {
        return new AuthoritiesResources(authorityService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/authority/assign")
    public MessageResource assignAuthorityToUser(@PathVariable("id") Long userId, @RequestBody Long authorityId) {
        userService.assignAuthority(userId, authorityId);
        return MessageResource.successMessage("Authority " + authorityId + " assigned to user " + userId);
    }

    @GetMapping("/preferences")
    public PreferencesResource preferences(Authentication auth) {
        Long userId = ((LoggedUser)auth.getPrincipal()).getId();
        Preferences preferences = preferencesService.get(userId);

        if (preferences == null) {
            preferences = new Preferences(userId);
        }

        return new PreferencesResource(preferences);
    }

    @PostMapping("/preferences")
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