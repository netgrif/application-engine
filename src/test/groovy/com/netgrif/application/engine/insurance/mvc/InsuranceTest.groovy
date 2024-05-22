package com.netgrif.application.engine.insurance.mvc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import com.netgrif.application.engine.workflow.web.responsebodies.TaskDataSets
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.hamcrest.CoreMatchers
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled

import org.junit.jupiter.api.DisplayName
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Disabled
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
class InsuranceTest {

    private static final String CASE_CREATE_URL = "/api/workflow/case"
    private static final String TASK_SEARCH_URL = "/api/task/search?sort=priority"
    private static final String TASK_ASSIGN_URL = "/api/task/assign/{id}"
    private static final String TASK_FINISH_URL = "/api/task/finish/{id}"
    private static final String TASK_DATA_URL = "/api/task/{id}/data"

    private static final String TASK_COVER_TYPE = "Nehnuteľnosť a domácnosť"
    private static final String TASK_BASIC_INFO = "Základné informácie"
    private static final String TASK_PROPERTY = "Nehnuteľnosť"
    private static final String TASK_PROPERTY_ADDITIONAL = "Doplnkové poistenie nehnuteľnosti"
    private static final String TASK_PROPERTY_BUILDINGS = "Vedľajšie stavby"
    private static final String TASK_HOUSEHOLD = "Domácnosť"
    private static final String TASK_HOUSEHOLD_ADDITIONAL = "Doplnkové poistenie domácnosti"
    private static final String TASK_SUMMARY = "Sumár"
    private static final String TASK_INFO = "Údaje o poistníkovi a mieste poistenia"
    private static final String TASK_OFFER = "Údaje o zmluve"
    private static final String TASK_END = "Základné  informácie"

    private static final String LOCALE_SK = "sk"

    private static final String CASE_NAME = "Test case"
    private static final String CASE_INITIALS = "TC"
    private static final String USER_EMAIL = "test@test.com"

    private Authentication auth

    private MockMvc mvc

    @Autowired
    private Importer importer

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IProcessRoleService processRoleService

    @Autowired
    private IUserService userService

    @Autowired
    private TestHelper testHelper
    // TODO: NAE-1858 remove, for test only
    @Autowired
    private IWorkflowService workflowService
    @Autowired
    private ObjectMapper objectMapper

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/insurance_portal_demo_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null

        netId = net.getNet().getStringId()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        def processRoles = importHelper.getProcessRolesByImportId(net.getNet(), ["agent": "1", "company": "2"])
        importHelper.createUser(new User(name: "Test", surname: "Integration", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user"), auths.get("admin")] as Authority[],
                [processRoles.get("agent"), processRoles.get("company")] as ProcessRole[])
        List<ProcessRole> roles = processRoleService.findAll(netId)
        processRoleService.assignRolesToUser(userService.findByEmail(USER_EMAIL, false).stringId, roles.findAll { it.importId in ["1", "2"] }.collect { it.stringId } as Set, userService.getLoggedOrSystem().transformToLoggedUser())

        auth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")
        auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()));

        objectWriter = objectMapper.writer().withDefaultPrettyPrinter()
    }

    private String caseId
    private String netId
    private String taskId
    private ObjectWriter objectWriter

    @Test
    @Disabled
    @DisplayName("Insurance Test")
    @Ignore // TODO: release/7.0.0 fix post methods with wrong json
    void test() {
        createCase()
        coverType()
        basicInfo()
        property()
        propertyAdditional()
        propertyBuildings()
        household()
        householdAdditional()
        summary()
        info()
        offer()
    }

    void coverType() {
        searchTasks(TASK_COVER_TYPE)
        assignTask()
        finishTask()
    }

    void basicInfo() {
        searchTasks(TASK_BASIC_INFO)
        assignTask()
        setDataBasicInfo()
        finishTask()
    }

    void property() {
        searchTasks(TASK_PROPERTY)
        assignTask()
        setDataProperty()
        finishTask()
    }

    void propertyAdditional() {
        searchTasks(TASK_PROPERTY_ADDITIONAL)
        assignTask()
        setDataPropertyAdditional()
        finishTask()
    }

    void propertyBuildings() {
        searchTasks(TASK_PROPERTY_BUILDINGS)
        assignTask()
        setDataPropertyBuildings()
        finishTask()
    }

    void household() {
        searchTasks(TASK_HOUSEHOLD)
        assignTask()
        setDataHousehold()
        finishTask()
    }

    void householdAdditional() {
        searchTasks(TASK_HOUSEHOLD_ADDITIONAL)
        assignTask()
        setDataHouseholdAdditional()
        finishTask()
    }

    void summary() {
        searchTasks(TASK_SUMMARY)
        assignTask()
        setDataSummary()
        finishTask()
    }

    void info() {
        searchTasks(TASK_INFO)
        assignTask()
        setDataInfo()
        finishTask()
    }

    void offer() {
        searchTasks(TASK_OFFER)
        assignTask()
        setDataOffer()
        finishTask()
    }

    void end() {
        searchTasks(TASK_END)
    }

    void createCase() {
        def content = JsonOutput.toJson([
                title: CASE_NAME,
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
                .andExpect(jsonPath('$.outcome.case.title', CoreMatchers.is(CASE_NAME)))
                .andExpect(jsonPath('$.outcome.case.petriNetId', CoreMatchers.is(netId)))
                .andReturn()
        def response = parseResult(result)
        caseId = response.outcome.case.stringId
    }

    void searchTasks(String title) {
        def content = JsonOutput.toJson([
                case: [
                        id: caseId
                ]
        ])
        def result = mvc.perform(post(TASK_SEARCH_URL)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andReturn()
        def response = parseResult(result)
        taskId = response?._embedded?.tasks?.find { it.title == title }?.stringId
        assert taskId != null
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

    void setDataBasicInfo() {
        setData(new DataSet([
                "101001": new TextField(rawValue:"84105")
        ]))
        def data = [
                (mapper[301005]): [
                        value: [value: "Bratislava"]
                ],
                (mapper[101002]): [
                        value: [value: false]
                ],
                (mapper[101003]): [
                        value: [value: false]
                ],
                (mapper[101004]): [
                        value: [value: false]
                ],
                (mapper[101005]): [
                        value: [value: "6 až 10"]
                ],
                (mapper[101006]): [
                        value: [value: "vlastník nehnuteľnosti"]
                ],
                (mapper[101007]): [
                        value: [value: "2"]
                ],
                (mapper[101008]): [
                        value: [value: "1"]
                ],
                (mapper[101012]): [
                        value: [value: false]
                ],
                (mapper[101014]): [
                        value: [value: false]
                ],
                (mapper[101016]): [
                        value: [value: "0"]
                ]
        ]
        setData(data)
    }

    void setDataProperty() {
        DataSet data = [
                (mapper[102001]): [
                        value: [value: "byt"]
                ],
                (mapper[105005]): [
                        value: [value: "50.00 €"]
                ],
                (mapper[105001]): [
                        value: [value: 10],
                ],
                (mapper[105002]): [
                        value: [value: 20],
                ],
                (mapper[105003]): [
                        value: [value: 30],
                ],
                (mapper[102002]): [
                        value: [value: "tehla a/alebo betón"]
                ],
                (mapper[102003]): [
                        value: [value: "škridla"]
                ],
                (mapper[102004]): [
                        value: [value: "6 až 10"]
                ],
                (mapper[102006]): [
                        value: [value: "1"]
                ],
                (mapper[102007]): [
                        value: [value: "1"]
                ],
        ]
        setData(data)
        data = [
                (mapper[107001]): [
                        value: [value: "15,000.00 €"],
                ]
        ]
        setData(data)
    }

    def setDataPropertyAdditional() {
        def data = [
                (mapper[105031]): [
                        value: [value: true]
                ],
                (mapper[105033]): [
                        value: [value: true]
                ]
        ]
        setData(data)
        data = [
                (mapper[105032]): [
                        value: [value: 500]
                ],
                (mapper[105034]): [
                        value: [value: 500]
                ]
        ]
        setData(data)
    }

    def setDataPropertyBuildings() {
        def data = [
                (mapper[105035]): [
                        value: [value: true]
                ],
                (mapper[105009]): [
                        value: [value: true]
                ],
                (mapper[105011]): [
                        value: [value: true]
                ],
                (mapper[105013]): [
                        value: [value: true]
                ],
                (mapper[105015]): [
                        value: [value: true]
                ],
                (mapper[105017]): [
                        value: [value: true]
                ],
                (mapper[105019]): [
                        value: [value: true]
                ],
                (mapper[105021]): [
                        value: [value: true]
                ],
                (mapper[105023]): [
                        value: [value: true]
                ],
                (mapper[105025]): [
                        value: [value: true]
                ],
                (mapper[105027]): [
                        value: [value: true]
                ],
                (mapper[105029]): [
                        value: [value: true]
                ]
        ]
        setData(data)
        data = [
                (mapper[105004]): [
                        value: [value: 100]
                ],
                (mapper[105008]): [
                        value: [value: false]
                ],
                (mapper[105007]): [
                        value: [value: 90_000]
                ],
                (mapper[105010]): [
                        value: [value: 500]
                ],
                (mapper[105012]): [
                        value: [value: 500]
                ],
                (mapper[105014]): [
                        value: [value: 500]
                ],
                (mapper[105016]): [
                        value: [value: 500]
                ],
                (mapper[105018]): [
                        value: [value: 500]
                ],
                (mapper[105020]): [
                        value: [value: 500]
                ],
                (mapper[105022]): [
                        value: [value: 500]
                ],
                (mapper[105024]): [
                        value: [value: 500]
                ],
                (mapper[105026]): [
                        value: [value: 500]
                ],
                (mapper[105028]): [
                        value: [value: 500]
                ],
                (mapper[105030]): [
                        value: [value: 500]
                ],
        ]
        setData(data)
    }

    def setDataHousehold() {
        def data = [
                (mapper[103001]): [
                        value: [value: "byt"]
                ],
                (mapper[106001]): [
                        value: [value: "150.00 €"]
                ],
                (mapper[106003]): [
                        value: [value: 100]
                ],
                (mapper[103002]): [
                        value: [value: "trvalá"]
                ],
                (mapper[103004]): [
                        value: [value: true]
                ],
                (mapper[103005]): [
                        value: [value: true]
                ]
        ]
        setData(data)
        data = [
                (mapper[107003]): [
                        value: [value: "15,000.00 €"]
                ],
                (mapper[104003]): [
                        value: [value: "Slovenská republika"]
                ]
        ]
        setData(data)
    }

    def setDataHouseholdAdditional() {
        def data = [
                (mapper[106004]): [
                        value: [value: true]
                ],
                (mapper[106006]): [
                        value: [value: true]
                ],
                (mapper[106008]): [
                        value: [value: true]
                ],
                (mapper[106010]): [
                        value: [value: true]
                ],
                (mapper[106012]): [
                        value: [value: true]
                ],
                (mapper[106014]): [
                        value: [value: true]
                ],
                (mapper[106016]): [
                        value: [value: true]
                ],
                (mapper[106018]): [
                        value: [value: true]
                ],
                (mapper[106020]): [
                        value: [value: true]
                ]
        ]
        setData(data)
        data = [
                (mapper[106005]): [
                        value: [value: 500]
                ],
                (mapper[106007]): [
                        value: [value: 500]
                ],
                (mapper[106009]): [
                        value: [value: 500]
                ],
                (mapper[106011]): [
                        value: [value: 500]
                ],
                (mapper[106013]): [
                        value: [value: 500]
                ],
                (mapper[106015]): [
                        value: [value: 500]
                ],
                (mapper[106017]): [
                        value: [value: 500]
                ],
                (mapper[106019]): [
                        value: [value: 500]
                ],
                (mapper[106021]): [
                        value: [value: 500]
                ]
        ]
        setData(data)
    }

    def setDataSummary() {
        def data = [
                (mapper[108001]): [
                        value: [value: "polročná"]
                ],
                (mapper[108002]): [
                        value: [value: true]
                ],
                (mapper[108003]): [
                        value: [value: "20%"]
                ]
        ]
        setData(data)
    }

    def setDataInfo() {
        def data = [
                (mapper[109007]): [
                        value: [value: "fyzická osoba"]
                ],
                (mapper[109010]): [
                        value: [value: "meno"]
                ],
                (mapper[109011]): [
                        value: [value: "priezvisko"]
                ],
                (mapper[109016]): [
                        value: [value: "OP"]
                ],
                (mapper[109017]): [
                        value: [value: "AB123456"]
                ],
                (mapper[109013]): [
                        value: [value: "SR"]
                ],
                (mapper[109014]): [
                        value: [value: "2018-02-05"]
                ],
                (mapper[109015]): [
                        value: [value: "1234567890"]
                ],
                (mapper[109019]): [
                        value: [value: "test@test.com"]
                ],
                (mapper[109045]): [
                        value: [value: "ulica"]
                ],
                (mapper[109046]): [
                        value: [value: "1"]
                ]
        ]
        setData(data)
    }

    def setDataOffer() {
        def data = [
                (mapper[109001]): [
                        value: [value: "2018-02-21"]
                ],
                (mapper[109006]): [
                        value: [value: "prevodom"]
                ]
        ]
        setData(data)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.getContentAsString(StandardCharsets.UTF_8))
    }
}
