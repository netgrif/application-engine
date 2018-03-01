package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.responsebodies.AuthoritiesResources;
import com.netgrif.workflow.auth.web.responsebodies.OrganizationsResource;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import com.netgrif.workflow.auth.web.responsebodies.UsersResource;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/res/user")
public class UserController {

    private static final Logger log = Logger.getLogger(UserController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private IAuthorityService authorityService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UserResource getUser(@PathVariable("id") Long userId, Locale locale) {
        return new UserResource(userService.findById(userId, false), "profile", locale);
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public UserResource getLoggedUser(Authentication auth, Locale locale) {
        return new UserResource(userService.findById(((LoggedUser) auth.getPrincipal()).getId(), false), "profile", locale);
    }

    @RequestMapping(value = "/{id}/small", method = RequestMethod.GET)
    public UserResource getSmallUser(@PathVariable("id") Long userId, Locale locale) {
        return new UserResource(userService.findById(userId, true), "small", locale, true);
    }

//    @RequestMapping(method = RequestMethod.GET)
//    public UsersResource getAll(Authentication auth, Locale locale) {
//        return new UsersResource(userService.findByOrganizations(((LoggedUser) auth.getPrincipal()).getOrganizations(), false), "all", locale, false);
//    }
//
//    @RequestMapping(value = "/small", method = RequestMethod.GET)
//    public UsersResource getAllSmall(Authentication auth, Locale locale) {
//        return new UsersResource(userService.findByOrganizations(((LoggedUser) auth.getPrincipal()).getOrganizations(), true), "small", locale, true);
//    }

    @RequestMapping(value = "/role/small", method = RequestMethod.POST)
    public UsersResource getAllWithRole(@RequestBody Set<String> roleIds, Locale locale) {
        return new UsersResource(userService.findByProcessRoles(roleIds, true), "small", locale, true);
    }

    //TODO: 2.6.2017 edit user profile


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

//    @PreAuthorize("hasRole('ADMIN')")
//    @RequestMapping(value = "/organizations", method = RequestMethod.GET)
//    public OrganizationsResource getAllOrganizations() {
//        return new OrganizationsResource(userService.getAllOrganizations());
//    }
}