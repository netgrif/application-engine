package com.netgrif.application.engine.permissions

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class QueryDSLViewPermissionTest {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IUserService userService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IDataService dataService

    @Autowired
    private TestHelper testHelper

    private static final String USER_EMAIL = "user123987645@test.com"

    private PetriNet net
    private PetriNet netWithUserRefs
    private IUser testUser
    private Authority userAuthority

    @BeforeEach
    void inti() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/view_permission_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        this.net = net.getNet()

        ImportPetriNetEventOutcome netWithUserRefs = petriNetService.importPetriNet(new FileInputStream("src/test/resources/view_permission_with_userRefs_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netWithUserRefs.getNet() != null
        this.netWithUserRefs = netWithUserRefs.getNet()

        userAuthority = authorityService.getOrCreate(Authority.user)

        testUser = importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [userAuthority] as Authority[], [] as ProcessRole[])
    }

    @Test
    void testSearchQueryDSLViewWithoutRole() {
        Case case_ = workflowService.createCase(net.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 0
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchQueryDSLViewWithUserWithPosRole() {
        Case case_ = workflowService.createCase(net.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        ProcessRole posViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_pos_role")
        userService.addRole(testUser, posViewRole.getStringId())

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 1 && casePage.getContent()[0].stringId == case_.stringId
        userService.removeRole(testUser, posViewRole.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchQueryDSLViewWithUserWithNegRole() {
        Case case_ = workflowService.createCase(net.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        ProcessRole negViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_neg_role")
        userService.addRole(testUser, negViewRole.getStringId())

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 0 && case_.negativeViewRoles.contains(negViewRole.stringId)
        userService.removeRole(testUser, negViewRole.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchQueryDSLViewWithoutUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 0
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchQueryDSLViewWithPosUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        case_ = workflowService.save(case_)
        sleep(4000)

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 1 && casePage.getContent()[0].stringId == case_.stringId && case_.viewUsers.contains(testUser.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchTaskQueryDSLViewWithPosUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        case_ = workflowService.save(case_)
        sleep(4000)

        TaskSearchRequest request = new TaskSearchRequest()
        request.process = [new com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet(netWithUserRefs.getStringId())]
        Page<Task> taskPage = taskService.search([request],
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale(), false)

        assert taskPage.getContent().size() == 1 && taskPage.content[0].caseId == case_.stringId && taskPage.content[0].viewUsers.contains(testUser.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchTaskQueryDSLViewWithUserWithPosRole() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        ProcessRole posViewRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "view_pos_role")
        userService.addRole(testUser, posViewRole.getStringId())

        TaskSearchRequest request = new TaskSearchRequest()
        request.process = [new com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet(netWithUserRefs.getStringId())]
        Page<Task> taskPage = taskService.search([request],
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale(), false)

        assert taskPage.getContent().size() == 1 && taskPage.getContent()[0].caseId == case_.stringId
        userService.removeRole(testUser, posViewRole.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchQueryDSLViewWithNegUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_neg": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        case_ = workflowService.save(case_)
        sleep(4000)

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 0 && case_.negativeViewUsers.contains(testUser.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchQueryDSLViewWithNegRoleAndPosUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()

        ProcessRole negViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_neg_role")
        userService.addRole(testUser, negViewRole.getStringId())
        case_ = workflowService.save(case_)
        sleep(4000)

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 1 && case_.viewUsers.contains(testUser.stringId)
        userService.removeRole(testUser, negViewRole.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }
}
