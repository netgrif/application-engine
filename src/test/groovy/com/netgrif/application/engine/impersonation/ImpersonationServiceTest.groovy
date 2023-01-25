package com.netgrif.application.engine.impersonation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImpersonationRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowAuthorizationService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.TaskSearchCaseRequest
import groovy.json.JsonSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.session.web.http.SessionRepositoryFilter
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDateTime

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ImpersonationServiceTest {

    public static final String X_AUTH_TOKEN = "x-auth-token"

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper helper

    @Autowired
    private IUserService userService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IDataService dataService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IImpersonationService impersonationService

    @Autowired
    private IImpersonationAuthorizationService impersonationAuthorizationService

    @Autowired
    private ITaskAuthorizationService taskAuthorizationService

    @Autowired
    private IWorkflowAuthorizationService workflowAuthorizationService

    @Autowired
    private WebApplicationContext wac

    MockMvc mvc

    Authentication auth1
    Authentication auth2
    Authentication adminUserAuth

    IUser user1
    IUser user2
    IUser adminUser

    PetriNet testNet

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        SessionRepositoryFilter<?> filter = wac.getBean(SessionRepositoryFilter.class);
        mvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(filter).apply(springSecurity()).build()

        testNet = helper.createNet("impersonation_test.xml").get()
        def authority = authorityService.getOrCreate(Authority.user)
        def authorityAnon = authorityService.getOrCreate(Authority.anonymous)
        def authorityAdmin = authorityService.getOrCreate(Authority.admin)

        user1 = helper.createUser(new User(name: "Test", surname: "User", email: "test@netgrif.com", password: "password", state: UserState.ACTIVE),
                [authority] as Authority[],
                [] as ProcessRole[])

        auth1 = new UsernamePasswordAuthenticationToken(user1.transformToLoggedUser(), (user1 as User).password, user1.authorities)
        auth1.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

        user2 = helper.createUser(new User(name: "Test", surname: "User2", email: "test2@netgrif.com", password: "password", state: UserState.ACTIVE),
                [authority, authorityAnon] as Authority[],
                testNet.roles.values() as ProcessRole[])

        auth2 = new UsernamePasswordAuthenticationToken(user2.transformToLoggedUser(), (user2 as User).password, user2.authorities)
        auth2.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

        adminUser = helper.createUser(new User(name: "Admin", surname: "User", email: "admin@netgrif.com", password: "password", state: UserState.ACTIVE),
                [authority, authorityAdmin] as Authority[],
                testNet.roles.values() as ProcessRole[])

        adminUserAuth = new UsernamePasswordAuthenticationToken(adminUser.transformToLoggedUser(), (adminUser as User).password, adminUser.authorities)
        adminUserAuth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
    }

    @Test
    void testImpersonationAdmin() {
        SecurityContextHolder.getContext().setAuthentication(adminUserAuth)
        assert impersonationAuthorizationService.canImpersonateUser(userService.loggedUserFromContext, user2.stringId)
        impersonationService.impersonateUser(user2.stringId)
        def impersonated = userService.loggedUser.getSelfOrImpersonated()
        assert userService.loggedUser.isImpersonating()
        assert impersonated.stringId == user2.stringId
        assert impersonated.authorities.collect { it.stringId }.sort() == user2.authorities.collect { it.stringId }.sort()
        assert impersonated.processRoles.collect { it.stringId }.sort() == user2.processRoles.collect { it.stringId }.sort()
    }

    @Test
    void testImpersonation() {
        def config = setup()
        assert impersonationAuthorizationService.canImpersonate(userService.loggedUserFromContext, config.stringId)
        impersonationService.impersonateByConfig(config.stringId)
        assert userService.loggedUser.isImpersonating()
        assert userService.loggedUser.getSelfOrImpersonated().stringId == user2.stringId

        impersonationService.endImpersonation()
        assert !userService.loggedUser.isImpersonating()
    }

    @Test
    void testImpersonationRolesAndAuths() {
        def role = user2.processRoles.find { it.importId == "test_role" }
        def auth = user2.authorities.find { it.name == Authority.user }
        def config = setup([role.stringId], [auth.stringId, authorityService.getOrCreate(Authority.admin).stringId])

        impersonationService.impersonateByConfig(config.stringId)
        def impersonatedRoles = userService.loggedUser.getImpersonated().getProcessRoles()
        def impersonatedAuths = userService.loggedUser.getImpersonated().getAuthorities()
        assert impersonatedRoles.size() == 2 && impersonatedRoles.any { it.stringId ==role.stringId }  // default role counts
        assert impersonatedAuths.size() == 1 && impersonatedAuths[0].stringId == auth.stringId

        def transformedUser = userService.loggedUser.transformToLoggedUser()
        def transformedUserImpersonated = transformedUser.getSelfOrImpersonated()
        assert transformedUser.isImpersonating()
        assert transformedUserImpersonated.getProcessRoles().size() == 2 && transformedUserImpersonated.getProcessRoles().any { it == role.stringId }  // default role counts
        assert transformedUserImpersonated.getAuthorities().size() == 1 && (transformedUserImpersonated.getAuthorities()[0] as Authority).stringId == auth.stringId
    }

    @Test
    void testTaskSearchAssignFinish() {
        def config = setup()
        impersonationService.impersonateByConfig(config.stringId)

        assert workflowAuthorizationService.canCallCreate(userService.loggedUserFromContext, testNet.stringId)
        def testCase = createTestCase()
        assert workflowAuthorizationService.canCallDelete(userService.loggedUserFromContext, testCase.stringId)
        def testTask1 = loadTask(testCase, "t1")

        sleep(8000) // elastic

        def caseReq = new CaseSearchRequest()
        caseReq.process = [new CaseSearchRequest.PetriNet(testCase.processIdentifier)]
        def cases = elasticCaseService.search([caseReq], userService.loggedUser.transformToLoggedUser(), PageRequest.of(0, 1), LocaleContextHolder.locale, false)
        assert !cases.content.isEmpty()

        def searchReq = new TaskSearchRequest()
        searchReq.transitionId = ["t1"]
        searchReq.useCase = [new TaskSearchCaseRequest(testCase.stringId, null)]
        def tasks = taskService.search([searchReq], PageRequest.of(0, 1), userService.loggedUser.transformToLoggedUser(), LocaleContextHolder.locale, false)
        assert tasks.content[0].stringId == testTask1.stringId

        assert taskAuthorizationService.canCallAssign(userService.loggedUserFromContext, testTask1.stringId)
        taskService.assignTask(userService.loggedUser.transformToLoggedUser(), testTask1.stringId)
        testTask1 = reloadTask(testTask1)
        assert testTask1.userId == user2.stringId

        assert taskAuthorizationService.canCallSaveData(userService.loggedUserFromContext, testTask1.stringId)
        assert taskAuthorizationService.canCallSaveFile(userService.loggedUserFromContext, testTask1.stringId)

        assert taskAuthorizationService.canCallFinish(userService.loggedUserFromContext, testTask1.stringId)
        taskService.finishTask(userService.loggedUser.transformToLoggedUser(), testTask1.stringId)
    }

    @Test
    void testAuthorization() {
        def config = setup()
        sleep(4000) // elastic

        def logged = userService.loggedUser.transformToLoggedUser()
        assert impersonationAuthorizationService.canImpersonate(logged, config.stringId)
        assert impersonationAuthorizationService.canImpersonateUser(logged, user2.stringId)

        config.dataSet["valid_to"].value = LocalDateTime.now().minusMinutes(1)
        workflowService.save(config)
        sleep(4000)

        assert !impersonationAuthorizationService.canImpersonate(logged, config.stringId)
        assert !impersonationAuthorizationService.canImpersonateUser(logged, user2.stringId)
    }

    @Test
    void testAuthMe() {
        def config = setup()
        def result = mvc.perform(get("/api/auth/login")
                .with(httpBasic(user1.email, "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn()
        def token = result.response.getHeader(X_AUTH_TOKEN)

        result = mvc.perform(post("/api/impersonate/config/" + config.stringId)
                .header(X_AUTH_TOKEN, token)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn()

        result = mvc.perform(get("/api/user/me")
                .header(X_AUTH_TOKEN, token)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn()

        String string = result.getResponse().getContentAsString()
        def json = new JsonSlurper().parse(string.getBytes())
        assert json["impersonated"] != null

        result = mvc.perform(post("/api/impersonate/clear")
                .header(X_AUTH_TOKEN, token)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn()

        result = mvc.perform(get("/api/user/me")
                .header(X_AUTH_TOKEN, token)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn()

        string = result.getResponse().getContentAsString()
        json = new JsonSlurper().parse(string.getBytes())
        assert json["impersonated"] == null
    }

    def setup(List<String> roles = null, List<String> auths = null) {
        def config = createConfigCase(user2, user1.stringId, roles, auths)
        SecurityContextHolder.getContext().setAuthentication(auth1)
        return config
    }

    def createConfigCase(IUser user, String impersonator, List<String> roles = null, List<String> auths = null) {
        def caze = helper.createCase("config", petriNetService.getNewestVersionByIdentifier(ImpersonationRunner.IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER))
        def owner = new UserFieldValue(user)
        caze.dataSet["impersonated"].value = owner
        caze.dataSet["impersonated_email"].value = owner.email
        caze.dataSet["config_owner"].value = new UserListFieldValue([owner])
        caze.dataSet["impersonators"].value = [impersonator]
        caze.dataSet["impersonated_roles"].value = roles ?: user.processRoles.stringId as List
        caze.dataSet["impersonated_authorities"].value = auths ?: user.authorities.stringId as List
        caze.dataSet["valid_from"].value = LocalDateTime.now().minusDays(1)

        /* set options so elastic indexing works */
        caze.dataSet["impersonators"].options = [(impersonator): new I18nString(impersonator)]
        caze.dataSet["impersonated_roles"].options = (caze.dataSet["impersonated_roles"].value as List).collectEntries { [(it): new I18nString(it as String)] } as Map<String, I18nString>
        caze.dataSet["impersonated_authorities"].options = (caze.dataSet["impersonated_authorities"].value as List).collectEntries { [(it): new I18nString(it as String)] } as Map<String, I18nString>
        caze = workflowService.save(caze)
        def initTask = caze.tasks.find { it.transition == "t2" }.task
        taskService.assignTask(userService.system.transformToLoggedUser(), initTask)
        taskService.finishTask(userService.system.transformToLoggedUser(), initTask)
        return workflowService.findOne(caze.stringId)
    }

    def createTestCase() {
        return helper.createCase("test", testNet)
    }

    def setData(Case caze, String transition, Map<String, String> dataSet) {
        dataService.setData(caze.tasks.find { it.transition == transition }.task, helper.populateDataset(dataSet.collectEntries {
            [(it.key): (["value": it.value, "type": "text"])]
        }))
        return workflowService.findOne(caze.stringId)
    }

    def loadTask(Case caze, String trans) {
        return taskService.findById(caze.tasks.find { it.transition == trans }.task)
    }

    def reloadTask(Task task) {
        return taskService.findById(task.stringId)
    }

}
