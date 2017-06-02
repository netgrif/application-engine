package com.fmworkflow.auth.web;

import com.fmworkflow.auth.domain.Token;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.service.interfaces.ITokenService;
import com.fmworkflow.auth.service.interfaces.IUserService;
import com.fmworkflow.auth.web.requestbodies.RegistrationRequest;
import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.mail.IMailService;
import com.fmworkflow.workflow.web.responsebodies.MessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/signup")
public class SignUpController {

    private static final Logger log = LoggerFactory.getLogger(SignUpController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private ITokenService tokenService;

    @Autowired
    private IMailService mailService;

    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public ModelAndView registrationForward() throws IOException {
        log.info("Forwarding to / from /signup");
        return new ModelAndView("forward:/");
    }

    @RequestMapping(method = RequestMethod.POST)
    public MessageResource registration(@RequestBody RegistrationRequest regRequest) {
        if (tokenService.authorizeToken(regRequest.email, regRequest.token)) {
            regRequest.password = new String(Base64.getDecoder().decode(regRequest.password));
            User user = new User(regRequest.email, regRequest.password, regRequest.name, regRequest.surname);
            userService.save(user);

            return MessageResource.successMessage("Registration complete");
        } else {
            return MessageResource.errorMessage("Registration of "+regRequest.email+" failed!");
        }
    }

    @RequestMapping(value = "/invite", method = RequestMethod.POST)
    public MessageResource invite(@RequestBody String email) {
        try {
            String mail = URLDecoder.decode(email.split("=")[1], StandardCharsets.UTF_8.name());
            Token token = tokenService.createToken(mail);
            mailService.sendRegistrationEmail(mail, token.getHashedToken());

            return MessageResource.successMessage("Mail was sent to "+mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error(e.toString());
            return MessageResource.errorMessage("Sending mail to "+email+" failed!");
        }
    }

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public MessageResource getEmail(@RequestBody String token) {
        String email = tokenService.getEmail(token);
        if (email != null) {
            return MessageResource.successMessage(email);
        } else {
            return MessageResource.errorMessage("Bad token!");
        }
    }


}