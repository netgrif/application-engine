package com.netgrif.application.engine.permissions

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.domain.params.ImportProcessParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
import com.netgrif.application.engine.workflow.domain.params.DeleteCaseParams
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
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
    private SuperCreator superCreator

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private IElasticTaskService elasticTaskService

    @Autowired
    private IDataService dataService

    @Autowired
    private IRoleService roleService

    @Autowired
    private TestHelper testHelper

    private static final String USER_EMAIL = "user123987645@test.com"

    private Process net
    private Process netWithUserRefs
    private Identity testIdentity

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/view_permission_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId))
        assert net.getProcess() != null
        this.net = net.getProcess()

        ImportPetriNetEventOutcome netWithUserRefs = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/view_permission_with_userRefs_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId))
        assert netWithUserRefs.getProcess() != null
        this.netWithUserRefs = netWithUserRefs.getProcess()

        testIdentity = importHelper.createIdentity(IdentityParams.with()
                .firstname(new TextField("Role"))
                .lastname(new TextField("Identity"))
                .username(new TextField(USER_EMAIL))
                .password(new TextField("password"))
                .build(), new ArrayList<Role>())

        TestHelper.login(testIdentity)
    }

    @Test
    void testSearchElasticViewWithUserWithoutRole() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(net)
                .title("Permission test")
                .authorId(testIdentity.toSession().activeActorId)
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet("vpt")] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, testIdentity.toSession().activeActorId,
                PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent().size() == 0
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchElasticViewWithUserWithPosRole() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(net)
                .title("Permission test")
                .authorId(testIdentity.toSession().activeActorId)
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        ProcessRole posViewRole = roleService.findProcessRoleByImportId("view_pos_role")
        roleService.assignRolesToActor(testIdentity.toSession().activeActorId, Set.of(posViewRole.stringId))

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet("vpt")] as List
        sleep(4000)
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, testIdentity.toSession().activeActorId,
                PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent() != null
        assert casePage.getContent().size() == 1
        assert casePage.getContent()[0].stringId == case_.stringId
        roleService.removeRolesFromActor(testIdentity.toSession().activeActorId, Set.of(posViewRole.stringId))
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchElasticViewWithUserWithNegRole() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(net)
                .title("Permission test")
                .authorId(testIdentity.toSession().activeActorId)
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        ProcessRole negViewRole = roleService.findProcessRoleByImportId("view_neg_role")
        roleService.assignRolesToActor(testIdentity.toSession().activeActorId, Set.of(negViewRole.stringId))

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(net.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, testIdentity.toSession().activeActorId,
                PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        // TODO: releas/8.0.0 negative view role
        assert casePage.getContent().size() == 0 && case_.processRolePermissions.getPermissions().keySet().contains(negViewRole.stringId)
        roleService.removeRolesFromActor(testIdentity.toSession().activeActorId, Set.of(negViewRole.stringId))
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchElasticViewWithUserWithoutUserRef() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .authorId(testIdentity.toSession().activeActorId)
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(netWithUserRefs.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, testIdentity.toSession().activeActorId,
                PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        assert casePage.getContent().size() == 0
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchElasticViewWithUserWithPosUserRef() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .authorId(testIdentity.toSession().activeActorId)
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        String taskId = case_.getTaskStringId("t1")
        dataService.setData(new SetDataParams(taskId, new DataSet([
                "view_ul_pos": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testIdentity.toSession().activeActorId)]))
        ] as Map<String, Field<?>>), superCreator.getLoggedSuper().activeActorId))
        case_ = workflowService.findOne(case_.stringId)
        sleep(4000)

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(netWithUserRefs.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, testIdentity.toSession().activeActorId,
                PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        // TODO: release/8.0.0 user view
        assert casePage.getContent().size() == 1 && casePage.getContent()[0].stringId == case_.stringId && case_.caseRolePermissions.getPermissions().keySet().contains(testIdentity.toSession().activeActorId)
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchElasticViewWithUserWithNegUserRef() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .authorId(testIdentity.toSession().activeActorId)
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        String taskId = case_.getTaskStringId("t1")
        dataService.setData(new SetDataParams(taskId, new DataSet([
                "view_ul_neg": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testIdentity.toSession().activeActorId)]))
        ] as Map<String, Field<?>>), superCreator.getLoggedSuper().activeActorId))
        case_ = workflowService.findOne(case_.stringId)
        sleep(4000)

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(netWithUserRefs.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, testIdentity.toSession().activeActorId,
                PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)
        // TODO: release/8.0.0 negative view user
        assert casePage.getContent().size() == 0 && case_.caseRolePermissions.getPermissions().keySet().contains(testIdentity.toSession().activeActorId)
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchElasticViewWithUserWithNegativeRoleAndPosUserRef() {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .authorId(testIdentity.toSession().activeActorId)
                .build()
        Case case_ = workflowService.createCase(createCaseParams).getCase()
        ProcessRole negViewRole = roleService.findProcessRoleByImportId("view_neg_role")
        roleService.assignRolesToActor(testIdentity.toSession().activeActorId, Set.of(negViewRole.stringId))
        String taskId = case_.getTaskStringId("t1")
        dataService.setData(new SetDataParams(taskId, new DataSet([
                "view_ul_pos": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(testIdentity.toSession().activeActorId)]))
        ] as Map<String, Field<?>>), superCreator.getLoggedSuper().activeActorId))
        case_ = workflowService.findOne(case_.stringId)
        sleep(4000)

        CaseSearchRequest caseSearchRequest = new CaseSearchRequest()
        caseSearchRequest.process = [new CaseSearchRequest.PetriNet(netWithUserRefs.getIdentifier())] as List
        Page<Case> casePage = elasticCaseService.search([caseSearchRequest] as List, testIdentity.toSession().activeActorId,
                PageRequest.of(0, 20), LocaleContextHolder.getLocale(), false)

        // TODO: release/8.0.0 view user
        assert casePage.getContent().size() == 1 && case_.caseRolePermissions.containsKey(testIdentity.toSession().activeActorId)
        roleService.removeRolesFromActor(testIdentity.toSession().activeActorId, Set.of(negViewRole.stringId))
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }
}
