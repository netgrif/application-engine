package com.netgrif.workflow.permissions

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.IUser
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.workflow.workflow.service.CaseSearchService
import com.netgrif.workflow.workflow.service.TaskSearchService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
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
        case_.dataSet["view_ul_pos"].value = [testUser.stringId]
        case_ = workflowService.save(case_)
        sleep(4000)

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 1 && casePage.getContent()[0].stringId == case_.stringId && case_.viewUsers.contains(testUser.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }

    @Test
    void testSearchQueryDSLViewWithNegUserRef() {
        Case case_ = workflowService.createCase(netWithUserRefs.getStringId(), "Permission test", "", testUser.transformToLoggedUser()).getCase()
        case_.dataSet["view_ul_neg"].value = [testUser.stringId]
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
        ProcessRole negViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_neg_role")
        userService.addRole(testUser, negViewRole.getStringId())
        case_.dataSet["view_ul_pos"].value = [testUser.stringId]
        case_ = workflowService.save(case_)
        sleep(4000)

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), testUser.transformToLoggedUser(), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 1 && case_.viewUsers.contains(testUser.stringId)
        userService.removeRole(testUser, negViewRole.getStringId())
        workflowService.deleteCase(case_.getStringId())
    }
}
