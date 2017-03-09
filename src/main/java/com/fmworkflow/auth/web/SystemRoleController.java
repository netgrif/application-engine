package com.fmworkflow.auth.web;

import com.fmworkflow.auth.service.IRoleService;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.auth.web.responsebodies.UsersSystemRolesListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
}
