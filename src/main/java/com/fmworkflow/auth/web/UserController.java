package com.fmworkflow.auth.web;

import com.fmworkflow.auth.service.SecurityService;
import com.fmworkflow.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String registration(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "surname") String surname,
            @RequestParam(name = "password") String password) {

        return "forward:/welcome";
    }
}
