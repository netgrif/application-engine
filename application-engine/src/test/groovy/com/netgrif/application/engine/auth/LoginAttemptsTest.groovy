package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.startup.ImportHelper
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginAttemptsTest {

    public static final String USER_EMAIL = "test@mail.sk"
    public static final String USER_PASSWORD = "password"
    public static final String USER_BAD_PASSWORD = "totok"
    public static final String LOGIN_URL = "/api/users/me"

    private MockMvc mvc

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private SecurityConfigurationProperties.SecurityLimitsProperties securityLimitsProperties;

    @Autowired
    private UserService userService;

    private Map<String, Authority> auths

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        importHelper.createUser(new User(firstName: "Test", lastName: "Integration", username: USER_EMAIL, email: USER_EMAIL, password: USER_PASSWORD, state: UserState.ACTIVE),
                [auths.get("user"), auths.get("admin")] as Authority[],
                [] as ProcessRole[])
    }


    @Test
    @Order(1)
    void loginOK() {
        def result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
        assert result
    }

    @Test
    @Order(2)
    void loginBadNewIP() {

        def result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_BAD_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_BAD_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_BAD_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_BAD_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        //BLOCK user
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with { it.remoteAddress("fakeIp")}
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
        assert result
    }

    @Test
    @Order(3)
    void loginBad() {

        def result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_BAD_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_BAD_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_BAD_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_BAD_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        //BLOCK user
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        sleep(securityLimitsProperties.getLoginTimeoutUnit().toMillis(securityLimitsProperties.getLoginTimeout())+2000) //Wait
        //UNBLOCK user
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(USER_EMAIL, USER_PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
        assert result
    }
}
