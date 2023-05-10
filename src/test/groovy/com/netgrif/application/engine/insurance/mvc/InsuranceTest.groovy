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
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField
import com.netgrif.application.engine.petrinet.domain.dataset.DateField
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationField
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.State
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import com.netgrif.application.engine.workflow.web.responsebodies.TaskDataSets
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.hamcrest.CoreMatchers
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                .param("size", "50")
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andReturn()
        def response = parseResult(result)
        taskId = response?._embedded?.tasks?.find { it.title == title && it.state == State.ENABLED.ordinal() }?.stringId
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
        def data = new DataSet([
                "301005": new EnumerationField(rawValue: new I18nString("Bratislava")),
                "101002": new BooleanField(rawValue: false),
                "101003": new BooleanField(rawValue: false),
                "101004": new BooleanField(rawValue: false),
                "101005": new EnumerationField(rawValue: new I18nString("6 až 10")),
                "101006": new EnumerationField(rawValue: new I18nString("vlastník nehnuteľnosti")),
                "101007": new EnumerationField(rawValue: new I18nString("2")),
                "101008": new EnumerationField(rawValue: new I18nString("1")),
                "101012": new BooleanField(rawValue: false),
                "101014": new BooleanField(rawValue: false),
                "101016": new EnumerationField(rawValue: new I18nString("0"))
        ])
        setData(data)
    }

    void setDataProperty() {
        DataSet data = new DataSet([
                "102001": new EnumerationField(rawValue: new I18nString("byt")),
                "105005": new EnumerationField(rawValue: new I18nString("50.00 €")),
                "105001": new NumberField(rawValue: 10D),
                "105002": new NumberField(rawValue: 20D),
                "105003": new NumberField(rawValue: 30D),
                "102002": new EnumerationField(rawValue: new I18nString("tehla a/alebo betón")),
                "102003": new EnumerationField(rawValue: new I18nString("škridla")),
                "102004": new EnumerationField(rawValue: new I18nString("6 až 10")),
                "102006": new EnumerationField(rawValue: new I18nString("1")),
                "102007": new EnumerationField(rawValue: new I18nString("1"))
        ])
        setData(data)
        data = new DataSet([
                "107001": new EnumerationField(rawValue: new I18nString("15,000.00 €"))
        ])
        setData(data)
    }

    def setDataPropertyAdditional() {
        def data = new DataSet([
                "105031": new BooleanField(rawValue: false),
                "105033": new BooleanField(rawValue: false)
        ])
        setData(data)
        data = new DataSet([
                "105032": new NumberField(rawValue: 500D),
                "105034": new NumberField(rawValue: 500D)
        ])
        setData(data)
    }

    def setDataPropertyBuildings() {
        def data = new DataSet([
                "105035": new BooleanField(rawValue: false),
                "105009": new BooleanField(rawValue: false),
                "105011": new BooleanField(rawValue: false),
                "105013": new BooleanField(rawValue: false),
                "105015": new BooleanField(rawValue: false),
                "105017": new BooleanField(rawValue: false),
                "105019": new BooleanField(rawValue: false),
                "105021": new BooleanField(rawValue: false),
                "105023": new BooleanField(rawValue: false),
                "105025": new BooleanField(rawValue: false),
                "105027": new BooleanField(rawValue: false),
                "105029": new BooleanField(rawValue: false)
        ])
        setData(data)
        data = new DataSet([
                "105004": new NumberField(rawValue: 100D),
                "105008": new BooleanField(rawValue: false),
                "105007": new NumberField(rawValue: 90_000D),
                "105010": new NumberField(rawValue: 500D),
                "105012": new NumberField(rawValue: 500D),
                "105014": new NumberField(rawValue: 500D),
                "105016": new NumberField(rawValue: 500D),
                "105018": new NumberField(rawValue: 500D),
                "105020": new NumberField(rawValue: 500D),
                "105022": new NumberField(rawValue: 500D),
                "105024": new NumberField(rawValue: 500D),
                "105026": new NumberField(rawValue: 500D),
                "105028": new NumberField(rawValue: 500D),
                "105030": new NumberField(rawValue: 500D)
        ])
        setData(data)
    }

    def setDataHousehold() {
        def data = new DataSet([
                "103001": new EnumerationField(rawValue: new I18nString("byt")),
                "106001": new EnumerationField(rawValue: new I18nString("150.00 €")),
                "106003": new NumberField(rawValue: 100D),
                "103002": new EnumerationField(rawValue: new I18nString("trvalá")),
                "103004": new BooleanField(rawValue: false),
                "103005": new BooleanField(rawValue: false),


        ])
        setData(data)
        data = new DataSet([
                "107003": new EnumerationField(rawValue: new I18nString("15,000.00 €")),
                "104003": new EnumerationField(rawValue: new I18nString("Slovenská republika"))
        ])
        setData(data)
    }

    def setDataHouseholdAdditional() {
        def data = new DataSet([
                "106004": new BooleanField(rawValue: true),
                "106006": new BooleanField(rawValue: true),
                "106008": new BooleanField(rawValue: true),
                "106010": new BooleanField(rawValue: true),
                "106012": new BooleanField(rawValue: true),
                "106014": new BooleanField(rawValue: true),
                "106016": new BooleanField(rawValue: true),
                "106018": new BooleanField(rawValue: true),
                "106020": new BooleanField(rawValue: true),
        ])
        setData(data)
        data = new DataSet([
                "106005": new NumberField(rawValue: 500D),
                "106007": new NumberField(rawValue: 500D),
                "106009": new NumberField(rawValue: 500D),
                "106011": new NumberField(rawValue: 500D),
                "106013": new NumberField(rawValue: 500D),
                "106015": new NumberField(rawValue: 500D),
                "106017": new NumberField(rawValue: 500D),
                "106019": new NumberField(rawValue: 500D),
                "106021": new NumberField(rawValue: 500D),
        ])
        setData(data)
    }

    def setDataSummary() {
        def data = new DataSet([
                "108001": new EnumerationField(rawValue: new I18nString("polročná")),
                "108002": new BooleanField(rawValue: true),
                "108003": new EnumerationField(rawValue: new I18nString("20%")),
        ])
        setData(data)
    }

    def setDataInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        formatter = formatter.withLocale(Locale.getDefault())
        LocalDate date = LocalDate.parse("2018-02-05", formatter);
        def data = new DataSet([
                "109007": new EnumerationField(rawValue: new I18nString("fyzická osoba")),
                "109010": new TextField(rawValue: "meno"),
                "109011": new TextField(rawValue: "priezvisko"),
                "109016": new EnumerationField(rawValue: new I18nString("OP")),
                "109017": new TextField(rawValue: "AB123456"),
                "109013": new EnumerationField(rawValue: new I18nString("SR")),
                "109014": new DateField(rawValue: date),
                "109015": new TextField(rawValue: "1234567890"),
                "109019": new TextField(rawValue: "test@test.com"),
                "109045": new TextField(rawValue: "ulica"),
                "109046": new TextField(rawValue: "1"),
        ])
        setData(data)
    }

    def setDataOffer() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        formatter = formatter.withLocale(Locale.getDefault())
        LocalDate date = LocalDate.parse("2018-02-21", formatter);
        def data = new DataSet([
                "109001": new DateField(rawValue: date),
                "109006": new EnumerationField(rawValue: new I18nString("prevodom"))
        ])
        setData(data)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.getContentAsString(StandardCharsets.UTF_8))
    }
}
