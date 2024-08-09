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
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.petrinet.domain.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
import com.netgrif.application.engine.workflow.domain.params.DeleteCaseParams
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
import com.netgrif.application.engine.workflow.domain.params.TaskParams
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
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

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/task_authorization_service_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert net.getNet() != null
        this.net = net.getNet()

        ImportPetriNetEventOutcome netWithUserRefs = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/task_authorization_service_test_with_userRefs.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert netWithUserRefs.getNet() != null
        this.netWithUserRefs = netWithUserRefs.getNet()

        def auths = importHelper.createAuthorities(["user": Authority.user])
        testUser = importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[]
        )
    }


    @Test
    void testTaskAuthorizationService() {
        def tests = [
                { -> testAssignAuthorization() },
                { -> testDelegateAuthorization() },
                { -> testFinishAuthorization() },
                { -> testCancelAuthorization() },
                { -> testSetDataAuthorization() },
        ]
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

        // TODO: release/8.0.0 change to dataset?
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
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(net)
                .title("Test assign")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        assert taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), case_.getTaskStringId("1"))
        userService.removeRole(testUser, positiveRole.getStringId())
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

    @Test
    void testCanNotAssign() {
        ProcessRole negativeRole = this.net.getRoles().values().find(v -> v.getImportId() == "assign_neg_role")
        userService.addRole(testUser, negativeRole.getStringId())
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(net)
                .title("Test assign")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        assert !taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), case_.getTaskStringId("1"))
        userService.removeRole(testUser, negativeRole.getStringId())
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

    @Test
    void testCanAssignWithUsersRef() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(netWithUserRefs)
                .title("Test assign")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        String taskId = case_.getTaskStringId("1")
        case_ = dataService.setData(new SetDataParams(taskId, new DataSet([
                "assign_pos_ul":new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testUser.stringId)]))
        ] as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
        workflowService.save(case_)
        sleep(4000)

        assert taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), taskId)
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

    @Test
    void testCannotAssignWithUsersRef() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(netWithUserRefs)
                .title("Test assign")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        String taskId = case_.getTaskStringId("1")
        case_ = dataService.setData(new SetDataParams(taskId, new DataSet([
                "assign_neg_ul": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testUser.stringId)]))
        ] as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
        sleep(4000)

        assert !taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), taskId)
        workflowService.deleteCase(new DeleteCaseParams(case_))
    }

    @Test
    void testCanAssignWithNegRoleAndPosUsersRef() {
        ProcessRole positiveRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "assign_pos_role")
        userService.addRole(testUser, positiveRole.getStringId())
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(netWithUserRefs)
                .title("Test assign")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        String taskId = case_.getTaskStringId("1")
        case_ = dataService.setData(new SetDataParams(taskId, new DataSet([
                "assign_pos_ul": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testUser.stringId)]))
        ] as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
        sleep(4000)

        assert taskAuthorizationService.canCallAssign(testUser.transformToLoggedUser(), taskId)
        userService.removeRole(testUser, positiveRole.getStringId())
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

    @Test
    void testCanFinish() {
        ProcessRole positiveRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "finish_pos_role")
        userService.addRole(testUser, positiveRole.getStringId())
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(netWithUserRefs)
                .title("Test Finish")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()

        String taskId = case_.getTaskStringId("1")
        TaskParams taskParams = TaskParams.with()
                .taskId(taskId)
                .user(testUser)
                .build()
        taskService.assignTask(taskParams)
        assert taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        userService.removeRole(testUser, positiveRole.getStringId())
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

    @Test
    void testCanNotFinish() {
        ProcessRole negativeRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "finish_neg_role")
        userService.addRole(testUser, negativeRole.getStringId())
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(netWithUserRefs)
                .title("Test Finish")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()

        String taskId = case_.getTaskStringId("1")
        TaskParams taskParams = TaskParams.with()
                .taskId(taskId)
                .user(testUser)
                .build()
        taskService.assignTask(taskParams)
        assert !taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        userService.removeRole(testUser, negativeRole.getStringId())
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

    @Test
    void testCanFinishWithUsersRef() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(netWithUserRefs)
                .title("Test Finish")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        String taskId = case_.getTaskStringId("1")
        case_ = dataService.setData(new SetDataParams(taskId, new DataSet([
                "finish_pos_ul": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testUser.stringId)]))
        ] as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
        sleep(4000)

        TaskParams taskParams = TaskParams.with()
                .taskId(taskId)
                .user(testUser)
                .build()
        taskService.assignTask(taskParams)
        assert taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

    @Test
    void testCannotFinishWithUsersRef() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(netWithUserRefs)
                .title("Test Finish")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        String taskId = case_.getTaskStringId("1")
        case_ = dataService.setData(new SetDataParams(taskId, new DataSet([
                "finish_neg_ul": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testUser.stringId)]))
        ] as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
        sleep(4000)

        TaskParams taskParams = TaskParams.with()
                .taskId(taskId)
                .user(testUser)
                .build()
        taskService.assignTask(taskParams)
        assert !taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

    @Test
    void testCanFinishWithNegRoleAndPosUsersRef() {
        ProcessRole positiveRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "finish_pos_role")
        userService.addRole(testUser, positiveRole.getStringId())
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(netWithUserRefs)
                .title("Test Finish")
                .color("")
                .loggedUser(testUser.transformToLoggedUser())
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        String taskId = case_.getTaskStringId("1")
        case_ = dataService.setData(new SetDataParams(taskId, new DataSet([
                "finish_pos_ul": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testUser.stringId)]))
        ] as Map<String, Field<?>>), superCreator.getSuperUser())).getCase()
        sleep(4000)

        TaskParams taskParams = TaskParams.with()
                .taskId(taskId)
                .user(testUser)
                .build()
        taskService.assignTask(taskParams)
        assert taskAuthorizationService.canCallFinish(testUser.transformToLoggedUser(), taskId)
        userService.removeRole(testUser, positiveRole.getStringId())
        DeleteCaseParams deleteCaseParams = DeleteCaseParams.with()
                .useCase(case_)
                .build()
        workflowService.deleteCase(deleteCaseParams)
    }

}
