package com.netgrif.workflow.elastic

import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.elastic.domain.ElasticCase
import com.netgrif.workflow.elastic.domain.ElasticCaseRepository
import com.netgrif.workflow.elastic.domain.ElasticTask
import com.netgrif.workflow.importer.service.Config
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
class ElasticSearchTest {

    private static final String LOCALE_SK = "sk"
    private static final String USER_EMAIL = "test@test.com"
    private static final String USER_PASSW = "password"
    private static final String SEARCH_URL = "/api/workflow/case/search"
    public static final String APPLICATION_HAL_JSON = "application/hal+json"
    public static final String PROCESS_TITLE = "Elastic test"
    public static final String PROCESS_INITIALS = "EST"

    @Autowired
    private Importer importer

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private ElasticCaseRepository repository

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ElasticsearchTemplate template

    @Autowired
    private SuperCreator superCreator

    private Authentication auth
    private MockMvc mvc
    private String netId
    private Map testCases

    @Before
    void before() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
        auth = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSW)
        template.deleteIndex(ElasticCase.class)
        template.createIndex(ElasticCase.class)
        template.deleteIndex(ElasticTask.class)
        template.createIndex(ElasticTask.class)
        template.putMapping(ElasticCase.class)
        template.putMapping(ElasticTask.class)

        repository.deleteAll()

        def net = importer.importPetriNet(new File("src/test/resources/all_data.xml"), PROCESS_TITLE, PROCESS_INITIALS, new Config())
        assert net.isPresent()

        netId = net.get().getStringId()

        def org = importHelper.createGroup("Test")
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        def processRoles = importHelper.createUserProcessRoles(["process_role": "Process role"], net.get())
        def testUser = importHelper.createUser(new User(name: "Test", surname: "Integration", email: USER_EMAIL, password: USER_PASSW, state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [org] as Group[],
                [processRoles.get("process_role")] as UserProcessRole[])

        10.times {
            def _case = importHelper.createCase("$it" as String, net.get())
            if (it % 2 == 0) {
                _case.processIdentifier = "test"
                _case.author = testUser.transformToAuthor()
            }
            _case.dataSet["number"].value = it * 100.0 as Double
            _case.dataSet["enumeration"].value = _case.petriNet.dataSet["enumeration"].choices[it % 3]
            workflowService.save(_case)
        }

        testCases = [
                "searchByPetriNetIdentifier": [
                        "json": JsonOutput.toJson([
                                "petriNet": [
                                        "id": "Default"
                                ]
                        ]),
                        "size": 10
                ],
                "searchByProcessIdentifier" : [
                        "json": JsonOutput.toJson([
                                "processIdentifier": "Default"
                        ]),
                        "size": 10
                ],
                "searchByAuthorId"          : [
                        "json": JsonOutput.toJson([
                                "author": [
                                        "id": superCreator.superUser.id
                                ]
                        ]),
                        "size": 10
                ],
                "searchByAuthorName"        : [
                        "json": JsonOutput.toJson([
                                "author": [
                                        "name": superCreator.superUser.fullName
                                ]
                        ]),
                        "size": 10
                ],
                "searchByAuthorEmail"       : [
                        "json": JsonOutput.toJson([
                                "author": [
                                        "email": superCreator.superUser.email
                                ]
                        ]),
                        "size": 10
                ],
                "searchByEnumeration"       : [
                        "json": JsonOutput.toJson([
                                "data": [
                                        "enumeration": "Carol"
                                ]
                        ]),
                        "size": 3
                ],
                "searchByNumber"            : [
                        "json": JsonOutput.toJson([
                                "data": [
                                        "number": 300.0
                                ]
                        ]),
                        "size": 1
                ]
        ]
    }

    @Test
    void testSearch() {
        testCases.entrySet().each { value ->
            def content = value["json"] as String
            def result = search(content)
            def response = parseResult(result)

            assert response?."_embedded"?."cases"?.size == value["size"]
        }
    }

    private MvcResult search(String content) {
        mvc.perform(
                post(SEARCH_URL)
                        .accept(APPLICATION_HAL_JSON)
                        .locale(Locale.forLanguageTag(LOCALE_SK))
                        .content(content)
                        .contentType(APPLICATION_JSON)
                        .with(csrf().asHeader())
                        .with(authentication(this.auth))
        )
                .andExpect(status().isOk())
                .andReturn()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.contentAsString ?: "{}")
    }
}
