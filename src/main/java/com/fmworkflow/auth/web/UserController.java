package com.fmworkflow.auth.web;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.service.SecurityService;
import com.fmworkflow.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registration(@RequestParam(name = "email") String email, @RequestParam(name = "login") String login, @RequestParam(name = "password") String password) {
        User user = new User(login, password, email);
        userService.save(user);
        securityService.autologin(user.getUsername(), user.getPassword());

        return "forward:/welcome";
    }

//    @RequestMapping("/user")
//    @ResponseBody
//    public User user(User user) {
//        return user;
//    }
}
