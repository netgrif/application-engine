package com.netgrif.application.engine.configuration.authentication.providers.basic;

import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.service.interfaces.ILoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NetgrifBasicAuthenticationProviderTest {

    @Mock
    private UserService userService;

    @Mock
    private ILoginAttemptService loginAttemptService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    private NetgrifBasicAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new NetgrifBasicAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        ReflectionTestUtils.setField(provider, "userService", userService);
        ReflectionTestUtils.setField(provider, "loginAttemptService", loginAttemptService);
        ReflectionTestUtils.setField(provider, "publisher", publisher);
    }

    @Test
    void supportsOnlyUsernamePasswordAuthentication() {
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(provider.supports(Authentication.class));
    }

    @Test
    void rejectsAuthenticationWithoutRemoteAddress() {
        UsernamePasswordAuthenticationToken authentication = authentication("user", "password", null);

        assertThrows(BadCredentialsException.class, () -> provider.authenticate(authentication));
        verify(loginAttemptService, never()).isBlocked(null);
    }

    @Test
    void rejectsBlockedRemoteAddressBeforeUserLookup() {
        UsernamePasswordAuthenticationToken authentication = authentication("user", "password", "127.0.0.1");
        when(loginAttemptService.isBlocked("127.0.0.1")).thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> provider.authenticate(authentication));
        verify(userService, never()).findUserByUsername("user", null);
    }

    @Test
    void recordsFailedLoginWhenUserDoesNotExist() {
        UsernamePasswordAuthenticationToken authentication = authentication("missing", "password", "127.0.0.1");
        when(userService.findUserByUsername("missing", null)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> provider.authenticate(authentication));
        verify(loginAttemptService).loginFailed("127.0.0.1");
    }

    @Test
    void rejectsNullPasswordEncoder() {
        assertThrows(IllegalArgumentException.class, () -> provider.setPasswordEncoder(null));
    }

    private UsernamePasswordAuthenticationToken authentication(String username, String password, String remoteAddress) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
        WebAuthenticationDetails details = mock(WebAuthenticationDetails.class);
        when(details.getRemoteAddress()).thenReturn(remoteAddress);
        authentication.setDetails(details);
        return authentication;
    }
}
