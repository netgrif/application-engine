package com.netgrif.workflow.auth

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import groovy.json.JsonOutput
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class TaskAuthenticationServiceTest {

    private static final String ASSIGN_TASK_URL = "/api/task/assign/"
    private static final String DELEGATE_TASK_URL = "/api/task/delegate/"
    private static final String FINISH_TASK_URL = "/api/task/finish/"
    private static final String CANCEL_TASK_URL = "/api/task/cancel/"
    private static final String SET_DATA_URL_TEMPLATE = "/api/task/%s/data"
    private static final String SET_FILE_URL_TEMPLATE = "/api/task/%s/file/%s"

    private static final String USER_WITH_ROLE_EMAIL = "role@test.com"
    private static final String USER_WITHOUT_ROLE_EMAIL = "norole@test.com"
    private static final String ADMIN_USER_EMAIL = "admin@test.com"

    private MockMvc mvc

    @Autowired
    private Importer importer

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Before
    void beforeAll() {
        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/task_authentication_service_test.xml"), "major", superCreator.getLoggedSuper())
        assert net.isPresent()

        this.net = net.get()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        def processRoles = importHelper.createUserProcessRoles(["role": "role"], this.net)

        def user = importHelper.createUser(new User(name: "Role", surname: "User", email: USER_WITH_ROLE_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as Group[],
                [processRoles.get("role")] as UserProcessRole[])

        userId = user.id
        userWithRoleAuth = new UsernamePasswordAuthenticationToken(USER_WITH_ROLE_EMAIL, "password")

        importHelper.createUser(new User(name: "NoRole", surname: "User", email: USER_WITHOUT_ROLE_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as Group[],
                [] as UserProcessRole[])

        userWithoutRoleAuth = new UsernamePasswordAuthenticationToken(USER_WITHOUT_ROLE_EMAIL, "password")

        importHelper.createUser(new User(name: "Admin", surname: "User", email: ADMIN_USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("admin")] as Authority[],
                [] as Group[],
                [] as UserProcessRole[])

        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_USER_EMAIL, "password")
    }

    private PetriNet net

    private Long userId

    void beforeEach() {
        def aCase = importHelper.createCase("Case", this.net)
        assert aCase != null

        taskId = importHelper.getTaskId("Transition", aCase.stringId)
        assert taskId != null

        aCase = importHelper.createCase("Case 2", this.net)
        assert aCase != null

        taskId2 = importHelper.getTaskId("Transition", aCase.stringId)
        assert taskId2 != null
    }

    private String taskId
    private String taskId2

    private Authentication userWithRoleAuth
    private Authentication userWithoutRoleAuth
    private Authentication adminAuth

    @Test
    void testTaskAuthenticationService() {
        def tests = [
                { -> testAssignAuthorisation() },
                { -> testDelegateAuthorisation() },
                { -> testFinishAuthorisation() },
                { -> testCancelAuthorisation() },
                { -> testSetDataAuthorisation() },
//                { -> testSetFileAuthorisation() },
        ]
        tests.each { t ->
            beforeEach()
            t()
        }
    }

    void testAssignAuthorisation() {
        mvc.perform(get(ASSIGN_TASK_URL + taskId)
                .with(authentication(this.userWithoutRoleAuth)))
                .andExpect(status().isForbidden())
        mvc.perform(get(ASSIGN_TASK_URL + taskId)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(get(ASSIGN_TASK_URL + taskId2)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

    void testDelegateAuthorisation() {
        mvc.perform(post(DELEGATE_TASK_URL + taskId)
                .content(userId.toString())
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userWithoutRoleAuth)))
                .andExpect(status().isForbidden())
        mvc.perform(post(DELEGATE_TASK_URL + taskId)
                .content(userId.toString())
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(post(DELEGATE_TASK_URL + taskId2)
                .content(userId.toString())
                .contentType(APPLICATION_JSON)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

    void testFinishAuthorisation() {
        mvc.perform(get(ASSIGN_TASK_URL + taskId)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(get(ASSIGN_TASK_URL + taskId2)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())

        mvc.perform(get(FINISH_TASK_URL + taskId)
                .with(authentication(this.userWithoutRoleAuth)))
                .andExpect(status().isForbidden())
        mvc.perform(get(FINISH_TASK_URL + taskId)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(get(FINISH_TASK_URL + taskId2)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

    void testCancelAuthorisation() {
        mvc.perform(get(ASSIGN_TASK_URL + taskId)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(get(ASSIGN_TASK_URL + taskId2)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())

        mvc.perform(get(CANCEL_TASK_URL + taskId)
                .with(authentication(this.userWithoutRoleAuth)))
                .andExpect(status().isForbidden())
        mvc.perform(get(CANCEL_TASK_URL + taskId)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(get(CANCEL_TASK_URL + taskId2)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

    void testSetDataAuthorisation() {
        mvc.perform(get(ASSIGN_TASK_URL + taskId)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(get(ASSIGN_TASK_URL + taskId2)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())

        def body = JsonOutput.toJson([
                text: [
                        value: "Helo world",
                        type : "text"
                ]
        ])

        mvc.perform(post(String.format(SET_DATA_URL_TEMPLATE, taskId))
                .content(body)
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userWithoutRoleAuth)))
                .andExpect(status().isForbidden())
        mvc.perform(post(String.format(SET_DATA_URL_TEMPLATE, taskId))
                .content(body)
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(post(String.format(SET_DATA_URL_TEMPLATE, taskId2))
                .content(body)
                .contentType(APPLICATION_JSON)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

// TODO 14.8.2020 test for file upload endpoint

//    void testSetFileAuthorisation() {
//        mvc.perform(get(ASSIGN_TASK_URL + taskId)
//                .with(authentication(this.userWithRoleAuth)))
//                .andExpect(status().isOk())
//        mvc.perform(get(ASSIGN_TASK_URL + taskId2)
//                .with(authentication(this.userWithRoleAuth)))
//                .andExpect(status().isOk())
//
//        MockMultipartFile file = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());
//
//        mvc.perform(multipart(String.format(SET_FILE_URL_TEMPLATE, taskId, "file"))
//                .file(file)
//                .characterEncoding("UTF-8")
//                .with(authentication(this.userWithoutRoleAuth)))
//                .andExpect(status().isForbidden())
//        mvc.perform(multipart(String.format(SET_FILE_URL_TEMPLATE, taskId, "file"))
//                .file(file)
//                .characterEncoding("UTF-8")
//                .with(authentication(this.userWithRoleAuth)))
//                .andExpect(status().isOk())
//        mvc.perform(multipart(String.format(SET_FILE_URL_TEMPLATE, taskId2, "file"))
//                .file(file)
//                .characterEncoding("UTF-8")
//                .contentType(APPLICATION_JSON)
//                .with(authentication(this.adminAuth)))
//                .andExpect(status().isOk())
//    }
}