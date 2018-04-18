package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.workflow.auth.web.responsebodies.AuthoritiesResources;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import com.netgrif.workflow.auth.web.responsebodies.UsersResource;
import com.netgrif.workflow.event.events.usecase.UpdateMarkingEvent;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

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

    @RequestMapping(method = RequestMethod.GET)
    public UsersResource getAll(@RequestParam(value = "small", required = false) Boolean small, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        Set<User> coMembers = userService.findAllCoMembers(((LoggedUser) auth.getPrincipal()).getUsername(), small);
        coMembers.add(userService.findById(((LoggedUser) auth.getPrincipal()).getId(), small));
        return new UsersResource(coMembers, "all", locale, small);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UserResource getUser(@PathVariable("id") Long userId, @RequestParam(value = "small", required = false) Boolean small, Locale locale) {
        small = small == null ? false : small;
        return new UserResource(userService.findById(userId, small), "profile", locale, small);
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public UserResource getLoggedUser(@RequestParam(value = "small", required = false) Boolean small, Authentication auth, Locale locale) {
        small = small == null ? false : small;
        if (!small)
            return new UserResource(userService.findById(((LoggedUser) auth.getPrincipal()).getId(), false), "profile", locale);
        else
            return new UserResource(((LoggedUser) auth.getPrincipal()).transformToUser(), "profile", locale);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public UsersResource updateUser(@PathVariable("id") Long userId, @RequestBody UpdateUserRequest updates, Authentication auth, Locale locale) {
        LoggedUser logged = (LoggedUser) auth.getPrincipal();
        if (!logged.isAdmin() && !Objects.equals(logged.getId(), userId)) {
            return null;
        }
        if(!logged.isAdmin() && Objects.equals(logged.getId(), userId)){
            return null; //TODO update user
        } else {
            return null; //TODO update user
        }
    }

    @RequestMapping(value = "/role", method = RequestMethod.POST)
    public UsersResource getAllWithRole(@RequestBody Set<String> roleIds, @RequestParam(value = "small", required = false) Boolean small, Locale locale) {
        small = small == null ? false : small;
        return new UsersResource(userService.findByProcessRoles(roleIds, small), "small", locale, small);
    }

    @RequestMapping(value = "/{id}/role/assign", method = RequestMethod.POST)
    public MessageResource assignRolesToUser(@PathVariable("id") Long userId, @RequestBody Set<String> roleIds) {
        try {
            processRoleService.assignRolesToUser(userId, roleIds);
            log.info("Process roles " + roleIds + " assigned to user " + userId);
            return MessageResource.successMessage("Selected roles assigned to user " + userId);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Assigning roles to user " + userId + " has failed!");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/authority", method = RequestMethod.GET)
    public AuthoritiesResources getAllAuthorities(Authentication auth) {
        return new AuthoritiesResources(authorityService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{id}/authority/assign", method = RequestMethod.POST)
    public MessageResource assignAuthorityToUser(@PathVariable("id") Long userId, @RequestBody Long authorityId) {
        userService.assignAuthority(userId, authorityId);
        return MessageResource.successMessage("Authority " + authorityId + " assigned to user " + userId);
    }
}