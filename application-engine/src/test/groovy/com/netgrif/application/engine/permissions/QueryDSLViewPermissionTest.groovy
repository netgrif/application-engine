package com.netgrif.application.engine.permissions

import com.netgrif.application.engine.auth.service.AuthorityService
import com.netgrif.application.engine.auth.service.GroupService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.workflow.params.CreateCaseParams
import com.netgrif.application.engine.workflow.params.DeleteCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.Task
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
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
    private UserService userService

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private AuthorityService authorityService

    @Autowired
    private GroupService groupService

    @Autowired
    private IDataService dataService

    @Autowired
    private TestHelper testHelper

    private static final String USER_EMAIL = "user123987645@test.com"

    private PetriNet net
    private PetriNet netWithUserRefs
    private AbstractUser testUser
    private Authority userAuthority

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/view_permission_test.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        assert net.getNet() != null
        this.net = net.getNet()

        ImportPetriNetEventOutcome netWithUserRefs = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/view_permission_with_userRefs_test.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        assert netWithUserRefs.getNet() != null
        this.netWithUserRefs = netWithUserRefs.getNet()

        userAuthority = authorityService.getOrCreate(Authority.user)

        testUser = importHelper.createUser(new User(firstName: "Role", lastName: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [userAuthority] as Authority[], [] as ProcessRole[])
    }

    @Test
    void testSearchQueryDSLViewWithoutRole() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 0
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchQueryDSLViewWithUserWithPosRole() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        ProcessRole posViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_pos_role")
        userService.addRole(testUser, posViewRole.getStringId())

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 1 && casePage.getContent()[0].stringId == case_.stringId
        userService.removeRole(testUser, posViewRole.getStringId())
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchQueryDSLViewWithUserWithNegRole() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        ProcessRole negViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_neg_role")
        userService.addRole(testUser, negViewRole.getStringId())

        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 0 && case_.negativeViewRoles.contains(negViewRole.stringId)
        userService.removeRole(testUser, negViewRole.getStringId())
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchQueryDSLViewWithoutActorRef() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())

        assert casePage.getContent().size() == 0
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchQueryDSLViewWithPosActorRef() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 1 && casePage.getContent()[0].stringId == case_.stringId && case_.viewActors.contains(testUser.getStringId())

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 0

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [groupService.getDefaultSystemGroup().stringId],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 1 && casePage.getContent()[0].stringId == case_.stringId
                && case_.viewActors.contains(groupService.getDefaultSystemGroup().getStringId())

        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchTaskQueryDSLViewWithPosActorRef() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        TaskSearchRequest request = new TaskSearchRequest()
        request.process = [new com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet(netWithUserRefs.getStringId())]
        Page<Task> taskPage = taskService.search([request],
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale(), false)
        assert taskPage.getContent().size() == 1 && taskPage.content[0].caseId == case_.stringId && taskPage.content[0].viewActors.contains(testUser.getStringId())

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        request = new TaskSearchRequest()
        request.process = [new com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet(netWithUserRefs.getStringId())]
        taskPage = taskService.search([request], PageRequest.of(0, 20),
                ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale(), false)
        assert taskPage.getContent().size() == 0

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [groupService.getDefaultSystemGroup().stringId],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        request = new TaskSearchRequest()
        request.process = [new com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet(netWithUserRefs.getStringId())]
        taskPage = taskService.search([request], PageRequest.of(0, 20),
                ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale(), false)
        assert taskPage.getContent().size() == 1 && taskPage.content[0].caseId == case_.stringId
                && taskPage.content[0].viewActors.contains(groupService.getDefaultSystemGroup().stringId)

        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchTaskQueryDSLViewWithUserWithPosRole() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        ProcessRole posViewRole = this.netWithUserRefs.getRoles().values().find(v -> v.getImportId() == "view_pos_role")
        userService.addRole(testUser, posViewRole.getStringId())

        TaskSearchRequest request = new TaskSearchRequest()
        request.process = [new com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet(netWithUserRefs.getStringId())]
        Page<Task> taskPage = taskService.search([request],
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale(), false)

        assert taskPage.getContent().size() == 1 && taskPage.getContent()[0].caseId == case_.stringId
        userService.removeRole(testUser, posViewRole.getStringId())
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchQueryDSLViewWithNegActorRef() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_neg": [
                        "value": [testUser.stringId],
                        "type": "actorList"
                ],
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 0 && case_.negativeViewActors.contains(testUser.getStringId())

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_neg": [
                        "value": [],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 1 && case_.negativeViewActors.isEmpty()

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_neg": [
                        "value": [groupService.getDefaultSystemGroup().stringId],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 0 && case_.negativeViewActors.contains(groupService.getDefaultSystemGroup().stringId)

        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }

    @Test
    void testSearchQueryDSLViewWithNegRoleAndPosActorRef() {
        Case case_ = workflowService.createCase(CreateCaseParams.with()
                .process(netWithUserRefs)
                .title("Permission test")
                .color("")
                .author(ActorTransformer.toLoggedUser(testUser))
                .build()).getCase()
        String taskId = (new ArrayList<>(case_.getTasks())).get(0).task

        ProcessRole negViewRole = this.net.getRoles().values().find(v -> v.getImportId() == "view_neg_role")
        userService.addRole(testUser, negViewRole.getStringId())

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [testUser.stringId],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        Page<Case> casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 1 && case_.viewActors.contains(testUser.stringId)

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 0 && case_.viewActors.isEmpty()

        case_ = dataService.setData(taskId, ImportHelper.populateDataset([
                "view_ul_pos": [
                        "value": [groupService.getDefaultSystemGroup().stringId],
                        "type": "actorList"
                ]
        ] as Map)).getCase()
        casePage = workflowService.search(["petriNet": ["identifier": netWithUserRefs.getIdentifier()], "fullText": "VPT"] as Map,
                PageRequest.of(0, 20), ActorTransformer.toLoggedUser(testUser), LocaleContextHolder.getLocale())
        assert casePage.getContent().size() == 1 && case_.viewActors.contains(groupService.getDefaultSystemGroup().stringId)

        userService.removeRole(testUser, negViewRole.getStringId())
        workflowService.deleteCase(new DeleteCaseParams(case_.getStringId()))
    }
}
