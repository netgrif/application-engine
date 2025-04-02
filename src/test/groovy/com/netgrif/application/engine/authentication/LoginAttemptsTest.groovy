package com.netgrif.application.engine.authentication

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authorization.domain.ApplicationRole
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.configuration.properties.SecurityLimitsProperties
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.startup.ImportHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.web.authentication.WebAuthenticationDetails
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
class LoginAttemptsTest {

    public static final String USER_EMAIL = "test@mail.sk"
    public static final String USER_PASSWORD = "password"
    public static final String USER_BAD_PASSWORD = "totok"
    public static final String LOGIN_URL = "/api/user/me"

    private MockMvc mvc

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IRoleService roleService

    @Autowired
    private SecurityLimitsProperties securityLimitsProperties;

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        List<ApplicationRole> appRoles = new ArrayList<>()
        appRoles.add(roleService.findApplicationRoleByImportId("admin"))
        appRoles.add(roleService.findApplicationRoleByImportId("identity"))
        importHelper.createIdentity(IdentityParams.with()
                .firstname(new TextField("Test"))
                .lastname(new TextField("Integration"))
                .username(new TextField(USER_EMAIL))
                .password(new TextField(USER_PASSWORD))
                .build(), appRoles)
    }


    @Test
    void loginOK() {
        def result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_PASSWORD, "fakeIpOK"))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
        assert result
    }

    @Test
    void loginBadNewIP() {

        def result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_BAD_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_BAD_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_BAD_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_BAD_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        //BLOCK user
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_PASSWORD, "fakeIp2"))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
        assert result
    }

    @Test
    void loginBad() {

        def result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_BAD_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_BAD_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_BAD_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_BAD_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        //BLOCK user
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
        assert result
        sleep(securityLimitsProperties.getLoginTimeoutUnit().toMillis(securityLimitsProperties.getLoginTimeout())+2000) //Wait
        //UNBLOCK user
        result = mvc.perform(MockMvcRequestBuilders.get(LOGIN_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(getAuth(USER_EMAIL, USER_PASSWORD, "fakeIp"))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
        assert result
    }

    private Authentication getAuth(String user, String password, String ip) {
        def authentication = new UsernamePasswordAuthenticationToken(user, password)
        def details = new MockHttpServletRequest()
        details.setRemoteAddr(ip)
        def authDetails = new WebAuthenticationDetails(details)
        authentication.setDetails(authDetails)
        return authentication
    }

}
