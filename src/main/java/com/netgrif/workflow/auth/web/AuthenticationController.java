package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IRegistrationService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.workflow.auth.web.responsebodies.UserResource;
import com.netgrif.workflow.mail.IMailService;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private IRegistrationService registrationService;

    @Autowired
    private IMailService mailService;

    @RequestMapping(value = "/signup/{token}", method = RequestMethod.GET)
    public ModelAndView registrationForward() throws IOException {
        log.info("Forwarding to / from /signup");
        return new ModelAndView("forward:/");
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public MessageResource signup(@RequestBody RegistrationRequest regRequest) {
        if (!registrationService.verifyToken(regRequest.email, regRequest.token))
            return MessageResource.errorMessage("Registration of " + regRequest.email + " has failed! Invalid token!");

        regRequest.password = new String(Base64.getDecoder().decode(regRequest.password));
        User user = registrationService.registerUser(regRequest);
        if (user == null)
            return MessageResource.errorMessage("Registration of " + regRequest.email + " has failed! No user with this email was found.");

        return MessageResource.successMessage("Registration complete");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/invite", method = RequestMethod.POST)
    public MessageResource invite(@RequestBody NewUserRequest newUserRequest) {
        try {
            newUserRequest.email = URLDecoder.decode(newUserRequest.email, StandardCharsets.UTF_8.name());
            User user = registrationService.createNewUser(newUserRequest);
            mailService.sendRegistrationEmail(user.getEmail(), user.getToken());

            return MessageResource.successMessage("Mail was sent to " + user.getEmail());
        } catch (IOException | TemplateException | MessagingException e) {
            log.error(e.toString());
            return MessageResource.errorMessage("Sending mail to " + newUserRequest.email + " failed!");
        }
    }

    @RequestMapping(value = "/signup/verify", method = RequestMethod.POST)
    public MessageResource verifyToken(@RequestBody String token) {
        String email = registrationService.getEmailToToken(token);
        return email != null ? MessageResource.successMessage(email) : MessageResource.errorMessage("Invalid token!");
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public UserResource login(Authentication auth, Locale locale){
        return new UserResource(((LoggedUser) auth.getPrincipal()).transformToUser(), "profile", locale);
    }


}