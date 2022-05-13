package com.netgrif.application.engine.insurance.mvc

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach

//import com.netgrif.application.engine.orgstructure.domain.Group

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
    private static final Closure<String> TASK_ASSIGN_URL = { id -> "/api/task/assign/$id" as String }
    private static final Closure<String> TASK_FINISH_URL = { id -> "/api/task/finish/$id" as String }
    private static final Closure<String> TASK_DATA_URL = { String id -> "/api/task/$id/data" as String }

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

    private static final String FIELD_TEXT = "text"
    private static final String FIELD_ENUM = "enumeration"
    private static final String FIELD_BOOL = "boolean"
    private static final String FIELD_NUM = "number"
    private static final String FIELD_DATE = "date"

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
        processRoleService.assignRolesToUser(userService.findByEmail(USER_EMAIL, false).getId(), roles.findAll { it.importId in ["1", "2"] }.collect { it.stringId } as Set, userService.getLoggedOrSystem().transformToLoggedUser())

        auth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")
        auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()));

        mapper = net.getNet().dataSet.collectEntries { [(it.value.importId as int): (it.key)] }
    }

    private String caseId
    private String netId
    private String taskId
    private Map mapper

    @Test
    @DisplayName("Insurance Test")
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

    def coverType() {
        searchTasks(TASK_COVER_TYPE, 3)
        assignTask()
        finishTask()
    }

    def basicInfo() {
        searchTasks(TASK_BASIC_INFO, 1)
        assignTask()
        setDataBasicInfo()
        finishTask()
    }

    def property() {
        searchTasks(TASK_PROPERTY, 2)
        assignTask()
        setDataProperty()
        finishTask()
    }

    def propertyAdditional() {
        searchTasks(TASK_PROPERTY_ADDITIONAL, 3)
        assignTask()
        setDataPropertyAdditional()
        finishTask()
    }

    def propertyBuildings() {
        searchTasks(TASK_PROPERTY_BUILDINGS, 4)
        assignTask()
        setDataPropertyBuildings()
        finishTask()
    }

    def household() {
        searchTasks(TASK_HOUSEHOLD, 5)
        assignTask()
        setDataHousehold()
        finishTask()
    }

    def householdAdditional() {
        searchTasks(TASK_HOUSEHOLD_ADDITIONAL, 6)
        assignTask()
        setDataHouseholdAdditional()
        finishTask()
    }

    def summary() {
        searchTasks(TASK_SUMMARY, 7)
        assignTask()
        setDataSummary()
        finishTask()
    }

    def info() {
        searchTasks(TASK_INFO, 8)
        assignTask()
        setDataInfo()
        finishTask()
    }

    def offer() {
        searchTasks(TASK_OFFER, 9)
        assignTask()
        setDataOffer()
        finishTask()
    }

    def end() {
        searchTasks(TASK_END, 12)
    }

    def createCase() {
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
                .andExpect(jsonPath('$.outcome.aCase.title', CoreMatchers.is(CASE_NAME)))
//                .andExpect(jsonPath('$.outcome.aCase.petriNetId', CoreMatchers.is(netId)))
                .andReturn()
        def response = parseResult(result)
        caseId = response.outcome.aCase.stringId
    }

    def searchTasks(String title, int expected) {
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
                .andExpect(jsonPath('$.page.totalElements', CoreMatchers.is(expected)))
                .andReturn()
        def response = parseResult(result)
        taskId = response?._embedded?.tasks?.find { it.title == title }?.stringId
        assert taskId != null
    }

    def assignTask() {
        mvc.perform(get(TASK_ASSIGN_URL(taskId))
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success', CoreMatchers.isA(String)))
                .andReturn()
        getData()
    }

    def finishTask() {
        mvc.perform(get(TASK_FINISH_URL(taskId))
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success', CoreMatchers.isA(String)))
                .andReturn()
    }

    def getData() {
        mvc.perform(get(TASK_DATA_URL(taskId))
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andReturn()
    }

    def setData(Map data) {
        def content = JsonOutput.toJson([(taskId): data])
        def result = mvc.perform(post(TASK_DATA_URL(taskId))
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andReturn()
        def response = parseResult(result)
        getData()
    }

    def setDataBasicInfo() {
        setData([
                (mapper[101001]): [
                        value: "84105",
                        type : FIELD_TEXT
                ]
        ])
        def data = [
                (mapper[301005]): [
                        value: "Bratislava",
                        type : FIELD_ENUM
                ],
                (mapper[101002]): [
                        value: false,
                        type : FIELD_BOOL
                ],
                (mapper[101003]): [
                        value: false,
                        type : FIELD_BOOL
                ],
                (mapper[101004]): [
                        value: false,
                        type : FIELD_BOOL
                ],
                (mapper[101005]): [
                        value: "6 až 10",
                        type : FIELD_ENUM
                ],
                (mapper[101006]): [
                        value: "vlastník nehnuteľnosti",
                        type : FIELD_ENUM
                ],
                (mapper[101007]): [
                        value: "2",
                        type : FIELD_ENUM
                ],
                (mapper[101008]): [
                        value: "1",
                        type : FIELD_ENUM
                ],
                (mapper[101012]): [
                        value: false,
                        type : FIELD_BOOL
                ],
                (mapper[101014]): [
                        value: false,
                        type : FIELD_BOOL
                ],
                (mapper[101016]): [
                        value: "0",
                        type : FIELD_ENUM
                ]
        ]
        setData(data)
    }

    def setDataProperty() {
        def data = [
                (mapper[102001]): [
                        value: "byt",
                        type : FIELD_ENUM
                ],
                (mapper[105005]): [
                        value: "50.00 €",
                        type : FIELD_ENUM
                ],
                (mapper[105001]): [
                        value: 10,
                        type : FIELD_NUM,
                ],
                (mapper[105002]): [
                        value: 20,
                        type : FIELD_NUM,
                ],
                (mapper[105003]): [
                        value: 30,
                        type : FIELD_NUM,
                ],
                (mapper[102002]): [
                        value: "tehla a/alebo betón",
                        type : FIELD_ENUM
                ],
                (mapper[102003]): [
                        value: "škridla",
                        type : FIELD_ENUM
                ],
                (mapper[102004]): [
                        value: "6 až 10",
                        type : FIELD_ENUM
                ],
                (mapper[102006]): [
                        value: "1",
                        type : FIELD_ENUM
                ],
                (mapper[102007]): [
                        value: "1",
                        type : FIELD_ENUM
                ],
        ]
        setData(data)
        data = [
                (mapper[107001]): [
                        value: "15,000.00 €",
                        type : FIELD_ENUM,
                ]
        ]
        setData(data)
    }

    def setDataPropertyAdditional() {
        def data = [
                (mapper[105031]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105033]): [
                        value: true,
                        type : FIELD_BOOL
                ]
        ]
        setData(data)
        data = [
                (mapper[105032]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105034]): [
                        value: 500,
                        type : FIELD_NUM
                ]
        ]
        setData(data)
    }

    def setDataPropertyBuildings() {
        def data = [
                (mapper[105035]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105009]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105011]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105013]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105015]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105017]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105019]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105021]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105023]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105025]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105027]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[105029]): [
                        value: true,
                        type : FIELD_BOOL
                ]
        ]
        setData(data)
        data = [
                (mapper[105004]): [
                        value: 100,
                        type : FIELD_NUM
                ],
                (mapper[105008]): [
                        value: false,
                        type : FIELD_BOOL
                ],
                (mapper[105007]): [
                        value: 90_000,
                        type : FIELD_NUM
                ],
                (mapper[105010]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105012]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105014]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105016]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105018]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105020]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105022]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105024]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105026]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105028]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[105030]): [
                        value: 500,
                        type : FIELD_NUM
                ],
        ]
        setData(data)
    }

    def setDataHousehold() {
        def data = [
                (mapper[103001]): [
                        value: "byt",
                        type : FIELD_ENUM
                ],
                (mapper[106001]): [
                        value: "150.00 €",
                        type : FIELD_ENUM
                ],
                (mapper[106003]): [
                        value: 100,
                        type : FIELD_NUM
                ],
                (mapper[103002]): [
                        value: "trvalá",
                        type : FIELD_ENUM
                ],
                (mapper[103004]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[103005]): [
                        value: true,
                        type : FIELD_BOOL
                ]
        ]
        setData(data)
        data = [
                (mapper[107003]): [
                        value: "15,000.00 €",
                        type : FIELD_ENUM
                ],
                (mapper[104003]): [
                        value: "Slovenská republika",
                        type : FIELD_ENUM
                ]
        ]
        setData(data)
    }

    def setDataHouseholdAdditional() {
        def data = [
                (mapper[106004]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[106006]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[106008]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[106010]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[106012]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[106014]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[106016]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[106018]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[106020]): [
                        value: true,
                        type : FIELD_BOOL
                ]
        ]
        setData(data)
        data = [
                (mapper[106005]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[106007]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[106009]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[106011]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[106013]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[106015]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[106017]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[106019]): [
                        value: 500,
                        type : FIELD_NUM
                ],
                (mapper[106021]): [
                        value: 500,
                        type : FIELD_NUM
                ]
        ]
        setData(data)
    }

    def setDataSummary() {
        def data = [
                (mapper[108001]): [
                        value: "polročná",
                        type : FIELD_ENUM
                ],
                (mapper[108002]): [
                        value: true,
                        type : FIELD_BOOL
                ],
                (mapper[108003]): [
                        value: "20%",
                        type : FIELD_ENUM
                ]
        ]
        setData(data)
    }

    def setDataInfo() {
        def data = [
                (mapper[109007]): [
                        value: "fyzická osoba",
                        type : FIELD_ENUM
                ],
                (mapper[109010]): [
                        value: "meno",
                        type : FIELD_TEXT
                ],
                (mapper[109011]): [
                        value: "priezvisko",
                        type : FIELD_TEXT
                ],
                (mapper[109016]): [
                        value: "OP",
                        type : FIELD_ENUM
                ],
                (mapper[109017]): [
                        value: "AB123456",
                        type : FIELD_TEXT
                ],
                (mapper[109013]): [
                        value: "SR",
                        type : FIELD_ENUM
                ],
                (mapper[109014]): [
                        value: "2018-02-05",
                        type : FIELD_DATE
                ],
                (mapper[109015]): [
                        value: "1234567890",
                        type : FIELD_TEXT
                ],
                (mapper[109019]): [
                        value: "test@test.com",
                        type : FIELD_TEXT
                ],
                (mapper[109045]): [
                        value: "ulica",
                        type : FIELD_TEXT
                ],
                (mapper[109046]): [
                        value: "1",
                        type : FIELD_TEXT
                ]
        ]
        setData(data)
    }

    def setDataOffer() {
        def data = [
                (mapper[109001]): [
                        value: "2018-02-21",
                        type : FIELD_DATE
                ],
                (mapper[109006]): [
                        value: "prevodom",
                        type : FIELD_ENUM
                ]
        ]
        setData(data)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.getContentAsString(StandardCharsets.UTF_8))
    }
}
