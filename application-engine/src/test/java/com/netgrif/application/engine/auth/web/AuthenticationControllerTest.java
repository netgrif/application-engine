package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.auth.service.InvalidUserTokenException;
import com.netgrif.application.engine.auth.service.UserFactory;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.auth.web.requestbodies.ChangePasswordRequest;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private IRegistrationService registrationService;

    @Mock
    private IMailService mailService;

    @Mock
    private UserService userService;

    @Mock
    private IMailAttemptService mailAttemptService;

    @Mock
    private UserFactory userFactory;

    @Mock
    private ISecurityContextService securityContextService;

    @Mock
    private LoggedUser loggedUser;

    @Mock
    private AbstractUser user;

    private SecurityConfigurationProperties.AuthProperties authProperties;
    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        authProperties = new SecurityConfigurationProperties.AuthProperties();
        controller = new AuthenticationController();
        ReflectionTestUtils.setField(controller, "registrationService", registrationService);
        ReflectionTestUtils.setField(controller, "mailService", mailService);
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "mailAttemptService", mailAttemptService);
        ReflectionTestUtils.setField(controller, "serverAuthProperties", authProperties);
        ReflectionTestUtils.setField(controller, "userResponseFactory", userFactory);
        ReflectionTestUtils.setField(controller, "securityContextService", securityContextService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void verifyTokenReturnsDecodedEmailWhenTokenIsValid() throws Exception {
        when(registrationService.verifyToken("token")).thenReturn(true);
        when(registrationService.decodeToken("token")).thenReturn(new String[]{"user@example.com", "key"});

        MessageResource response = controller.verifyToken("token");

        assertEquals("user@example.com", response.getContent().getSuccess());
    }

    @Test
    void verifyTokenHidesInvalidTokenDetails() throws Exception {
        when(registrationService.verifyToken("bad")).thenReturn(true);
        when(registrationService.decodeToken("bad")).thenThrow(new InvalidUserTokenException("bad"));

        MessageResource response = controller.verifyToken("bad");

        assertEquals("Invalid token!", response.getContent().getError());
    }

    @Test
    void signupRegistersUserWhenTokenIsValid() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.token = "token";
        request.password = Base64.getEncoder().encodeToString("secret".getBytes(StandardCharsets.UTF_8));
        when(registrationService.decodeToken("token")).thenReturn(new String[]{"user@example.com", "key"});
        when(registrationService.verifyToken("token")).thenReturn(true);
        when(registrationService.registerUser(request)).thenReturn(user);

        MessageResource response = controller.signup(request);

        assertEquals("Registration complete", response.getContent().getSuccess());
        assertEquals("secret", request.password);
    }

    @Test
    void signupRejectsInvalidAndMissingUsers() throws Exception {
        RegistrationRequest invalidToken = new RegistrationRequest();
        invalidToken.token = "invalid";
        when(registrationService.decodeToken("invalid")).thenReturn(new String[]{"user@example.com", "key"});
        when(registrationService.verifyToken("invalid")).thenReturn(false);

        MessageResource invalidResponse = controller.signup(invalidToken);

        assertEquals("Registration of user@example.com has failed! Invalid token!", invalidResponse.getContent().getError());

        RegistrationRequest missingUser = new RegistrationRequest();
        missingUser.token = "missing";
        missingUser.password = Base64.getEncoder().encodeToString("secret".getBytes(StandardCharsets.UTF_8));
        when(registrationService.decodeToken("missing")).thenReturn(new String[]{"missing@example.com", "key"});
        when(registrationService.verifyToken("missing")).thenReturn(true);
        when(registrationService.registerUser(missingUser)).thenReturn(null);

        MessageResource missingResponse = controller.signup(missingUser);

        assertEquals("Registration of missing@example.com has failed! No user with this email was found.", missingResponse.getContent().getError());
    }

    @Test
    void inviteRequiresAdminWhenOpenRegistrationIsDisabled() {
        authProperties.setOpenRegistration(false);
        NewUserRequest request = new NewUserRequest();
        request.email = "user%40example.com";

        MessageResource response = controller.invite(request, null);

        assertEquals("Only admin can invite new users!", response.getContent().getError());
        verify(registrationService, never()).createNewUser(request);
    }

    @Test
    void inviteDoesNotRevealBlockedAddress() {
        authProperties.setOpenRegistration(true);
        NewUserRequest request = new NewUserRequest();
        request.email = "user%40example.com";
        when(mailAttemptService.isBlocked("user@example.com")).thenReturn(true);

        MessageResource response = controller.invite(request, null);

        assertEquals("Done", response.getContent().getSuccess());
        verify(registrationService, never()).createNewUser(request);
    }

    @Test
    void inviteCreatesUserAndSendsRegistrationEmail() throws Exception {
        authProperties.setOpenRegistration(true);
        NewUserRequest request = new NewUserRequest();
        request.email = "user%40example.com";
        when(registrationService.createNewUser(request)).thenReturn(user);

        MessageResource response = controller.invite(request, null);

        assertEquals("Done", response.getContent().getSuccess());
        verify(mailService).sendRegistrationEmail(user);
        verify(mailAttemptService).mailAttempt("user@example.com");
    }

    @Test
    void inviteReturnsFailedWhenMailSendingFails() throws Exception {
        authProperties.setOpenRegistration(true);
        NewUserRequest request = new NewUserRequest();
        request.email = "user%40example.com";
        when(registrationService.createNewUser(request)).thenReturn(user);
        org.mockito.Mockito.doThrow(new MessagingException("smtp down")).when(mailService).sendRegistrationEmail(user);

        MessageResource response = controller.invite(request, null);

        assertEquals("Failed", response.getContent().getError());
    }

    @Test
    void resetPasswordDoesNotRevealBlockedEmail() {
        when(mailAttemptService.isBlocked("user@example.com")).thenReturn(true);

        MessageResource response = controller.resetPassword("user@example.com");

        assertEquals("Done", response.getContent().getSuccess());
        verify(registrationService, never()).resetPassword("user@example.com");
    }

    @Test
    void resetPasswordSendsResetEmailForExistingUser() throws Exception {
        when(registrationService.resetPassword("user@example.com")).thenReturn(user);
        when(user.getEmail()).thenReturn("user@example.com");

        MessageResource response = controller.resetPassword("user@example.com");

        assertEquals("Done", response.getContent().getSuccess());
        verify(mailService).sendPasswordResetEmail(user);
        verify(mailAttemptService).mailAttempt("user@example.com");
    }

    @Test
    void resetPasswordReturnsFailedWhenMailFails() throws Exception {
        when(registrationService.resetPassword("user@example.com")).thenReturn(user);
        org.mockito.Mockito.doThrow(new IOException("template")).when(mailService).sendPasswordResetEmail(user);

        MessageResource response = controller.resetPassword("user@example.com");

        assertEquals("Failed", response.getContent().getError());
    }

    @Test
    void recoverAccountChangesPasswordForValidToken() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.token = "token";
        request.password = Base64.getEncoder().encodeToString("new-secret".getBytes(StandardCharsets.UTF_8));
        when(registrationService.verifyToken("token")).thenReturn(true);
        when(registrationService.decodeToken("token")).thenReturn(new String[]{"user@example.com", "key"});
        when(registrationService.recover("user@example.com", "new-secret")).thenReturn(user);

        MessageResource response = controller.recoverAccount(request);

        assertEquals("Account is successfully recovered. You can login now.", response.getContent().getSuccess());
    }

    @Test
    void recoverAccountRejectsInvalidTokenAndMissingUser() throws Exception {
        RegistrationRequest invalid = new RegistrationRequest();
        invalid.token = "invalid";
        when(registrationService.verifyToken("invalid")).thenReturn(false);

        assertEquals("Invalid token!", controller.recoverAccount(invalid).getContent().getError());

        RegistrationRequest missing = new RegistrationRequest();
        missing.token = "missing";
        missing.password = Base64.getEncoder().encodeToString("new-secret".getBytes(StandardCharsets.UTF_8));
        when(registrationService.verifyToken("missing")).thenReturn(true);
        when(registrationService.decodeToken("missing")).thenReturn(new String[]{"missing@example.com", "key"});
        when(registrationService.recover("missing@example.com", "new-secret")).thenReturn(null);

        assertEquals("Recovery of account has failed!", controller.recoverAccount(missing).getContent().getError());
    }

    @Test
    void changePasswordRejectsMissingAuthentication() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "user",
                Base64.getEncoder().encodeToString("old-password".getBytes()),
                Base64.getEncoder().encodeToString("new-password".getBytes())
        );

        MessageResource response = controller.changePassword(null, request);

        assertEquals("Unauthorized!", response.getContent().getError());
    }

    @Test
    void changePasswordRejectsInvalidRequestsAndForeignUsers() {
        when(loggedUser.getRealmId()).thenReturn("realm");
        when(loggedUser.isAdmin()).thenReturn(false);
        when(loggedUser.getStringId()).thenReturn("self");
        when(user.getStringId()).thenReturn("target");
        when(userService.findUserByUsername("target", "realm")).thenReturn(Optional.of(user));
        ChangePasswordRequest request = new ChangePasswordRequest(
                "target",
                Base64.getEncoder().encodeToString("old-password".getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString("new-password".getBytes(StandardCharsets.UTF_8))
        );

        MessageResource invalidRequest = controller.changePassword(new UsernamePasswordAuthenticationToken(loggedUser, null), null);
        MessageResource foreignUser = controller.changePassword(new UsernamePasswordAuthenticationToken(loggedUser, null), request);

        assertEquals("Invalid request!", invalidRequest.getContent().getError());
        assertEquals("You can change only your own password!", foreignUser.getContent().getError());
    }

    @Test
    void changePasswordUpdatesSelfAndReloadsSecurityContext() {
        when(loggedUser.getRealmId()).thenReturn("realm");
        when(loggedUser.getStringId()).thenReturn("self");
        when(loggedUser.isAdmin()).thenReturn(false);
        when(user.getStringId()).thenReturn("self");
        when(userService.findUserByUsername("self", "realm")).thenReturn(Optional.of(user));
        when(registrationService.isPasswordSufficient("new-password")).thenReturn(true);
        when(registrationService.stringMatchesUserPassword(user, "old-password")).thenReturn(true);
        ChangePasswordRequest request = new ChangePasswordRequest(
                "self",
                Base64.getEncoder().encodeToString("old-password".getBytes(StandardCharsets.UTF_8)),
                Base64.getEncoder().encodeToString("new-password".getBytes(StandardCharsets.UTF_8))
        );

        MessageResource response = controller.changePassword(new UsernamePasswordAuthenticationToken(loggedUser, null), request);

        assertEquals("Password is successfully changed.", response.getContent().getSuccess());
        verify(registrationService).changePassword(user, "new-password");
        verify(securityContextService).saveToken("self");
        verify(securityContextService).reloadSecurityContext(loggedUser);
    }

    @Test
    void loginUsesSecurityContextAuthentication() {
        com.netgrif.application.engine.auth.web.responsebodies.User responseUser =
                org.mockito.Mockito.mock(com.netgrif.application.engine.auth.web.responsebodies.User.class);
        when(loggedUser.getStringId()).thenReturn("self");
        when(userService.findById("self", null)).thenReturn(user);
        when(userFactory.getUser(user, Locale.ENGLISH)).thenReturn(responseUser);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loggedUser, null));

        ResponseEntity<?> response = controller.login(null, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseUser, response.getBody());
    }

    @Test
    void loginRejectsMissingSecurityContextAuthentication() {
        ResponseEntity<?> response = controller.login(null, Locale.ENGLISH);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
