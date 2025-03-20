package com.netgrif.application.engine.authentication.web;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.service.IdentityService;
import com.netgrif.application.engine.authentication.service.InvalidIdentityTokenException;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import com.netgrif.application.engine.authentication.web.requestbodies.ChangePasswordRequest;
import com.netgrif.application.engine.authentication.web.requestbodies.NewIdentityRequest;
import com.netgrif.application.engine.authentication.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.authentication.web.responsebodies.User;
import com.netgrif.application.engine.authentication.web.responsebodies.UserResource;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import freemarker.template.TemplateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@ConditionalOnProperty(
        value = "nae.auth.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Authentication")
public class AuthenticationController {

    private final IRegistrationService registrationService;

    private final IMailService mailService;

    private final IUserService userService;

    private final IMailAttemptService mailAttemptService;

    private final ServerAuthProperties serverAuthProperties;

    private final ISecurityContextService securityContextService;
    private final IdentityService identityService;

    @Operation(summary = "New identity registration")
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource signup(@RequestBody RegistrationRequest regRequest) {
        try {
            if (!registrationService.verifyToken(regRequest.token)) {
                String email = registrationService.decodeToken(regRequest.token)[0];
                return MessageResource.errorMessage(String.format("Registration of %s has failed! Invalid token!", email));
            }

            regRequest.password = new String(Base64.getDecoder().decode(regRequest.password));
            Identity identity = registrationService.registerIdentity(regRequest);
            if (identity == null) {
                String email = registrationService.decodeToken(regRequest.token)[0];
                return MessageResource.errorMessage(String.format("Registration of %s has failed! No identity with this email was found.", email));
            }

            return MessageResource.successMessage("Registration complete");
        } catch (InvalidIdentityTokenException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Invalid token!");
        }
    }

    @Operation(summary = "Send invitation to a new user", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/invite", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource invite(@RequestBody NewIdentityRequest newIdentityRequest, Authentication auth) {
        try {
            if (!serverAuthProperties.isOpenRegistration()) {
                return MessageResource.errorMessage("Registration is turned off.");
            }
            if (auth == null || !((Identity) auth.getPrincipal()).isAdmin()) {
                return MessageResource.errorMessage("Only admin can invite new users!");
            }

            newIdentityRequest.email = URLDecoder.decode(newIdentityRequest.email, StandardCharsets.UTF_8);
            if (mailAttemptService.isBlocked(newIdentityRequest.email)) {
                return MessageResource.successMessage("Done");
            }

            Identity identity = registrationService.createNewIdentity(newIdentityRequest);
            if (identity == null) {
                return MessageResource.successMessage("Done");
            }
            mailService.sendRegistrationEmail(identity);
            mailAttemptService.mailAttempt(newIdentityRequest.email);

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
            if (registrationService.verifyToken(token)) {
                return MessageResource.successMessage(registrationService.decodeToken(token)[0]);
            } else {
                return MessageResource.errorMessage("Invalid token!");
            }
        } catch (InvalidIdentityTokenException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Invalid token!");
        }
    }

    @Operation(summary = "Verify validity of an authentication token")
    @GetMapping(value = "/verify", produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource verifyAuthToken(Authentication auth) {
        Identity identity = (Identity) auth.getPrincipal();
        return MessageResource.successMessage(String.format("Auth Token successfully verified, for identity [%s] %s",
                identity.getStringId(), identity.getFullName()));
    }

    @Operation(summary = "Login to the system", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/login", produces = MediaTypes.HAL_JSON_VALUE)
    public UserResource login(Authentication auth, Locale locale) {
        return new UserResource(new User(userService.findByAuth(auth)), "profile");
    }

    @Operation(summary = "Reset password")
    @PostMapping(value = "/reset", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource resetPassword(@RequestBody String recoveryEmail) {
        if (mailAttemptService.isBlocked(recoveryEmail)) {
            return MessageResource.successMessage("Done");
        }
        try {
            Identity identity = registrationService.resetPassword(recoveryEmail);
            if (identity != null) {
                mailService.sendPasswordResetEmail(identity);
                mailAttemptService.mailAttempt(identity.getUsername());
            }
            return MessageResource.successMessage("Done");
        } catch (MessagingException | IOException | TemplateException e) {
            log.error(e.toString());
            return MessageResource.errorMessage("Failed");
        }
    }

    @Operation(summary = "Account recovery")
    @PostMapping(value = "/recover", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource recoverAccount(@RequestBody RegistrationRequest request) {
        try {
            if (!registrationService.verifyToken(request.token)) {
                return MessageResource.errorMessage("Invalid token!");
            }
            String email = registrationService.decodeToken(request.token)[0];
            String password = new String(Base64.getDecoder().decode(request.password));
            Identity identity = registrationService.recover(email, password);
            if (identity == null) {
                return MessageResource.errorMessage("Recovery of account has failed!");
            }
            return MessageResource.successMessage("Account is successfully recovered. You can login now.");
        } catch (InvalidIdentityTokenException e) {
            log.error(e.getMessage());
            return MessageResource.errorMessage("Invalid token!");
        }
    }

    @Operation(summary = "Set a new password", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/changePassword", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource changePassword(Authentication auth, @RequestBody ChangePasswordRequest request) {
        try {
            if (request.password == null || request.newPassword == null) {
                return MessageResource.errorMessage("Insufficient password!");
            }
            Optional<Identity> identityOpt = identityService.findByUsername(request.login);
            if (identityOpt.isEmpty()) {
                return MessageResource.errorMessage("Incorrect login!");
            }

            String newPassword = new String(Base64.getDecoder().decode(request.newPassword));
            if (!registrationService.isPasswordSufficient(newPassword)) {
                return MessageResource.errorMessage("Insufficient password!");
            }

            String currentPassword = new String(Base64.getDecoder().decode(request.password));
            if (registrationService.matchesIdentityPassword(identityOpt.get(), currentPassword)) {
                registrationService.changePassword(identityOpt.get(), newPassword);
                securityContextService.saveToken(((LoggedIdentity) auth.getPrincipal()).getIdentityId());
                securityContextService.reloadSecurityContext((LoggedIdentity) auth.getPrincipal());
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