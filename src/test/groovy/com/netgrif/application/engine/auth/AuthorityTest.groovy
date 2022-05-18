package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.AuthorizingObject
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.startup.ImportHelper
import groovy.json.JsonOutput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.junit.jupiter.api.Assertions.assertThrows
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class AuthorityTest {

    private static final String GROUP_NAME = "Test group"
    public static final String USER_EMAIL = "test@mail.sk"
    public static final String USER_PASSWORD = "password"

    public static final String AUTHORITY_DELETE_API = "/api/authority/delete/{name}"
    public static final String AUTHORITY_CREATE_API = "/api/authority/create"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IAuthorityService authorityService

    private MockMvc mvc
    private Authentication authentication

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def auths = importHelper.createAuthorities(["user": [AuthorizingObject.AUTHORITY_CREATE.name(),
                                                             AuthorizingObject.AUTHORITY_DELETE.name()]])
        def authorityList = auths.get("user").toArray()

        importHelper.createUser(new User(name: "Test", surname: "Integration", email: USER_EMAIL, password: USER_PASSWORD, state: UserState.ACTIVE),
                authorityList as Authority[], [] as ProcessRole[])
    }

    @Test
    void testCreateAuthority() {
        authentication = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSWORD)

        def content = JsonOutput.toJson(["name": "TEST_AUTHORITY"])

        mvc.perform(MockMvcRequestBuilders.post(AUTHORITY_CREATE_API)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()

        def newAuthority = authorityService.findByName("TEST_AUTHORITY")

        assert newAuthority != null
    }

    @Test
    void testDeleteAuthority() {
        Authority a = authorityService.getOrCreate("TEST_AUTHORITY")

        assert a != null

        authentication = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSWORD)

        mvc.perform(MockMvcRequestBuilders.delete(AUTHORITY_DELETE_API.replace("{name}", "TEST_AUTHORITY"))
                .accept(MediaTypes.HAL_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authorityService.findByName("TEST_AUTHORITY")
        })

        assert exception.getMessage() == "Could not find authority with name [TEST_AUTHORITY]"
    }
}
