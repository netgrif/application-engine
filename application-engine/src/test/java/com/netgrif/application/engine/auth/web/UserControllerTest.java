package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.*;
import com.netgrif.application.engine.auth.web.requestbodies.PreferencesRequest;
import com.netgrif.application.engine.auth.web.requestbodies.UserCreateRequest;
import com.netgrif.application.engine.auth.web.requestbodies.UserSearchRequestBody;
import com.netgrif.application.engine.auth.web.responsebodies.PreferencesResource;
import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ProcessRoleService processRoleService;

    @Mock
    private PreferencesService preferencesService;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private RealmService realmService;

    @Mock
    private UserFactory userFactory;

    @Mock
    private Authentication authentication;

    @Mock
    private LoggedUser loggedUser;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, processRoleService, preferencesService, authorityService, realmService, userFactory);
    }

    @Test
    void preferencesCreatesDefaultPreferencesWhenUserHasNone() {
        String userId = new ObjectId().toString();
        when(authentication.getPrincipal()).thenReturn(loggedUser);
        when(loggedUser.getStringId()).thenReturn(userId);

        ResponseEntity<PreferencesResource> response = controller.preferences(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getPreferences().getUserId());
        assertEquals(200, response.getBody().getPreferences().getDrawerWidth());
    }

    @Test
    void savePreferencesStoresPreferencesForLoggedUser() {
        String userId = new ObjectId().toString();
        when(authentication.getPrincipal()).thenReturn(loggedUser);
        when(loggedUser.getStringId()).thenReturn(userId);
        PreferencesRequest request = new PreferencesRequest();
        request.setLocale(Locale.ENGLISH.toLanguageTag());
        request.setDrawerWidth(320);

        ResponseEntity<?> response = controller.savePreferences(request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(preferencesService).save(argThat(preferences ->
                userId.equals(preferences.getUserId())
                        && Locale.ENGLISH.toLanguageTag().equals(preferences.getLocale())
                        && preferences.getDrawerWidth() == 320
        ));
    }

    @Test
    void createUserReturnsCreatedUserWhenRealmExistsAndUsernameIsFree() {
        AbstractUser domainUser = domainUser("john");
        User responseUser = new User(domainUser);
        UserCreateRequest request = createRequest("john");
        when(realmService.getRealmById("realm")).thenReturn(Optional.of(realm("realm")));
        when(userService.findUserByUsername("john", "realm")).thenReturn(Optional.empty());
        when(userService.createUser("john", "john@example.com", "John", "User", "secret", "realm")).thenReturn(domainUser);
        when(userFactory.getUser(domainUser, Locale.ENGLISH)).thenReturn(responseUser);

        ResponseEntity<User> response = controller.createUser("realm", request, Locale.ENGLISH);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(responseUser, response.getBody());
    }

    @Test
    void createUserReportsBadRealmConflictAndUnexpectedFailure() {
        UserCreateRequest request = createRequest("john");
        when(realmService.getRealmById("missing")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.BAD_REQUEST, controller.createUser("missing", request, Locale.ENGLISH).getStatusCode());

        when(realmService.getRealmById("realm")).thenReturn(Optional.of(realm("realm")));
        when(userService.findUserByUsername("john", "realm")).thenReturn(Optional.of(domainUser("john")));
        assertEquals(HttpStatus.CONFLICT, controller.createUser("realm", request, Locale.ENGLISH).getStatusCode());

        when(userService.findUserByUsername("broken", "realm")).thenThrow(new IllegalStateException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createUser("realm", createRequest("broken"), Locale.ENGLISH).getStatusCode());
    }

    @Test
    void getAllUsersTransformsUsersWhenRealmExists() {
        AbstractUser domainUser = domainUser("john");
        User responseUser = new User(domainUser);
        Pageable pageable = PageRequest.of(0, 10);
        when(realmService.getRealmById("realm")).thenReturn(Optional.of(realm("realm")));
        when(userService.findAllUsers("realm", pageable)).thenReturn(new PageImpl<>(List.of(domainUser), pageable, 1));
        when(userFactory.getUser(domainUser, Locale.ENGLISH)).thenReturn(responseUser);

        ResponseEntity<org.springframework.data.domain.Page<User>> response = controller.getAllUsers("realm", pageable, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertSame(responseUser, response.getBody().getContent().getFirst());
        when(realmService.getRealmById("missing")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getAllUsers("missing", pageable, Locale.ENGLISH).getStatusCode());
    }

    @Test
    void getLoggedUserHandlesSuccessMissingAndBadIds() {
        AbstractUser domainUser = domainUser("john");
        User responseUser = new User(domainUser);
        when(authentication.getPrincipal()).thenReturn(loggedUser);
        when(loggedUser.getStringId()).thenReturn("john-id");
        when(loggedUser.getRealmId()).thenReturn("realm");
        when(userService.findById("john-id", "realm")).thenReturn(domainUser);
        when(userFactory.getUser(domainUser, Locale.ENGLISH)).thenReturn(responseUser);

        ResponseEntity<User> ok = controller.getLoggedUser(authentication, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, ok.getStatusCode());
        assertSame(responseUser, ok.getBody());

        when(userService.findById("missing-id", "realm")).thenReturn(null);
        when(loggedUser.getStringId()).thenReturn("missing-id");
        assertEquals(HttpStatus.UNAUTHORIZED, controller.getLoggedUser(authentication, Locale.ENGLISH).getStatusCode());

        when(userService.findById("bad-id", "realm")).thenThrow(new IllegalArgumentException("bad"));
        when(loggedUser.getStringId()).thenReturn("bad-id");
        when(loggedUser.getId()).thenReturn(new ObjectId());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getLoggedUser(authentication, Locale.ENGLISH).getStatusCode());
    }

    @Test
    void searchMapsRoleStringsToProcessResourceIds() {
        AbstractUser domainUser = domainUser("john");
        User responseUser = new User(domainUser);
        Pageable pageable = PageRequest.of(0, 10);
        String roleId = new ProcessResourceId().toString();
        String negativeRoleId = new ProcessResourceId().toString();
        UserSearchRequestBody query = new UserSearchRequestBody();
        query.setFulltext("jo");
        query.setRoles(List.of(roleId));
        query.setNegativeRoles(List.of(negativeRoleId));
        when(authentication.getPrincipal()).thenReturn(loggedUser);
        when(userService.searchAllCoMembers(eq("jo"), processIds(roleId), processIds(negativeRoleId), eq(loggedUser), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(domainUser), pageable, 1));
        when(userFactory.getUser(domainUser, Locale.ENGLISH)).thenReturn(responseUser);

        ResponseEntity<org.springframework.data.domain.Page<User>> response = controller.search(query, pageable, authentication, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseUser, response.getBody().getContent().getFirst());
    }

    @Test
    void getUserAllowsAdminOrSelfAndRejectsForeignOrInvalidIds() {
        AbstractUser domainUser = domainUser("john");
        User responseUser = new User(domainUser);
        when(userService.getLoggedUserFromContext()).thenReturn(loggedUser);
        when(loggedUser.isAdmin()).thenReturn(false);
        when(loggedUser.getId()).thenReturn(new ObjectId("64b000000000000000000001"));
        when(loggedUser.getUsername()).thenReturn("john");

        assertEquals(HttpStatus.UNAUTHORIZED, controller.getUser("realm", "64b000000000000000000002", Locale.ENGLISH).getStatusCode());

        when(loggedUser.isAdmin()).thenReturn(true);
        when(userService.findById("64b000000000000000000001", "realm")).thenReturn(domainUser);
        when(userFactory.getUser(domainUser, Locale.ENGLISH)).thenReturn(responseUser);
        ResponseEntity<User> admin = controller.getUser("realm", "64b000000000000000000001", Locale.ENGLISH);
        assertEquals(HttpStatus.OK, admin.getStatusCode());
        assertSame(responseUser, admin.getBody());

        when(userService.findById("bad", "realm")).thenThrow(new IllegalArgumentException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, controller.getUser("realm", "bad", Locale.ENGLISH).getStatusCode());
    }

    @Test
    void assignRolesAndAuthoritiesReturnSuccessOrBadRequest() {
        AbstractUser domainUser = domainUser("john");
        String roleId = new ProcessResourceId().toString();
        when(authentication.getPrincipal()).thenReturn(loggedUser);
        when(userService.findById("john-id", "realm")).thenReturn(domainUser);

        assertEquals(HttpStatus.OK, controller.assignRolesToUser("realm", "john-id", Set.of(roleId), authentication).getStatusCode());
        verify(processRoleService).assignRolesToUser(eq(domainUser), processIds(roleId), eq(loggedUser));

        when(userService.findById("missing-id", "realm")).thenThrow(new IllegalArgumentException("missing"));
        assertEquals(HttpStatus.BAD_REQUEST, controller.assignRolesToUser("realm", "missing-id", Set.of(), authentication).getStatusCode());

        assertEquals(HttpStatus.OK, controller.assignAuthorityToUser("realm", "john-id", "ADMIN").getStatusCode());
        verify(userService).assignAuthority("john-id", "realm", "ADMIN");

        when(userService.assignAuthority("john-id", "realm", "BROKEN")).thenThrow(new IllegalArgumentException("broken"));
        assertEquals(HttpStatus.BAD_REQUEST, controller.assignAuthorityToUser("realm", "john-id", "BROKEN").getStatusCode());
    }

    @Test
    void getAllAuthoritiesReturnsUnpagedAuthorities() {
        Authority authority = new com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl("ADMIN");
        when(authorityService.findAll(Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(authority)));

        ResponseEntity<List<Authority>> response = controller.getAllAuthorities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(authority), response.getBody());
    }

    private UserCreateRequest createRequest(String username) {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setFirstName("John");
        request.setLastName("User");
        request.setPassword("secret");
        return request;
    }

    private AbstractUser domainUser(String username) {
        com.netgrif.application.engine.adapter.spring.auth.domain.User user =
                new com.netgrif.application.engine.adapter.spring.auth.domain.User();
        user.setUsername(username);
        user.setRealmId("realm");
        user.setEmail(username + "@example.com");
        user.setFirstName("John");
        user.setLastName("User");
        return user;
    }

    private Realm realm(String id) {
        return new com.netgrif.application.engine.adapter.spring.auth.domain.Realm(id);
    }

    private Collection<ProcessResourceId> processIds(String... expectedIds) {
        Set<String> expected = Set.of(expectedIds);
        return argThat(ids -> ids != null
                && ids.size() == expected.size()
                && ids.stream().map(ProcessResourceId::toString).allMatch(expected::contains));
    }
}
