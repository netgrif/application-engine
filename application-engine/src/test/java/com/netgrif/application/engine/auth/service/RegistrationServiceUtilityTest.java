package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationServiceUtilityTest {

    @Mock
    private UserService userService;

    @Mock
    private GroupService groupService;

    @Mock
    private ProcessRoleService processRoleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void initMocks() {
        org.mockito.MockitoAnnotations.openMocks(this);
    }

    @Test
    void encodesAndDecodesRegistrationToken() throws Exception {
        RegistrationService service = service();

        String token = service.encodeToken("user@example.com", "token-key");

        assertArrayEquals(new String[]{"user@example.com", "token-key"}, service.decodeToken(token));
    }

    @Test
    void rejectsInvalidTokens() {
        RegistrationService service = service();
        String notEmailToken = service.encodeToken("not-email", "token");

        assertThrows(InvalidUserTokenException.class, () -> service.decodeToken(null));
        assertThrows(InvalidUserTokenException.class, () -> service.decodeToken(""));
        assertThrows(InvalidUserTokenException.class, () -> service.decodeToken("not-base64"));
        assertThrows(InvalidUserTokenException.class, () -> service.decodeToken(notEmailToken));
    }

    @Test
    void checksPasswordLengthAgainstConfiguredMinimum() {
        RegistrationService service = service();

        assertFalse(service.isPasswordSufficient("short"));
        assertTrue(service.isPasswordSufficient("long-enough"));
    }

    @Test
    void generatesExpirationDateUsingConfiguredValidityPeriod() {
        RegistrationService service = service();

        LocalDateTime before = LocalDateTime.now().plusDays(2).minusSeconds(1);
        LocalDateTime expiration = service.generateExpirationDate();
        LocalDateTime after = LocalDateTime.now().plusDays(2).plusSeconds(1);

        assertTrue(expiration.isAfter(before));
        assertTrue(expiration.isBefore(after));
    }

    @Test
    void verifiesTokenAgainstPersistedUser() {
        RegistrationService service = wiredService();
        User user = user("user@example.com");
        user.setToken("token-key");
        user.setExpirationDate(LocalDateTime.now().plusHours(1));
        String token = service.encodeToken("user@example.com", "token-key");
        when(userService.findByEmail("user@example.com", null)).thenReturn(user);

        assertTrue(service.verifyToken(token));

        user.setToken("other");
        assertFalse(service.verifyToken(token));
        assertFalse(service.verifyToken("not-a-token"));
    }

    @Test
    void encodesAndMatchesUserPassword() {
        RegistrationService service = wiredService();
        User user = user("user@example.com");
        user.setPassword("plain");
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(true);

        service.encodeUserPassword(user);

        assertEquals("encoded", user.getPassword());
        assertTrue(service.stringMatchesUserPassword(user, "plain"));
        User emptyPasswordUser = user("empty@example.com");
        assertThrows(IllegalArgumentException.class, () -> service.encodeUserPassword(emptyPasswordUser));
    }

    @Test
    void changePasswordEncodesAndSavesUser() {
        RegistrationService service = wiredService();
        User user = user("user@example.com");
        when(passwordEncoder.encode("new")).thenReturn("encoded-new");
        when(userService.saveUser(user, "realm")).thenReturn(user);

        service.changePassword(user, "new");

        assertEquals("encoded-new", user.getPassword());
        verify(userService).saveUser(user, "realm");
    }

    @Test
    void createNewUserReturnsNullWhenExistingUserIsActive() {
        RegistrationService service = wiredService();
        User active = user("active@example.com");
        active.setState(UserState.ACTIVE);
        NewUserRequest request = new NewUserRequest();
        request.email = "active@example.com";
        when(userService.findByEmail("active@example.com", null)).thenReturn(active);

        assertNull(service.createNewUser(request));
        verify(userService, never()).saveUser(any(AbstractUser.class), eq(null));
    }

    @Test
    void createNewUserInitializesInactiveUserWithRolesAndGroups() {
        RegistrationService service = wiredService();
        NewUserRequest request = new NewUserRequest();
        request.email = "new@example.com";
        request.processRoles = Set.of("role-a");
        request.groups = Set.of("group-a");
        ProcessRole defaultRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole(new ProcessResourceId().toString());
        User saved = user("new@example.com");
        when(processRoleService.getDefaultRole()).thenReturn(defaultRole);
        when(userService.saveUser(any(AbstractUser.class), eq(null))).thenReturn(saved);

        AbstractUser result = service.createNewUser(request);

        assertSame(saved, result);
        verify(userService).addDefaultAuthorities(any(AbstractUser.class));
        verify(userService).addRole(any(AbstractUser.class), eq("role-a"));
        verify(userService).addRole(any(AbstractUser.class), eq(defaultRole.getStringId()));
        verify(groupService).addUser(saved, "group-a");
    }

    @Test
    void createNewUserClearsStaleRolesWhenRenewingInactiveUser() {
        RegistrationService service = wiredService();
        NewUserRequest request = new NewUserRequest();
        request.email = "renew@example.com";
        request.processRoles = Set.of("role-a");
        User inactive = user("renew@example.com");
        inactive.setState(UserState.INACTIVE);
        ProcessRole staleRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole(new ProcessResourceId().toString());
        ProcessRole defaultRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole(new ProcessResourceId().toString());
        inactive.addProcessRole(staleRole);
        when(userService.findByEmail("renew@example.com", null)).thenReturn(inactive);
        when(processRoleService.getDefaultRole()).thenReturn(defaultRole);
        when(userService.saveUser(inactive, null)).thenReturn(inactive);

        AbstractUser result = service.createNewUser(request);

        assertSame(inactive, result);
        assertTrue(inactive.getProcessRoles().isEmpty());
        assertTrue(inactive.getProcessRoleIds().isEmpty());
        verify(userService).addRole(inactive, "role-a");
        verify(userService).addRole(inactive, defaultRole.getStringId());
    }

    @Test
    void registerUserActivatesInvitedUser() throws Exception {
        RegistrationService service = wiredService();
        User user = user("user@example.com");
        user.setToken("token-key");
        user.setExpirationDate(LocalDateTime.now().plusHours(1));
        RegistrationRequest request = new RegistrationRequest();
        request.token = service.encodeToken("user@example.com", "token-key");
        request.name = "John";
        request.surname = "Registered";
        request.password = "secret";
        when(userService.findByEmail("user@example.com", null)).thenReturn(user);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userService.saveUser(user, null)).thenReturn(user);

        AbstractUser result = service.registerUser(request);

        assertSame(user, result);
        assertEquals("John", user.getFirstName());
        assertEquals("Registered", user.getLastName());
        assertEquals("encoded-secret", user.getPassword());
        assertEquals(UserState.ACTIVE, user.getState());
        verify(passwordEncoder).encode("secret");
        verify(userService).saveUser(user, null);
    }

    @Test
    void registerUserRejectsMismatchedOrExpiredInvitationToken() {
        RegistrationService service = wiredService();
        User user = user("user@example.com");
        user.setToken("other-token");
        user.setExpirationDate(LocalDateTime.now().plusHours(1));
        RegistrationRequest request = new RegistrationRequest();
        request.token = service.encodeToken("user@example.com", "token-key");
        when(userService.findByEmail("user@example.com", null)).thenReturn(user);

        assertThrows(InvalidUserTokenException.class, () -> service.registerUser(request));

        user.setToken("token-key");
        user.setExpirationDate(LocalDateTime.now().minusSeconds(1));
        assertThrows(InvalidUserTokenException.class, () -> service.registerUser(request));
    }

    @Test
    void registerUserReturnsNullWhenEmailIsUnknown() throws Exception {
        RegistrationService service = wiredService();
        RegistrationRequest request = new RegistrationRequest();
        request.token = service.encodeToken("missing@example.com", "token-key");

        assertNull(service.registerUser(request));
    }

    @Test
    void resetPasswordBlocksOnlyActiveUsers() {
        RegistrationService service = wiredService();
        User active = mock(User.class);
        User inactive = user("inactive@example.com");
        inactive.setState(UserState.INACTIVE);
        when(userService.findByEmail("active@example.com", null)).thenReturn(active);
        when(userService.findByEmail("inactive@example.com", null)).thenReturn(inactive);
        when(active.isActive()).thenReturn(true);
        when(userService.saveUser(active, null)).thenReturn(active);

        assertSame(active, service.resetPassword("active@example.com"));
        verify(active).setState(UserState.BLOCKED);
        verify(active).setPassword(null);
        verify(active).setToken(org.mockito.ArgumentMatchers.anyString());
        verify(active).setExpirationDate(any(LocalDateTime.class));
        assertNull(service.resetPassword("inactive@example.com"));
        assertNull(service.resetPassword("missing@example.com"));
    }

    @Test
    void recoverActivatesAndEncodesBlockedUserPassword() {
        RegistrationService service = wiredService();
        User user = mock(User.class);
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-secret");
        when(user.getPassword()).thenReturn("new-secret");
        when(userService.findByEmail("user@example.com", null)).thenReturn(user);
        when(userService.saveUser(user, null)).thenReturn(user);

        assertSame(user, service.recover("user@example.com", "new-secret"));
        verify(user).setState(UserState.ACTIVE);
        verify(user).setPassword("new-secret");
        verify(user).setPassword("encoded-secret");
        verify(user).setToken(null);
        verify(user).setExpirationDate(null);
        assertNull(service.recover("missing@example.com", "new-secret"));
    }

    @Test
    void scheduledCleanupAndTokenResetUseUserService() {
        RegistrationService service = wiredService();
        User blocked = mock(User.class);
        when(userService.findAllByStateAndExpirationDateBefore(eq(UserState.BLOCKED), any(LocalDateTime.class), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(blocked)));

        service.removeExpiredUsers();
        service.resetExpiredToken();

        verify(userService).removeAllByStateAndExpirationDateBefore(eq(UserState.INACTIVE), any(LocalDateTime.class), eq(null));
        verify(blocked).setToken(null);
        verify(blocked).setExpirationDate(null);
        verify(userService).saveUsers(List.of(blocked));
    }

    private RegistrationService service() {
        SecurityConfigurationProperties.AuthProperties properties = new SecurityConfigurationProperties.AuthProperties();
        properties.setMinimalPasswordLength(8);
        properties.setTokenValidityPeriod(2);
        RegistrationService service = new RegistrationService();
        ReflectionTestUtils.setField(service, "serverAuthProperties", properties);
        return service;
    }

    private RegistrationService wiredService() {
        RegistrationService service = service();
        PaginationProperties paginationProperties = new PaginationProperties();
        paginationProperties.setBackendPageSize(2);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "groupService", groupService);
        ReflectionTestUtils.setField(service, "processRoleService", processRoleService);
        ReflectionTestUtils.setField(service, "paginationProperties", paginationProperties);
        return service;
    }

    private User user(String email) {
        com.netgrif.application.engine.adapter.spring.auth.domain.User user =
                new com.netgrif.application.engine.adapter.spring.auth.domain.User();
        user.setEmail(email);
        user.setUsername(email);
        user.setRealmId("realm");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }
}
