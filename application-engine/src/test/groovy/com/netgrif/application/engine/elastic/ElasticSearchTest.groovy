package com.netgrif.application.engine.elastic

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
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
        classes = ApplicationEngine.class
)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.yaml"
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
    private ElasticsearchTemplate template

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private IElasticCaseMappingService caseMappingService

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
        testHelper.truncateDbs()

        def net = petriNetService.importPetriNet(new ImportPetriNetParams(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())).getNet()
        def net2 = petriNetService.importPetriNet(new ImportPetriNetParams(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())).getNet()
        assert net
        assert net2

        netId = net.getStringId()
        netId2 = net2.getStringId()

        assert netId
        assert netId2

//        def org = importHelper.createGroup("Test")
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
//        def processRoles = importHelper.getProcessRoles(net.get())
        def testUser = importHelper.createUser(new User(firstName: "Test", lastName: "Integration", email: USER_EMAIL, password: USER_PASSW, state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [net.roles.values().find { it.importId == "process_role" }] as ProcessRole[])
        auth = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(testUser), USER_PASSW, testUser.authoritySet as List)

        10.times {
            def _case = importHelper.createCaseAsSuper("$it" as String, it % 2 == 0 ? net : net2)
            _case.dataSet["number"].value = it * 100.0 as Double
            _case.dataSet["enumeration"].value = _case.petriNet.dataSet["enumeration"].choices[it % 3]

            def savedCase = workflowService.save(_case)
            elasticCaseService.indexNow(caseMappingService.transform(savedCase))
        }

        waitForIndexedCases(14)

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
                        "size": 12
                ],
                "searchByAuthorUserName"    : [
                        "json": JsonOutput.toJson([
                                "author": [
                                        "username": superCreator.superUser.username
                                ]
                        ]),
                        "size": 12
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
                ],
                "searchByAuthorName"        : [
                        "json": JsonOutput.toJson([
                                "author": [
                                "name": superCreator.superUser.name
                        ]
                     ]),
                        "size": 12
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

            assert response?."_embedded"?."cases"?.size() == value.value["size"]
            assert response?."_embedded"?."cases"?.size() == value.value["size"] : "$value.key expected ${value.value["size"]}, got ${response?."_embedded"?."cases"?.size()}, response: ${result.response.contentAsString}"
        }
    }

    private MvcResult search(String content) {
        mvc.perform(
                post(SEARCH_URL)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .locale(Locale.forLanguageTag(LOCALE_SK))
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(csrf().asHeader())
                        .with(authentication(this.auth))
        )
                .andExpect(status().isOk())
                .andReturn()
    }

    private void waitForIndexedCases(long expectedCount) {
        long deadline = System.currentTimeMillis() + 15_000
        while (repository.count() < expectedCount && System.currentTimeMillis() < deadline) {
            Thread.sleep(700)
        }
        assert repository.count() >= expectedCount
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.contentAsString ?: "{}")
    }
}
