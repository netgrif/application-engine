package com.netgrif.workflow.elastic

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.elastic.domain.ElasticCaseRepository
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
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

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchTest)

    private static final String LOCALE_SK = "sk"
    private static final String USER_EMAIL = "test@test.com"
    private static final String USER_PASSW = "password"
    private static final String SEARCH_URL = "/api/workflow/case/search"

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private ElasticCaseRepository repository

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ElasticsearchRestTemplate template

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    private Authentication auth
    private MockMvc mvc
    private String netId, netId2
    private Map testCases

    @BeforeEach
    void before() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
        auth = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSW)
        testHelper.truncateDbs()

        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        def net2 = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.isPresent()
        assert net2.isPresent()

        netId = net.get().getStringId()
        netId2 = net2.get().getStringId()

        def org = importHelper.createGroup("Test")
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
//        def processRoles = importHelper.getProcessRoles(net.get())
        def testUser = importHelper.createUser(new User(name: "Test", surname: "Integration", email: USER_EMAIL, password: USER_PASSW, state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [net.get().roles.values().find { it.importId == "process_role" }] as ProcessRole[])

        10.times {
            def _case = importHelper.createCase("$it" as String, it % 2 == 0 ? net.get() : net2.get())
            _case.dataSet["number"].value = it * 100.0 as Double
            _case.dataSet["enumeration"].value = _case.petriNet.dataSet["enumeration"].choices[it % 3]
            workflowService.save(_case)
        }

        testCases = [
                "searchByPetriNetIdentifier": [
                        "json": JsonOutput.toJson([
                                "process": [
                                        "identifier": "all_data"
                                ]
                        ]),
                        "size": 10
                ],
                "searchByAuthorId"          : [
                        "json": JsonOutput.toJson([
                                "author": [
                                        "id": superCreator.superUser.stringId
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
            log.info "Testing $value.key"
            def content = value.value["json"] as String
            def result = search(content)
            def response = parseResult(result)

            assert response?."_embedded"?."cases"?.size == value.value["size"]
        }
    }

    private MvcResult search(String content) {
        mvc.perform(
                post(SEARCH_URL)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .locale(Locale.forLanguageTag(LOCALE_SK))
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
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
