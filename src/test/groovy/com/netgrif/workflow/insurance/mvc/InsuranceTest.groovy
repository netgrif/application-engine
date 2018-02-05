package com.netgrif.workflow.insurance.mvc

import com.netgrif.workflow.ImportHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.importer.Importer
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.TEXT_PLAIN
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
class InsuranceTest {

    private static final String CASE_CREATE_URL = "/res/workflow/case"
    private static final String TASK_SEARCH_URL = "/res/task/search?sort=priority"
    private static final def TASK_ASSIGN_URL = { id -> "/res/task/assign/$id" }
    private static final def TASK_FINISH_URL = { id -> "/res/task/finish/$id" }
    private static final def TASK_DATA_URL = { id -> "/res/task/$id/data" }

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

    @Before
    void before() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def net = importer.importPetriNet(new File("src/test/resources/insurance_portal_demo_test.xml"), CASE_NAME, CASE_INITIALS)
        assert net.isPresent()

        netId = net.get().stringId

        def org = importHelper.createOrganization("Insurance Company")
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        def processRoles = importHelper.createUserProcessRoles(["agent": "Agent", "company": "Company"], net.get())
        importHelper.createUser(new User(name: "Test", surname: "Integration", email: USER_EMAIL, password: "password"),
                [auths.get("user")] as Authority[],
                [org] as Organization[],
                [processRoles.get("agent"), processRoles.get("company")] as UserProcessRole[])

        auth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")
    }

    private String caseId
    private String netId
    private String taskId
    private def datagroups

    @Test
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
        offer()
        end()
    }

    def coverType() {
        searchTasks(TASK_COVER_TYPE, 3)
        assignTask()
        finishTask()
    }

    def basicInfo() {
        searchTasks(TASK_BASIC_INFO, 1)
        assignTask()
        getData()
//        setData()
        getData()
        finishTask()
    }

    def property() {
        searchTasks(TASK_PROPERTY, 2)
        assignTask()
        finishTask()
    }

    def propertyAdditional() {
        searchTasks(TASK_PROPERTY_ADDITIONAL, 3)
        assignTask()
        finishTask()
    }

    def propertyBuildings() {
        searchTasks(TASK_PROPERTY_BUILDINGS, 4)
        assignTask()
        finishTask()
    }

    def household() {
        searchTasks(TASK_HOUSEHOLD, 5)
        assignTask()
        finishTask()
    }

    def householdAdditional() {
        searchTasks(TASK_HOUSEHOLD_ADDITIONAL, 6)
        assignTask()
        finishTask()
    }

    def summary() {
        searchTasks(TASK_SUMMARY, 7)
        assignTask()
        finishTask()
    }

    def info() {
        searchTasks(TASK_INFO, 8)
        assignTask()
        finishTask()
    }

    def offer() {
        searchTasks(TASK_OFFER, 9)
        assignTask()
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
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .content(content)
                .contentType(APPLICATION_JSON)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.title', CoreMatchers.is(CASE_NAME)))
                .andExpect(jsonPath('$.petriNetId', CoreMatchers.is(netId)))
                .andReturn()
        def response = parseResult(result)
        caseId = response.stringId
    }

    def searchTasks(String title, int expected) {
        def content = JsonOutput.toJson([
                case: caseId
        ])
        def result = mvc.perform(post(TASK_SEARCH_URL)
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .content(content)
                .contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success', CoreMatchers.isA(String)))
                .andReturn()
    }

    def finishTask() {
        mvc.perform(get(TASK_FINISH_URL(taskId))
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success', CoreMatchers.isA(String)))
                .andReturn()
    }

    def getData() {
        def result = mvc.perform(get(TASK_DATA_URL(taskId))
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .locale(Locale.forLanguageTag(LOCALE_SK))
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andReturn()
        def response = parseResult(result)
        datagroups = response?._embedded?.dataGroups //TODO: fix get data
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.contentAsString)
    }
}