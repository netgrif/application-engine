package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.RegisteredUser;
import com.netgrif.application.engine.auth.service.InvalidUserTokenException;
import com.netgrif.application.engine.auth.service.UserDetailsServiceImpl;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.requestbodies.ChangePasswordRequest;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.auth.web.responsebodies.IUserFactory;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import freemarker.template.TemplateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(
        value = "nae.auth.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Authentication")
public class AuthenticationController {

    @Autowired
    private IRegistrationService registrationService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IMailAttemptService mailAttemptService;

    @Autowired
    private ServerAuthProperties serverAuthProperties;

    @Autowired
    private IUserFactory userResponseFactory;

    @Autowired
    private ISecurityContextService securityContextService;

    @Operation(summary = "New user registration")
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource signup(@RequestBody RegistrationRequest regRequest) {
        try {
            String email = registrationService.decodeToken(regRequest.token)[0];
            if (!registrationService.verifyToken(regRequest.token))
                return MessageResource.errorMessage("Registration of " + email + " has failed! Invalid token!");

            regRequest.password = new String(Base64.getDecoder().decode(regRequest.password));
            RegisteredUser user = registrationService.registerUser(regRequest);
            if (user == null)
                return MessageResource.errorMessage("Registration of " + email + " has failed! No user with this email was found.");

            return MessageResource.successMessage("Registration complete");
        } catch (InvalidUserTokenException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Invalid token!");
        }
    }

    @Operation(summary = "Send invitation to a new user", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/invite", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource invite(@RequestBody NewUserRequest newUserRequest, Authentication auth) {
        try {
            if (!serverAuthProperties.isOpenRegistration() && (auth == null || !((LoggedUser) auth.getPrincipal()).getSelfOrImpersonated().isAdmin())) {
                return MessageResource.errorMessage("Only admin can invite new users!");
            }

            newUserRequest.email = URLDecoder.decode(newUserRequest.email, StandardCharsets.UTF_8.name());
            if (mailAttemptService.isBlocked(newUserRequest.email)) {
                return MessageResource.successMessage("Done");
            }

            RegisteredUser user = registrationService.createNewUser(newUserRequest);
            if (user == null)
                return MessageResource.successMessage("Done");
            mailService.sendRegistrationEmail(user);

            mailAttemptService.mailAttempt(newUserRequest.email);
            return MessageResource.successMessage("Done");
        } catch (IOException | TemplateException | MessagingException e) {
            log.error(e.toString());
            return MessageResource.errorMessage("Failed");
        }
    }

    @Operation(summary = "Verify validity of a registration token", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/token/verify", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource verifyToken(@RequestBody String token) {
        try {
            if (registrationService.verifyToken(token))
                return MessageResource.successMessage(registrationService.decodeToken(token)[0]);
            else
                return MessageResource.errorMessage("Invalid token!");
        } catch (InvalidUserTokenException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Invalid token!");
        }
    }

    @Operation(summary = "Verify validity of an authentication token")
    @GetMapping(value = "/verify", produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource verifyAuthToken(Authentication auth) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        return MessageResource.successMessage("Auth Token successfully verified, for user [" + loggedUser.getId() + "] " + loggedUser.getFullName());
    }

    @Operation(summary = "Login to the system", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/login", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource login(Authentication auth, Locale locale) {
        return new UserResource(userResponseFactory.getUser(userService.findByAuth(auth), locale), "profile");
    }

    @Operation(summary = "Reset password")
    @PostMapping(value = "/reset", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource resetPassword(@RequestBody String recoveryEmail) {
        if (mailAttemptService.isBlocked(recoveryEmail)) {
            return MessageResource.successMessage("Done");
        }
        try {
            RegisteredUser user = registrationService.resetPassword(recoveryEmail);
            if (user != null) {
                mailService.sendPasswordResetEmail(user);
                mailAttemptService.mailAttempt(user.getEmail());
                return MessageResource.successMessage("Done");
            } else {
                return MessageResource.successMessage("Done");
            }
        } catch (MessagingException | IOException | TemplateException e) {
            log.error(e.toString());
            return MessageResource.errorMessage("Failed");
        }
    }

    @Operation(summary = "Account recovery")
    @PostMapping(value = "/recover", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource recoverAccount(@RequestBody RegistrationRequest request) {
        try {
            if (!registrationService.verifyToken(request.token))
                return MessageResource.errorMessage("Invalid token!");
            RegisteredUser user = registrationService.recover(registrationService.decodeToken(request.token)[0], new String(Base64.getDecoder().decode(request.password)));
            if (user == null)
                return MessageResource.errorMessage("Recovery of account has failed!");
            return MessageResource.successMessage("Account is successfully recovered. You can login now.");
        } catch (InvalidUserTokenException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Invalid token!");
        }
    }

    @Operation(summary = "Set a new password", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/changePassword", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource changePassword(Authentication auth, @RequestBody ChangePasswordRequest request) {
        try {
            RegisteredUser user = (RegisteredUser) userService.findByEmail(request.login, false);
            if (user == null || request.password == null || request.newPassword == null) {
                return MessageResource.errorMessage("Incorrect login!");
            }

            String newPassword = new String(Base64.getDecoder().decode(request.newPassword));
            if (!registrationService.isPasswordSufficient(newPassword)) {
                return MessageResource.errorMessage("Insufficient password!");
            }

            String password = new String(Base64.getDecoder().decode(request.password));
            if (registrationService.stringMatchesUserPassword(user, password)) {
                registrationService.changePassword(user, newPassword);
                securityContextService.saveToken(((LoggedUser) auth.getPrincipal()).getId());
                securityContextService.reloadSecurityContext((LoggedUser) auth.getPrincipal());

            } else {
                return MessageResource.errorMessage("Incorrect password!");
            }

            return MessageResource.successMessage("Password is successfully changed.");
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("There has been a problem!");
        }
    }
}