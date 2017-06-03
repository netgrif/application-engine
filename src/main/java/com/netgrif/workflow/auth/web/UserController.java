package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IRoleService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.responsebodies.AuthoritiesResources;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import com.netgrif.workflow.auth.web.responsebodies.UsersResource;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.petrinet.web.PetriNetController;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/res/user")
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IProcessRoleService processRoleService;
    @Autowired
    private IRoleService roleService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UserResource getUser(@PathVariable("id") Long userId) {
        return new UserResource(userService.findById(userId),"profile");
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public UserResource getLoggedUser(Authentication auth) {
        return new UserResource(userService.findById(((LoggedUser) auth.getPrincipal()).getId()),"profile");
    }

    @RequestMapping(value = "/{id}/small", method = RequestMethod.GET)
    public UserResource getSmallUser(@PathVariable("id") Long userId) {
        return new UserResource(userService.findById(userId), "small",true);
    }

    @RequestMapping(method = RequestMethod.GET)
    public UsersResource getAll(Authentication auth) {
        return new UsersResource(userService.findByOrganizations(((LoggedUser) auth.getPrincipal()).getOrganizations()), "all",false);
    }

    @RequestMapping(value = "/small", method = RequestMethod.GET)
    public UsersResource getAllSmall(Authentication auth) {
        return new UsersResource(userService.findByOrganizations(((LoggedUser) auth.getPrincipal()).getOrganizations()), "small",true);
    }

    @RequestMapping(value = "/role/{id}/small", method = RequestMethod.GET)
    public UsersResource getAllWithRole(@PathVariable("id") String roleId) {
        return new UsersResource(userService.findByProcessRole(PetriNetController.decodeUrl(roleId)), "small",true);
    }

    //TODO: 2.6.2017 edit user profile


    @RequestMapping(value = "/{id}/role/assign", method = RequestMethod.POST)
    public MessageResource assignRolesToUser(@PathVariable("id") Long userId, @RequestBody Set<String> roleIds){
        if(processRoleService.assignRolesToUser(userId,roleIds))
            return MessageResource.successMessage("Selected roles assigned to user "+userId);
        else
            return MessageResource.errorMessage("Assigning roles to user "+userId+" has failed!");
    }

    @RequestMapping(value = "/authority", method = RequestMethod.GET)
    public AuthoritiesResources getAllAuthorities(Authentication auth){
        if(((LoggedUser)auth.getPrincipal()).getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("admin")))
            return new AuthoritiesResources(roleService.findAll());
        else
            return new AuthoritiesResources();
    }

    @RequestMapping(value = "/{id}/authority/assign", method = RequestMethod.POST)
    public MessageResource assignAuthorityToUser(@PathVariable("id") Long userId, @RequestBody Long authorityId){
        userService.assignRole(userId,authorityId);
        return MessageResource.successMessage("Role "+authorityId+" assigned to user "+userId);
    }


}
