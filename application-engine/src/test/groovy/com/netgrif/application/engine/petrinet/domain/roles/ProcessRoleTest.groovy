package com.netgrif.application.engine.petrinet.domain.roles

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ProcessRoleTest {

    private static final String CASE_CREATE_URL = "/api/workflow/case"
    private static final String TASK_SEARCH_URL = "/api/task/search?sort=priority"

    private static final String LOCALE_SK = "sk"

    private static final String CASE_NAME = "Test case"
    private static final String CASE_INITIALS = "TC"
    private static final String USER_EMAIL_VIEW = "ProcessRoleTest@test.com"
    private static final String USER_EMAIL_PERFORM = "ProcessRoleTestPerform@test.com"
    private static final String USER_EMAIL_BOTH = "ProcessRoleTestPerformView@test.com"

    private Authentication auth

    private MockMvc mvc

    @Autowired
    private Importer importer

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private ProcessRoleRepository userProcessRoleRepository

    @Autowired
    private SuperCreatorRunner superCreator;

    @Autowired
    TestHelper testHelper

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def net = petriNetService.importPetriNet(new ImportPetriNetParams(new FileInputStream("src/test/resources/rolref_view.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert net.getNet() != null

        this.netId = net.getNet().getStringId()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        def processRoles = userProcessRoleRepository.findAllByProcessId(this.netId, Pageable.unpaged()).content
        AbstractUser viewUser = importHelper.createUser(new User(firstName: "Test", lastName: "Integration", email: USER_EMAIL_VIEW, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [processRoles.find {
                    it.getStringId() == net.getNet().roles.values().find {
                        it.name.defaultValue == "View"
                    }.stringId
                }] as ProcessRole[])

        AbstractUser performUser = importHelper.createUser(new User(firstName: "Test", lastName: "Integration", email: USER_EMAIL_PERFORM, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [processRoles.find { it.getStringId() == net.getNet().roles.values().find { it.name.defaultValue == "Perform" }.stringId }] as ProcessRole[])

        AbstractUser bothUser = importHelper.createUser(new User(firstName: "Test", lastName: "Integration", email: USER_EMAIL_BOTH, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [processRoles.find { it.getStringId() == net.getNet().roles.values().find { it.name.defaultValue == "View" }.stringId },
                 processRoles.find { it.getStringId() == net.getNet().roles.values().find { it.name.defaultValue == "Perform" }.stringId }] as ProcessRole[])

        viewAuth = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(viewUser), "password", viewUser.authoritySet as List)
        performAuth = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(performUser), "password", performUser.authoritySet as List)
        bothAuth = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(bothUser), "password", bothUser.authoritySet as List)
    }

    private String caseId
    private String netId
    private String taskId
    private Authentication viewAuth
    private Authentication performAuth
    private Authentication bothAuth

    @Test
    void testViewLogic() {
        this.auth = viewAuth
        createCase()
        searchTasks("View", 1)

        this.auth = performAuth
//        createCase()
        searchTasks("Perform", 1)

        this.auth = bothAuth
//        createCase()
        searchTasks("View", 2)
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
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .with(csrf().asHeader())
                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andReturn()
        def response = parseResult(result)
        caseId = createdCaseId(response)
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

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.contentAsString)
    }

    private static String createdCaseId(def response) {
        def outcome = response.outcome
        def aCase = outcome?.get("acase") ?: outcome?.get("aCase") ?: outcome?.get("case")
        if (aCase?.id) {
            return aCase.id
        }
        def matcher = (response.success as String) =~ /Case with id (.+) was created/
        assert matcher.find() : "Create case response did not contain a case id: ${response}"
        return matcher.group(1)
    }
}
