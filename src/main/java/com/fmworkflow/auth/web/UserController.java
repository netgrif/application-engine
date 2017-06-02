package com.fmworkflow.auth.web;

import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.auth.service.interfaces.IUserService;
import com.fmworkflow.auth.web.responsebodies.UserResource;
import com.fmworkflow.auth.web.responsebodies.UsersResource;
import com.fmworkflow.workflow.web.responsebodies.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/res/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UserResource getUser(@PathVariable("id") Long userId) {
        return new UserResource(userService.findById(userId));
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public UserResource getLoggedUser(Authentication auth) {
        return getUser(((LoggedUser) auth.getPrincipal()).getId());
    }

    @RequestMapping(value = "/{id}/small", method = RequestMethod.GET)
    public UserResource getSmallUser(@PathVariable("id") Long userId) {
        return new UserResource(userService.findById(userId),true);
    }

    @RequestMapping(method = RequestMethod.GET)
    public UsersResource getAll(Authentication auth) {
        return new UsersResource(userService.findByOrganizations(((LoggedUser) auth.getPrincipal()).getOrganizations()),false);
    }

    @RequestMapping(value = "/small", method = RequestMethod.GET)
    public UsersResource getAllSmall(Authentication auth){
        return new UsersResource(userService.findByOrganizations(((LoggedUser) auth.getPrincipal()).getOrganizations()),true);
    }

    //TODO: 2.6.2017 edit user profile

    //TODO: 2.6.2017 assign system roles to user
//    @RequestMapping(value = "/{id}/authority/assign", method = RequestMethod.POST)
//    public MessageResource assignRolesToUser()

    //TODO: 2.6.2017 assign process roles to user

    //TODO: 2.6.2017 get all system roles


}
