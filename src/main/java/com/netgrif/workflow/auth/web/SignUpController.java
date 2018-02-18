package com.netgrif.workflow.auth.web;

import com.netgrif.workflow.auth.domain.UnactivatedUser;
import com.netgrif.workflow.auth.service.interfaces.IUnactivatedUserService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.workflow.mail.IMailService;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/signup")
public class SignUpController {

    private static final Logger log = LoggerFactory.getLogger(SignUpController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private IUnactivatedUserService unactivatedUserService;

    @Autowired
    private IMailService mailService;

    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public ModelAndView registrationForward() throws IOException {
        log.info("Forwarding to / from /signup");
        return new ModelAndView("forward:/");
    }

    @RequestMapping(method = RequestMethod.POST)
    public MessageResource registration(@RequestBody RegistrationRequest regRequest) {
        if(!unactivatedUserService.authorizeToken(regRequest.email, regRequest.token))
            return MessageResource.errorMessage("Registration of " + regRequest.email + " has failed! Invalid token!");

        regRequest.password = new String(Base64.getDecoder().decode(regRequest.password));
        userService.saveNew(unactivatedUserService.createUser(regRequest));

        return MessageResource.successMessage("Registration complete");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/invite", method = RequestMethod.POST)
    public MessageResource invite(@RequestBody NewUserRequest newUserRequest) {
        try {
            newUserRequest.email = URLDecoder.decode(newUserRequest.email, StandardCharsets.UTF_8.name());
            UnactivatedUser user = unactivatedUserService.createUnactivatedUser(newUserRequest);
            mailService.sendRegistrationEmail(user.getEmail(), user.getToken());

            return MessageResource.successMessage("Mail was sent to " + user.getEmail());
        } catch (IOException | TemplateException | MessagingException e) {
            log.error(e.toString());
            return MessageResource.errorMessage("Sending mail to " + newUserRequest.email + " failed!");
        }
    }

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public MessageResource getEmail(@RequestBody String token) {
        String email =  unactivatedUserService.getEmail(token);
        return email != null ? MessageResource.successMessage(email) : MessageResource.errorMessage("Bad token!");
    }


}