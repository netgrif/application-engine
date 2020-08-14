package com.netgrif.workflow.auth

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Filter
import com.netgrif.workflow.workflow.domain.repositories.FilterRepository
import groovy.json.JsonOutput
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class FilterAuthenticationServiceTest {

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

    @Before
    void before() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])

        importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as Group[],
                [] as UserProcessRole[])

        userAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")

        importHelper.createUser(new User(name: "Admin", surname: "User", email: ADMIN_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("admin")] as Authority[],
                [] as Group[],
                [] as UserProcessRole[])

        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, "password")
    }

    private Authentication userAuth
    private Authentication adminAuth

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
