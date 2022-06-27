package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Filter
import com.netgrif.application.engine.workflow.domain.repositories.FilterRepository
import groovy.json.JsonOutput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

//import com.netgrif.application.engine.orgstructure.domain.Group

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class FilterAuthorizationServiceTest {

    private static final String CREATE_FILTER_URL = "/api/filter"
    private static final String DELETE_FILTER_URL = "/api/filter/"

    private static final String USER_EMAIL = "user@test.com"
    private static final String ADMIN_EMAIL = "admin@test.com"

    private MockMvc mvc

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private FilterRepository filterRepository


    @Autowired
    private TestHelper testHelper

    private Authentication userAuth
    private Authentication adminAuth

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])

        importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])

        userAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")
        userAuth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

        importHelper.createUser(new User(name: "Admin", surname: "User", email: ADMIN_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("admin")] as Authority[],
                [] as ProcessRole[])

        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, "password")
        adminAuth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
    }


    @Test
    void testDeleteFilter() {
        def body = [
                title      : "",
                visibility : Filter.VISIBILITY_PUBLIC,
                description: "",
                type       : "",
                query      : ""
        ]

        body["title"] = "user filter 1"
        mvc.perform(post(CREATE_FILTER_URL)
                .content(JsonOutput.toJson(body))
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())

        body["title"] = "user filter 2"
        mvc.perform(post(CREATE_FILTER_URL)
                .content(JsonOutput.toJson(body))
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())

        body["title"] = "other filter"
        mvc.perform(post(CREATE_FILTER_URL)
                .content(JsonOutput.toJson(body))
                .contentType(APPLICATION_JSON)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())


        def filters = filterRepository.findAll()
        Filter otherFilter = filters.find { f -> f.title.defaultValue.equals("other filter") }
        assert otherFilter != null
        mvc.perform(delete(DELETE_FILTER_URL + otherFilter.stringId)
                .with(authentication(this.userAuth)))
                .andExpect(status().isForbidden())

        Filter filter1 = filters.find { f -> f.title.defaultValue.equals("user filter 1") }
        assert filter1 != null
        mvc.perform(delete(DELETE_FILTER_URL + filter1.stringId)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())

        Filter filter2 = filters.find { f -> f.title.defaultValue.equals("user filter 2") }
        assert filter2 != null
        mvc.perform(delete(DELETE_FILTER_URL + filter2.stringId)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

}
