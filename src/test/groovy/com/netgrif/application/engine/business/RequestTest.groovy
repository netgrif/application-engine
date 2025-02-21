package com.netgrif.application.engine.business

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.authorization.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.State
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import com.netgrif.application.engine.workflow.web.responsebodies.TaskDataSets
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.nio.charset.StandardCharsets

import static com.netgrif.application.engine.workflow.domain.State.DISABLED
import static com.netgrif.application.engine.workflow.domain.State.ENABLED
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApplicationEngine.class
)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
class RequestTest {

    @Test
    void firstDepartmentTest() {
        createCase("My request")
        submitRequest(1_234d)
        register()
        firstDepartment()
        answer()
        assertRequest([
                "t1": DISABLED,
                "t2": DISABLED,
                "t3": DISABLED,
                "t4": DISABLED,
                "t5": DISABLED,
                "t6": DISABLED,
                "t7": DISABLED,
                "t8": ENABLED,
                "t9": ENABLED
        ])
    }

    @Test
    void secondDepartmentTest() {
        createCase("My request")
        submitRequest(2_345d)
        register()
        secondDepartment()
        answer()
        assertRequest([
                "t1": DISABLED,
                "t2": DISABLED,
                "t3": DISABLED,
                "t4": DISABLED,
                "t5": DISABLED,
                "t6": DISABLED,
                "t7": DISABLED,
                "t8": ENABLED,
                "t9": ENABLED
        ])
    }

    void submitRequest(double customerId) {
        searchTasks("t1")
        assignTask()
        setData(new DataSet([
                "name"        : new TextField(rawValue: "John"),
                "surname"     : new TextField(rawValue: "Doe"),
                "email"       : new TextField(rawValue: "johndoe@email.com"),
                "customer_id" : new NumberField(rawValue: customerId),
                "request_text": new TextField(rawValue: "Please change my address")
        ]))
        finishTask()
    }

    void register() {
        searchTasks("t2")
        assignTask()
        setData(new DataSet([
                "request_origin": new EnumerationMapField(rawValue: "online")
        ]))
        finishTask()
    }

    void firstDepartment() {
        searchTasks("t4")
        assignTask()
        setData(DataSet.of("answer_department", new TextField(rawValue: "Address changed. 1st dpt")))
        finishTask()
    }

    void secondDepartment() {
        searchTasks("t7")
        assignTask()
        setData(DataSet.of("answer_department", new TextField(rawValue: "Address changed. 2nd dpt")))
        finishTask()
    }

    void answer() {
        searchTasks("t5")
        assignTask()
        setData(DataSet.of("answer_registration", new TextField(rawValue: "Your address was changed")))
        finishTask()
    }

    void assertRequest(Map<String, State> taskStates) {
        Case requestCase = workflowService.findOne(caseId)
        List<Task> requestTasks = taskService.findAllByCase(caseId)

        taskStates.each { String t, State state ->
            assert requestCase.tasks[t].state == state
            assert requestTasks.find { it.transitionId == t }.state == state
        }
    }

    void createCase(String title) {
        def content = JsonOutput.toJson([
                title: title,
                netId: netId,
                color: "color"
        ])
        def result = mvc.perform(post(CASE_CREATE_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(csrf().asHeader())
                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.outcome.case.title', CoreMatchers.is(title)))
                .andExpect(jsonPath('$.outcome.case.petriNetId', CoreMatchers.is(netId)))
                .andReturn()
        def response = parseResult(result)
        caseId = response.outcome.case.stringId
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.getContentAsString(StandardCharsets.UTF_8))
    }

    void assignTask() {
        mvc.perform(get(TASK_ASSIGN_URL, taskId)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success', CoreMatchers.isA(String)))
                .andReturn()
        getData()
    }

    void finishTask() {
        mvc.perform(get(TASK_FINISH_URL, taskId)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success', CoreMatchers.isA(String)))
                .andReturn()
    }

    void getData() {
        mvc.perform(get(TASK_DATA_URL, taskId)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andReturn()
    }

    void setData(DataSet dataSet) {
        String content = objectWriter.writeValueAsString(new TaskDataSets([(taskId): dataSet]))
        mvc.perform(post(TASK_DATA_URL, taskId)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andReturn()
        getData()
    }

    void searchTasks(String transitionId) {
        def content = JsonOutput.toJson([
                query: "caseId: ${caseId}"
        ])
        def result = mvc.perform(post(TASK_SEARCH_ELASTIC_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andReturn()
        def response = parseResult(result)
        taskId = response?._embedded?.tasks?.find { it.transitionId == transitionId }?.stringId
        assert taskId != null
    }

    private static final String CASE_CREATE_URL = "/api/workflow/case"
    private static final String TASK_SEARCH_ELASTIC_URL = "/api/task/search_es?sort=priority&page=0&size=50"
    private static final String TASK_ASSIGN_URL = "/api/task/assign/{id}"
    private static final String TASK_FINISH_URL = "/api/task/finish/{id}"
    private static final String TASK_DATA_URL = "/api/task/{id}/data"
    private static final String USER_EMAIL = "test@test.com"
    private static final String LOCALE_SK = "sk"

    private Authentication auth
    private MockMvc mvc

    private WebApplicationContext wac
    private TestHelper testHelper
    private SuperCreator superCreator
    private ImportHelper importHelper
    private ObjectMapper objectMapper
    private IPetriNetService petriNetService
    private IProcessRoleService roleService
    private IUserService userService
    private IWorkflowService workflowService
    private ITaskService taskService

    @Autowired
    RequestTest(WebApplicationContext wac, TestHelper testHelper, SuperCreator superCreator, ImportHelper importHelper, ObjectMapper objectMapper, IPetriNetService petriNetService, IProcessRoleService roleService, IUserService userService, IWorkflowService workflowService, ITaskService taskService) {
        this.wac = wac
        this.testHelper = testHelper
        this.superCreator = superCreator
        this.importHelper = importHelper
        this.objectMapper = objectMapper
        this.petriNetService = petriNetService
        this.roleService = roleService
        this.userService = userService
        this.workflowService = workflowService
        this.taskService = taskService
    }

    private String caseId
    private String netId
    private String taskId
    private ObjectWriter objectWriter

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def net = petriNetService.importPetriNet(TestHelper.stream("request.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null

        netId = net.getNet().getStringId()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        def processRoles = importHelper.getProcessRolesByImportId(net.getNet(), ["first": "first", "second": "second", "system": "system", "user": "user", "registration": "registration"])
        importHelper.createUser(new User(name: "Test", surname: "Integration", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user"), auths.get("admin")] as Authority[],
                [processRoles.get("first"), processRoles.get("registration"), processRoles.get("second"), processRoles.get("system"), processRoles.get("user")] as ProcessRole[])
        List<ProcessRole> roles = roleService.findAll(netId)
        roleService.assignRolesToUser(userService.findByEmail(USER_EMAIL).stringId, roles.findAll { it.importId in ["1", "2"] }.collect { it.stringId } as Set, userService.getLoggedOrSystem().transformToLoggedUser())

        auth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")
        auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()));

        objectWriter = objectMapper.writer().withDefaultPrettyPrinter()
    }
}
