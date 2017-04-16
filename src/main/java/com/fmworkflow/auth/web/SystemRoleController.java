package com.fmworkflow.auth.web;

import com.fmworkflow.auth.service.interfaces.IRoleService;
import com.fmworkflow.auth.service.interfaces.IUserService;
import com.fmworkflow.auth.web.responsebodies.UsersSystemRolesListResponse;
import com.fmworkflow.json.JsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/res/systemrole")
public class SystemRoleController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IRoleService roleService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public
    @ResponseBody
    UsersSystemRolesListResponse getUsersAndRoles() {
        UsersSystemRolesListResponse response = new UsersSystemRolesListResponse();
        response.setUsers(userService.findAll());
        response.setRoles(roleService.findAll());
        return response;
    }

    @RequestMapping(value = "/assign", method = RequestMethod.POST)
    public @ResponseBody String assignRoleToUser(@RequestParam("email") String userEmail, @RequestParam("roleId") Long roleId) {
        try {
            userService.assignRole(userEmail, roleId);
            return JsonBuilder.successMessage("Role succesfully assigned to user " + userEmail);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("Unable to assign role to user " + userEmail);
        }
    }
}
