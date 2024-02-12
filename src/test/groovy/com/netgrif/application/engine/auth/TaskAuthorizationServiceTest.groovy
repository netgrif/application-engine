package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.json.JsonOutput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.context.WebApplicationContext

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class TaskAuthorizationServiceTest {

    private static final String ASSIGN_TASK_URL = "/api/task/assign/"
    private static final String DELEGATE_TASK_URL = "/api/task/delegate/"
    private static final String FINISH_TASK_URL = "/api/task/finish/"
    private static final String CANCEL_TASK_URL = "/api/task/cancel/"
    private static final String SET_DATA_URL_TEMPLATE = "/api/task/%s/data"
    private static final String SET_FILE_URL_TEMPLATE = "/api/task/%s/file/%s"

    private static final String USER_WITH_ROLE_EMAIL = "role@test.com"
    private static final String USER_WITHOUT_ROLE_EMAIL = "norole@test.com"
    private static final String ADMIN_USER_EMAIL = "admin@test.com"
    private static final String USER_EMAIL = "user123987645@test.com"

    private MockMvc mvc

    private PetriNet net
    private PetriNet netWithUserRefs
    private IUser testUser

    private String userId

    private Authentication userWithRoleAuth
    private Authentication userWithoutRoleAuth
    private Authentication adminAuth


    @Autowired
    private Importer importer

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ITaskAuthorizationService taskAuthorizationService

    @Autowired
    private IUserService userService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    TestHelper testHelper

    @Autowired
    private ITaskService taskService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IDataService dataService

    private String taskId
    private String taskId2

//    @BeforeEach
//    void before() {
//        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/task_authentication_service_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
//        assert net.getNet() != null
//
//        this.net = net.getNet()
//
//        mvc = MockMvcBuilders
//                .webAppContextSetup(wac)
//                .apply(springSecurity())
//                .build()
//
//        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
//        def processRoles = userProcessRoleRepository.findAllByNetId(this.net.getStringId())
//
//        def user = importHelper.createUser(new User(name: "Role", surname: "User", email: USER_WITH_ROLE_EMAIL, password: "password", state: UserState.ACTIVE),
//                [auths.get("user")] as Authority[],
//                [processRoles.find({ it.name.equals("role") })] as ProcessRole[])
//
//        userId = user.getStringId()
//        this.userWithRoleAuth = new UsernamePasswordAuthenticationToken(USER_WITH_ROLE_EMAIL, "password")
//
//        importHelper.createUser(new User(name: "NoRole", surname: "User", email: USER_WITHOUT_ROLE_EMAIL, password: "password", state: UserState.ACTIVE),
//                [auths.get("user")] as Authority[],
//                [] as ProcessRole[])
//
//        this.userWithoutRoleAuth = new UsernamePasswordAuthenticationToken(USER_WITHOUT_ROLE_EMAIL, "password")
//
//        importHelper.createUser(new User(name: "Admin", surname: "User", email: ADMIN_USER_EMAIL, password: "password", state: UserState.ACTIVE),
//                [auths.get("admin")] as Authority[],
//                [] as ProcessRole[])
//
//        this.adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_USER_EMAIL, "password")
//    }
//
//    void beforeEach() {
//        def aCase = importHelper.createCase("Case", this.net)
//        assert aCase != null
//
//        taskId = importHelper.getTaskId("Transition", aCase.stringId)
//        assert taskId != null
//
//        aCase = importHelper.createCase("Case 2", this.net)
//        assert aCase != null
//
//        taskId2 = importHelper.getTaskId("Transition", aCase.stringId)
//        assert taskId2 != null
//    }

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/task_authorization_service_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        this.net = net.getNet()

        ImportPetriNetEventOutcome netWithUserRefs = petriNetService.importPetriNet(new FileInputStream("src/test/resources/task_authorization_service_test_with_userRefs.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netWithUserRefs.getNet() != null
        this.netWithUserRefs = netWithUserRefs.getNet()

        def auths = importHelper.createAuthorities(["user": Authority.user])
        testUser = importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
//                [org] as Group[],
                [] as ProcessRole[]
        )
    }


    @Test
    @Disabled("Assign Test")
    void testTaskAuthorizationService() {
        def tests = [
                { -> testAssignAuthorization() },
                { -> testDelegateAuthorization() },
                { -> testFinishAuthorization() },
                { -> testCancelAuthorization() },
                { -> testSetDataAuthorization() },
//                { -> testSetFileAuthorization() },
        ]
//        tests.each { t ->
//            beforeEach()
//            t()
//        }
    }

    void testAssignAuthorization() {
        mvc.perform(get(ASSIGN_TASK_URL + taskId)
                .with(authentication(userWithoutRoleAuth)))
                .andExpect(status().is4xxClientError())
        mvc.perform(get(ASSIGN_TASK_URL + taskId)
                .with(authentication(userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(get(ASSIGN_TASK_URL + taskId2)
                .with(authentication(adminAuth)))
                .andExpect(status().isOk())
    }

    void testDelegateAuthorization() {
        mvc.perform(post(DELEGATE_TASK_URL + taskId)
                .content(userId.toString())
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .with(authentication(this.userWithoutRoleAuth)))
                .andExpect(status().isForbidden())
        mvc.perform(post(DELEGATE_TASK_URL + taskId)
                .content(userId.toString())
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .with(authentication(this.userWithRoleAuth)))
                .andExpect(status().isOk())
        mvc.perform(post(DELEGATE_TASK_URL + taskId2)
                .content(userId.toString())
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

    void testFinishAuthorization() {
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

    void testCancelAuthorization() {
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

    void testSetDataAuthorization() {
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

//    void testSetFileAuthorization() {
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

    @Test
    void testCanAssign() {
        ProcessRole positiveRole = this.net.getRoles().values().find(v -> v.getImportId() == "assign_pos_role")
        userService.addRole(testUser, positiveRole.getStringId())
        Case case_ = workflowService.createCase(net.getStringId(), "Test assign", "", testUser.transformToLoggedUser()).getCase()
        assert taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), (new ArrayList<>(case_.getTasks())).get(0).task)
        userService.removeRole(testUser, positiveRole.getStringId())
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCanNotAssign() {
        ProcessRole negativeRole = this.net.getRoles().values().find(v -> v.getImportId() == "assign_neg_role")
        userService.addRole(testUser, negativeRole.getStringId())
        Case case_ = workflowService.createCase(net.getStringId(), "Test assign", "", testUser.transformToLoggedUser()).getCase()
        assert !taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), (new ArrayList<>(case_.getTasks())).get(0).task)
        userService.removeRole(testUser, negativeRole.getStringId())
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCanAssignWithUsersRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test assign", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "assign_pos_ul": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        workflowService.save(case_)
        sleep(4000)

        assert taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), taskId)
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCannotAssignWithUsersRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test assign", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "assign_neg_ul": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        workflowService.save(case_)
        sleep(4000)

        assert !taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), taskId)
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCanAssignWithNegRoleAndPosUsersRef() {
        ProcessRole positiveRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "assign_pos_role")
        userService.addRole(testUser, positiveRole.getStringId())
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test assign", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "assign_pos_ul": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        workflowService.save(case_)
        sleep(4000)

        assert taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), taskId)
        userService.removeRole(testUser, positiveRole.getStringId())
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCanFinish() {
        ProcessRole positiveRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "finish_pos_role")
        userService.addRole(testUser, positiveRole.getStringId())
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test Finish", "", testUser.transformToLoggedUser()).getCase()

        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        taskService.assignTask(testUser.transformToLoggedUser(), taskId)
        assert taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        userService.removeRole(testUser, positiveRole.getStringId())
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCanNotFinish() {
        ProcessRole negativeRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "finish_neg_role")
        userService.addRole(testUser, negativeRole.getStringId())
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test Finish", "", testUser.transformToLoggedUser()).getCase()

        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        taskService.assignTask(testUser.transformToLoggedUser(), taskId)
        assert !taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        userService.removeRole(testUser, negativeRole.getStringId())
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCanFinishWithUsersRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test Finish", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "finish_pos_ul": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        workflowService.save(case_)
        sleep(4000)

        taskService.assignTask(testUser.transformToLoggedUser(), taskId)
        assert taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCannotFinishWithUsersRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test Finish", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "finish_neg_ul": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        workflowService.save(case_)
        sleep(4000)

        taskService.assignTask(testUser.transformToLoggedUser(), taskId)
        assert !taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        workflowService.deleteCase(case_.stringId)
    }

    @Test
    void testCanFinishWithNegRoleAndPosUsersRef() {
        ProcessRole positiveRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "finish_pos_role")
        userService.addRole(testUser, positiveRole.getStringId())
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test Finish", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "finish_pos_ul": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        workflowService.save(case_)
        sleep(4000)

        taskService.assignTask(testUser.transformToLoggedUser(), taskId)
        assert taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        userService.removeRole(testUser, positiveRole.getStringId())
        workflowService.deleteCase(case_.stringId)
    }

}
