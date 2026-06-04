package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.nio.charset.StandardCharsets

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@Slf4j
@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class CasePermissionsTest {

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IUserService userService

    @Autowired
    private IDataService dataService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ActionDelegate actionDelegate

    @Autowired
    private WebApplicationContext wac

    private MockMvc mvc

    private static final String CREATE_CASE_PERMISSION_DIRECTORY = "src/test/resources/petriNets/permissions/case/default/create"
    private static final String CREATE_CASE_PERMISSION_DEFAULT_DISABLED_DIRECTORY = "src/test/resources/petriNets/permissions/case/defaultDisabled/create"

    private static final String DELETE_CASE_PERMISSION_DIRECTORY = "src/test/resources/petriNets/permissions/case/default/delete"
    private static final String DELETE_CASE_PERMISSION_DEFAULT_DISABLED_DIRECTORY = "src/test/resources/petriNets/permissions/case/defaultDisabled/delete"

    private static final String VIEW_CASE_PERMISSION_DIRECTORY = "src/test/resources/petriNets/permissions/case/default/view"
    private static final String VIEW_CASE_PERMISSION_DEFAULT_DISABLED_DIRECTORY = "src/test/resources/petriNets/permissions/case/defaultDisabled/view"

    private static final String CORRECT_PERMISSIONS_CSV_FILEPATH = "src/test/resources/csv/permissions - correct.csv"
    private static final String CORRECT_PERMISSIONS_DEFAULT_DISABLED_CSV_FILEPATH = "src/test/resources/csv/permissions - correct default disabled.csv"

    private static final String CREATE_CASE_CORRECT_PERMISSIONS_CSV_FILEPATH = "src/test/resources/csv/create case permissions - correct.csv"
    private static final String CREATE_CASE_CORRECT_PERMISSIONS_DEFAULT_DISABLED_CSV_FILEPATH = "src/test/resources/csv/create case permissions - correct default disabled.csv"

    private static final String CASE_BASE_URL = "/api/workflow/case"

    private static Map<String, IUser> testUsers = [:]
    private static List<IUser> withRoles = []
    private static List<String> inUserRef = []
    private static Map<String, Map<String, List<String>>> correctResults = new HashMap<>()

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        testUsers.clear()
        actionDelegate.outcomes = []
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        [
                new User("no_permissions@mail.com", "password", "No", "Permissions"),
                new User("has_role@mail.com", "password", "Has", "Role"),
                new User("in_userRef@mail.com", "password", "In", "UserRef"),
                new User("both_permissions@mail.com", "password", "Both", "Permissions")
        ].each {
            it.setState(UserState.ACTIVE)
            testUsers.put(it.getEmail(), userService.saveNew(it))
        }

        withRoles = [testUsers.get("has_role@mail.com"), testUsers.get("both_permissions@mail.com")]
        inUserRef = [testUsers.get("in_userRef@mail.com").stringId, testUsers.get("both_permissions@mail.com").stringId]
        correctResults = permissionsCsvToExpectedMap()
    }

    @Test
    void testCreateCasePermission() {
        testUsers.remove("in_userRef@mail.com")
        testUsers.remove("both_permissions@mail.com")
        withRoles.removeIf { it.email == "both_permissions@mail.com" }
        Map<String, PetriNet> testNets = importTestNets(CREATE_CASE_PERMISSION_DIRECTORY)
        Map<String, PetriNet> testNetsNoDefault = importTestNets(CREATE_CASE_PERMISSION_DEFAULT_DISABLED_DIRECTORY)
        Map<String, Map<String, List<String>>> results = [:]
        testUsers.forEach((email, user) -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(email, "password")
            auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

            results.put(email, [
                    "Default enabled" : resolveCreateWithNets(testNets, auth),
                    "Default DISabled": resolveCreateWithNets(testNetsNoDefault, auth)
            ])
        })
        correctResults = importCorrectResultsForCreatePermission()
        compareTestResultsToExpected(results, "Create")
    }

    @Test
    void testDeleteCasePermission() {
        Map<String, PetriNet> testNets = importTestNets(DELETE_CASE_PERMISSION_DIRECTORY)
        Map<String, PetriNet> testNetsNoDefault = importTestNets(DELETE_CASE_PERMISSION_DEFAULT_DISABLED_DIRECTORY)
        Map<String, Map<String, List<String>>> results = [:]
        testUsers.forEach((email, user) -> {

            results.put(email, [
                    "Default enabled" : resolveDeleteWithNets(testNets, user),
                    "Default DISabled": resolveDeleteWithNets(testNetsNoDefault, user)
            ])
        })
        compareTestResultsToExpected(results, "Delete")
    }

    @Test
    void testViewCasePermission() {
        Map<String, PetriNet> testNets = importTestNets(VIEW_CASE_PERMISSION_DIRECTORY)
        createTestCases(testNets)
        Map<String, PetriNet> testNetsNoDefault = importTestNets(VIEW_CASE_PERMISSION_DEFAULT_DISABLED_DIRECTORY)
        createTestCases(testNetsNoDefault)
        Map<String, Map<String, List<String>>> resultsElastic = [:]

        testUsers.forEach((email, user) -> {

            resultsElastic.put(email, [
                    "Default enabled" : resolveViewWithNets(testNets, user),
                    "Default DISabled": resolveViewWithNets(testNetsNoDefault, user)
            ])
        })
        compareTestResultsToExpected(resultsElastic, "View")
    }

    void createTestCases(Map<String, PetriNet> testNets) {
        testNets.forEach((identifier, net) -> {
            def content = JsonOutput.toJson([
                    netId: net.stringId
            ])
            Authentication authSuper = new UsernamePasswordAuthenticationToken("super@netgrif.com", "password")
            authSuper.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
            String caseId = parseResult(performRequest(buildCreateCaseRequest(content, authSuper))).outcome.aCase.stringId
            Case createdCase = workflowService.findOne(caseId)
            actionDelegate.setData("t_001", createdCase, [
                    "users": [
                            "type" : "userList",
                            "value": inUserRef
                    ]
            ]).getCase()
        })
    }

    List<String> resolveViewWithNets(Map<String, PetriNet> testNets, IUser user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user.email, "password")
        auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
        List<String> found = []
        testNets.forEach((identifier, net) -> {
            MockHttpServletRequestBuilder request = buildElasticSearchCaseRequest(net.identifier, auth)

            if (performSearchRequestAndResolveSuccess(request, net.identifier)) {
                found.add(net.identifier)
            }
        })
        return found.sort()
    }

    List<String> resolveDeleteWithNets(Map<String, PetriNet> testNets, IUser user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user.email, "password")
        auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
        List<String> deleted = []
        testNets.forEach((identifier, net) -> {
            def content = JsonOutput.toJson([
                    netId: net.stringId
            ])

            Authentication authSuper = new UsernamePasswordAuthenticationToken("super@netgrif.com", "password")
            authSuper.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
            String caseId = parseResult(performRequest(buildCreateCaseRequest(content, authSuper))).outcome.aCase.stringId
            Case createdCase = workflowService.findOne(caseId)
            actionDelegate.setData("t_001", createdCase, [
                    "users": [
                            "type" : "userList",
                            "value": inUserRef
                    ]
            ]).getCase()

            if (performRequestAndResolveSuccess(buildDeleteCaseRequest(caseId, auth))) {
                deleted.add(net.identifier)
            }
        })
        return deleted.sort()
    }

    List<String> resolveCreateWithNets(Map<String, PetriNet> testNets, Authentication auth) {
        List<String> created = []
        testNets.forEach((identifier, net) -> {
            def content = JsonOutput.toJson([
                    netId: net.stringId
            ])

            boolean result = performRequestAndResolveSuccess(buildCreateCaseRequest(content, auth))
            if (result) {
                created.add(net.identifier)
            }
        })
        return created.sort()
    }

    static MockHttpServletRequestBuilder buildCreateCaseRequest(String content, Authentication auth) {
        return post(CASE_BASE_URL)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(LocaleContextHolder.getLocale())
                .with(csrf().asHeader())
                .with(authentication(auth))
    }

    static MockHttpServletRequestBuilder buildDeleteCaseRequest(String caseId, Authentication auth) {
        return delete(CASE_BASE_URL.concat("/${caseId}"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(LocaleContextHolder.getLocale())
                .with(csrf().asHeader())
                .with(authentication(auth))
    }

    static MockHttpServletRequestBuilder buildElasticSearchCaseRequest(String netIdentifier, Authentication auth) {
        return post(CASE_BASE_URL.concat("/search"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(JsonOutput.toJson([
                        "process": [
                                "identifier": netIdentifier
                        ],
                ]))
                .locale(LocaleContextHolder.getLocale())
                .with(csrf().asHeader())
                .with(authentication(auth))
    }

    boolean performRequestAndResolveSuccess(MockHttpServletRequestBuilder request) {
        MvcResult result = performRequest(request)
        if (result.getResponse().getStatus() == 200) {
            return true
        }
        return false
    }

    boolean performSearchRequestAndResolveSuccess(MockHttpServletRequestBuilder request, String netIdentifier) {
        MvcResult result = performRequest(request)
        Object parsedResult = parseResult(result)
        if (parsedResult.page.totalElements == 1 && parsedResult._embedded.cases[0].processIdentifier == netIdentifier) {
            return true
        }
        return false
    }

    MvcResult performRequest(MockHttpServletRequestBuilder request) {
        return mvc.perform(request).andReturn()
    }

    Map<String, PetriNet> importTestNets(String testNetDirectory) {
        File testNetsDirectory = new File(testNetDirectory)
        assertTrue(testNetsDirectory.isDirectory())
        File[] testNets = testNetsDirectory.listFiles()
        assertTrue(testNets.size() > 0)
        Map<String, PetriNet> importedNets = [:]
        for (File testNet in testNets) {
            try (FileInputStream fis = new FileInputStream(testNet)) {
                PetriNet net = petriNetService.importPetriNet(fis, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
                withRoles.forEach { user ->
                    assignRoleToUser(net, user)
                }
                importedNets.put(net.identifier, net)
            } catch (FileNotFoundException e) {
                log.error("Could not import net [${testNet.name}]", e)
                throw new IllegalArgumentException(e)
            }
        }
        return importedNets
    }

    void assignRoleToUser(PetriNet net, IUser user) {
        testUsers.put(user.email, userService.addRole(user, net.roles.values().find { role -> role.importId == "process_role" }.stringId))
    }

    static Map<String, Map<String, List<String>>> permissionsCsvToExpectedMap() {
        Map<String, Map<String, List<String>>> expectedMap = initializeExpectedPermissionsMap()

        addPermissionsCsvToExpectedMap(
                CORRECT_PERMISSIONS_CSV_FILEPATH,
                "Default enabled",
                expectedMap
        )

        addPermissionsCsvToExpectedMap(
                CORRECT_PERMISSIONS_DEFAULT_DISABLED_CSV_FILEPATH,
                "Default DISabled",
                expectedMap
        )

        return expectedMap
    }

    static void addPermissionsCsvToExpectedMap(String csvFilePath,
                                               String defaultRoleKey,
                                               Map<String, Map<String, List<String>>> expectedMap,
                                               Map<String, String> userColumns = [:]
    ) {
        File csvFile = new File(csvFilePath)
        if (userColumns.isEmpty()) {
            userColumns = this.userColumns()
        }

        List<String> lines = csvFile.readLines("UTF-8").findAll { it?.trim() }
        assertFalse(lines.isEmpty(), "CSV file is empty: ${csvFilePath}")
        List<String> header = lines.first().split(",", -1)*.trim()

        int transitionTitleIndex = header.indexOf("Transition title")
        assertTrue(transitionTitleIndex >= 0, "Missing required column 'Transition title' in ${csvFilePath}")

        lines.tail().each { line ->
            List<String> columns = line.split(",", -1)*.trim()
            String title = columns[transitionTitleIndex]

            userColumns.each { csvColumnName, userEmail ->
                int permissionIndex = header.indexOf(csvColumnName)
                assertTrue(permissionIndex >= 0, "Missing required column '${csvColumnName}' in ${csvFilePath}")

                String permissionValue = columns[permissionIndex]
                        .replace(".", "")
                        .trim()
                        .toUpperCase()

                if (permissionValue == "TRUE") {
                    expectedMap[userEmail][defaultRoleKey] << title.replace("=", "_").replace(" ", "_")
                }
            }
        }

        expectedMap.values().each { Map<String, List<String>> permissionsByDefaultRole ->
            permissionsByDefaultRole[defaultRoleKey].sort()
        }
    }

    static Map<String, Map<String, List<String>>> initializeExpectedPermissionsMap() {
        return userColumns().values().collectEntries { String email ->
            [
                    email,
                    [
                            "Default enabled" : [],
                            "Default DISabled": []
                    ]
            ]
        }
    }

    static Map<String, String> userColumns() {
        return [
                "No permissions"             : "no_permissions@mail.com",
                "Has role"                   : "has_role@mail.com",
                "Is in userList"             : "in_userRef@mail.com",
                "Has role and is in userList": "both_permissions@mail.com"
        ]
    }

    static void compareTestResultsToExpected(Map<String, Map<String, List<String>>> testResultMap, String permissionType) {
        println("\n========== ${permissionType} permissions comparison ==========")

        testUsers.keySet().each { String userEmail ->
            println("\nUser: ${userEmail}")

            ["Default enabled", "Default DISabled"].each { String defaultRoleKey ->
                Set<String> actualNetIdentifiersIds = new TreeSet<>(
                        testResultMap.get(userEmail)?.get(defaultRoleKey) ?: []
                )

                Set<String> expectedNetIdentifiers = new TreeSet<>(
                        correctResults.get(userEmail)?.get(defaultRoleKey)?.collect { permissionType.toLowerCase().concat("_case_").concat(it) } ?: []
                )

                Set<String> presentInBoth = new TreeSet<>(actualNetIdentifiersIds)
                presentInBoth.retainAll(expectedNetIdentifiers)

                Set<String> presentOnlyInMap = new TreeSet<>(actualNetIdentifiersIds)
                presentOnlyInMap.removeAll(expectedNetIdentifiers)

                Set<String> presentOnlyInCorrectResultsWithDefaultRoleMap = new TreeSet<>(expectedNetIdentifiers)
                presentOnlyInCorrectResultsWithDefaultRoleMap.removeAll(actualNetIdentifiersIds)

                println("\n${defaultRoleKey}:")
                println("Present in both results (${presentInBoth.size()}): ${presentInBoth}")
                println("Present only in test results (${presentOnlyInMap.size()}): ${presentOnlyInMap}")
                println("Present only in correct results (${presentOnlyInCorrectResultsWithDefaultRoleMap.size()}): ${presentOnlyInCorrectResultsWithDefaultRoleMap}")

                assertTrue(presentInBoth.size() == actualNetIdentifiersIds.size() && presentInBoth.size() == expectedNetIdentifiers.size())
            }
        }

        println("\n=================================================")
    }

    static importCorrectResultsForCreatePermission() {
        Map<String, Map<String, List<String>>> expectedMap = initializeExpectedPermissionsMap()

        Map<String, String> userColumns = userColumns()
        userColumns.remove("Is in userList")
        userColumns.remove("Has role and is in userList")


        addPermissionsCsvToExpectedMap(
                CREATE_CASE_CORRECT_PERMISSIONS_CSV_FILEPATH,
                "Default enabled",
                expectedMap, userColumns
        )

        addPermissionsCsvToExpectedMap(
                CREATE_CASE_CORRECT_PERMISSIONS_DEFAULT_DISABLED_CSV_FILEPATH,
                "Default DISabled",
                expectedMap, userColumns
        )

        return expectedMap
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.getContentAsString(StandardCharsets.UTF_8))
    }
}