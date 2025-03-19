package com.netgrif.application.engine.permissions

import com.netgrif.application.engine.TestHelper
import com.netgrif.core.auth.domain.Authority
import com.netgrif.core.auth.domain.IUser
import com.netgrif.core.auth.domain.User
import com.netgrif.core.auth.domain.enums.UserState
import com.netgrif.auth.service.AuthorityService
import com.netgrif.auth.service.UserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.core.petrinet.domain.VersionType
import com.netgrif.core.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.core.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.core.workflow.domain.Case
import com.netgrif.core.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
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
class ElasticSearchViewPermissionTest {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Autowired
    private UserService userService

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private AuthorityService authorityService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private IElasticTaskService elasticTaskService

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

        testUser = importHelper.createUser(new com.netgrif.adapter.auth.domain.User(firstName: "Role", lastName: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [userAuthority] as Authority[], [] as ProcessRole[])
    }

    @Test
    void testSearchElasticViewWithUserWithoutRole() {
        Case case_ = workflowService.createCase(net.getStringId(), "Permission test", "", userService.transformToLoggedUser(testUser)).getCase()

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet("vpt")] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, userService.transformToLoggedUser(testUser), PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent().size() == 0
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchElasticViewWithUserWithPosRole() {
        Case case_ = workflowService.createCase(net.getStringId(), "Permission test", "", userService.transformToLoggedUser(testUser)).getCase()
        ProcessRole posViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_pos_role")
        userService.addRole(testUser, posViewRole.getStringId())

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet("vpt")] as List
        sleep(4000)
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, userService.transformToLoggedUser(testUser), PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent() != null
        assert casePage.getContent().size() == 1
        assert casePage.getContent()[0].stringId == case_.stringId
        userService.removeRole(testUser, posViewRole.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchElasticViewWithUserWithNegRole() {
        Case case_ = workflowService.createCase(net.getStringId(), "Permission test", "", userService.transformToLoggedUser(testUser)).getCase()
        ProcessRole negViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_neg_role")
        userService.addRole(testUser, negViewRole.getStringId())

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(net.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, userService.transformToLoggedUser(testUser), PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent().size() == 0 && case_.negativeViewRoles.contains(negViewRole.stringId)
        userService.removeRole(testUser, negViewRole.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchElasticViewWithUserWithoutUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", userService.transformToLoggedUser(testUser)).getCase()

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(netWithUserRefs.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, userService.transformToLoggedUser(testUser), PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent().size() == 0
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchElasticViewWithUserWithPosUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", userService.transformToLoggedUser(testUser)).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        case_ = workflowService.save(case_)
        sleep(4000)

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(netWithUserRefs.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, userService.transformToLoggedUser(testUser), PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent().size() == 1 && casePage.getContent()[0].stringId == case_.stringId && case_.viewUsers.contains(testUser.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchElasticViewWithUserWithNegUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", userService.transformToLoggedUser(testUser)).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_neg": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        case_ = workflowService.save(case_)
        sleep(4000)

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(netWithUserRefs.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, userService.transformToLoggedUser(testUser), PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent().size() == 0 && case_.negativeViewUsers.contains(testUser.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchElasticViewWithUserWithNegativeRoleAndPosUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", userService.transformToLoggedUser(testUser)).getCase()
        ProcessRole negViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_neg_role")
        userService.addRole(testUser, negViewRole.getStringId())
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task
        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "userList"
                ]
        ] as Map)).getCase()
        case_ = workflowService.save(case_)
        sleep(4000)

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(netWithUserRefs.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, userService.transformToLoggedUser(testUser), PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent().size() == 1 && case_.viewUsers.contains(testUser.stringId)
        userService.removeRole(testUser, negViewRole.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }
}
