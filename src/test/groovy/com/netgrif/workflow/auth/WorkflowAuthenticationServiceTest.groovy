package com.netgrif.workflow.auth

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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
import org.springframework.test.web.servlet.MvcResult
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
class WorkflowAuthenticationServiceTest {

    private static final String CREATE_CASE_URL = "/api/workflow/case"
    private static final String DELETE_CASE_URL = "/api/workflow/case/"

    private static final String USER_EMAIL = "user@test.com"
    private static final String ADMIN_EMAIL = "admin@test.com"

    private MockMvc mvc

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private ImportHelper importHelper

    @Before
    void before() {
        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/task_authentication_service_test.xml"), "major", superCreator.getLoggedSuper())
        assert net.isPresent()

        this.net = net.get()

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

    private PetriNet net

    private Authentication userAuth
    private Authentication adminAuth

    @Test
    void testDeleteCase() {
        def body = JsonOutput.toJson([
                title: "test case",
                netId: this.net.stringId,
                color: "color"
        ])

        def result = mvc.perform(post(CREATE_CASE_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())
                .andReturn()
        def response = parseResult(result)
        def userCaseId1 = response.stringId

        result = mvc.perform(post(CREATE_CASE_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())
                .andReturn()
        response = parseResult(result)
        def userCaseId2 = response.stringId

        result = mvc.perform(post(CREATE_CASE_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
                .andReturn()
        response = parseResult(result)
        def otherUserCaseId = response.stringId



        mvc.perform(delete(DELETE_CASE_URL + otherUserCaseId)
                .with(authentication(this.userAuth)))
                .andExpect(status().isForbidden())

        mvc.perform(delete(DELETE_CASE_URL + userCaseId1)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())

        mvc.perform(delete(DELETE_CASE_URL + userCaseId2)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.contentAsString)
    }
}
