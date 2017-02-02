package com.fmworkflow.auth.web;

import com.fmworkflow.auth.domain.Token;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.service.ITokenService;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.mail.IMailService;
import org.hibernate.validator.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/login")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private ITokenService tokenService;

    @Autowired
    private IMailService mailService;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String registration(
            @RequestParam(name = "token") String token,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "surname") String surname,
            @RequestParam(name = "password") String password) {
        if (tokenService.authorizeToken(email, token)) {
            User user = new User(email, password, name, surname);
            userService.save(user);

            return JsonBuilder.init()
                    .addSuccessMessage("fici to")
                    .build();
        } else {
            return JsonBuilder.init()
                    .addErrorMessage("nefici to")
                    .build();
        }
    }

    @RequestMapping(value = "/invite", method = RequestMethod.POST)
    public String invite(@RequestParam(name = "email") @Email String email) {
        try {
            Token token = tokenService.createToken(email);
            mailService.sendRegistrationEmail(email, token.getHashedToken());

            return JsonBuilder.init()
                    .addSuccessMessage("Mail sent")
                    .build();
        } catch (MessagingException e) {
            log.error(e.toString());
            return JsonBuilder.init()
                    .addErrorMessage("Sending mail unsuccessful")
                    .build();
        }
    }
}
