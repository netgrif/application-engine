package com.netgrif.application.engine.auth

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
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
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowAuthorizationService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.context.WebApplicationContext

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class WorkflowAuthorizationServiceTest {

    private static final String CREATE_CASE_URL = "/api/workflow/case"
    private static final String DELETE_CASE_URL = "/api/workflow/case/"

    private static final String USER_EMAIL = "user123987645@test.com"
    private static final String ADMIN_EMAIL = "admin65489796451@test.com"

    private MockMvc mvc

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IWorkflowAuthorizationService workflowAuthorizationService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IDataService dataService

    @Autowired
    TestHelper testHelper

    @Autowired
    private IUserService userService

    private PetriNet net
    private PetriNet netWithUserRefs

    private Authentication userAuth
    private Authentication adminAuth
    private IUser testUser

////    @BeforeEach
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
//
//        importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
//                [auths.get("user")] as Authority[],
////                [] as Group[],
//                [] as ProcessRole[])
//
//        userAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")
//
//        importHelper.createUser(new User(name: "Admin", surname: "User", email: ADMIN_EMAIL, password: "password", state: UserState.ACTIVE),
//                [auths.get("admin")] as Authority[],
////                [] as Group[],
//                [] as ProcessRole[])
//
//        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, "password")
//    }

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/workflow_authorization_service_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        this.net = net.getNet()

        ImportPetriNetEventOutcome netWithUserRefs = petriNetService.importPetriNet(new FileInputStream("src/test/resources/workflow_authorization_service_test_with_userRefs.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netWithUserRefs.getNet() != null
        this.netWithUserRefs = netWithUserRefs.getNet()

        def auths = importHelper.createAuthorities(["user": Authority.user])
        testUser = importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")]as Authority[],
//                [org] as Group[],
                [] as ProcessRole[])
    }

    @Test
    @Disabled
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
        String userCaseId1 = response.outcome.aCase.stringId

        result = mvc.perform(post(CREATE_CASE_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())
                .andReturn()
        response = parseResult(result)
        String userCaseId2 = response.outcome.aCase.stringId

        result = mvc.perform(post(CREATE_CASE_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
                .andReturn()
        response = parseResult(result)
        String otherUserCaseId = response.outcome.acase.stringId

        /* TODO: momentalne vracia 200 OK, ma User vediet zmazat case ktory vytvoril Admin?
        mvc.perform(delete(DELETE_CASE_URL + otherUserCaseId)
                .with(authentication(this.userAuth)))
                .andExpect(status().isForbidden())
        */
        mvc.perform(delete(DELETE_CASE_URL + userCaseId1)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())

        mvc.perform(delete(DELETE_CASE_URL + userCaseId2)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }

    @Test
    void testCanCallCreate() {
        ProcessRole positiveCreateRole = this.net.getRoles().values().find(v -> v.getImportId() == "create_pos_role")
        userService.addRole(testUser, positiveCreateRole.getStringId())
        assert workflowAuthorizationService.canCallCreate(testUser.transformToLoggedUser(), net.getStringId())
        userService.removeRole(testUser, positiveCreateRole.getStringId())
    }

    @Test
    void testCanCallDelete() {
        ProcessRole positiveDeleteRole = this.net.getRoles().values().find(v -> v.getImportId() == "delete_pos_role")
        userService.addRole(testUser, positiveDeleteRole.getStringId())
        Case case_ = workflowService.createCase(net.getStringId(), "Test delete", "", testUser.transformToLoggedUser()).getCase()
        assert workflowAuthorizationService.canCallDelete(testUser.transformToLoggedUser(), case_.getStringId())
        userService.removeRole(testUser, positiveDeleteRole.getStringId())
    }

    @Test
    void testCanCallCreateFalse() {
        ProcessRole positiveCreateRole = this.net.getRoles().values().find(v -> v.getImportId() == "create_neg_role")
        userService.addRole(testUser, positiveCreateRole.getStringId())
        assert !workflowAuthorizationService.canCallCreate(testUser.transformToLoggedUser(), net.getStringId())
        userService.removeRole(testUser, positiveCreateRole.getStringId())
    }

    @Test
    void testCanCallDeleteFalse() {
        ProcessRole deleteRole = this.net.getRoles().values().find(v -> v.getImportId() == "delete_neg_role")
        userService.addRole(testUser, deleteRole.getStringId())
        Case case_ = workflowService.createCase(net.getStringId(), "Test delete", "", testUser.transformToLoggedUser()).getCase()
        assert !workflowAuthorizationService.canCallDelete(testUser.transformToLoggedUser(), case_.getStringId())
        userService.removeRole(testUser, deleteRole.getStringId())
    }


    @Test
    void testCanCallDeleteRoleFalseUserRefTrue() {
        ProcessRole posDeleteRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "delete_pos_role")
        ProcessRole negDeleteRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "delete_neg_role")

        userService.addRole(testUser, posDeleteRole.getStringId())
        userService.addRole(testUser, negDeleteRole.getStringId())

        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test delete", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "pos_user_list": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        workflowService.save(case_)

        assert workflowAuthorizationService.canCallDelete(testUser.transformToLoggedUser(), case_.getStringId())

        userService.removeRole(testUser, posDeleteRole.getStringId())
        userService.removeRole(testUser, negDeleteRole.getStringId())
    }

    @Test
    void testCanCallDeleteRoleFalseUserRefTrueUserRefFalse() {
        ProcessRole posDeleteRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "delete_pos_role")
        ProcessRole negDeleteRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "delete_neg_role")

        userService.addRole(testUser, posDeleteRole.getStringId())
        userService.addRole(testUser, negDeleteRole.getStringId())

        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Test delete", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "pos_user_list": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ],
                "neg_user_list": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        workflowService.save(case_)

        assert !workflowAuthorizationService.canCallDelete(testUser.transformToLoggedUser(), case_.getStringId())

        userService.removeRole(testUser, posDeleteRole.getStringId())
        userService.removeRole(testUser, negDeleteRole.getStringId())
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.contentAsString)
    }
}
