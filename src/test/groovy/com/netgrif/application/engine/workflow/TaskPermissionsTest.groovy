package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.TaskSearchCaseRequest
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class TaskPermissionsTest {

    @Autowired
    private IElasticTaskService elasticTaskService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IUserService userService

    @Autowired
    private IDataService dataService

    @Autowired
    private ActionDelegate actionDelegate

    @Autowired
    private TestHelper testHelper

    @Autowired
    private WebApplicationContext wac

    private MockMvc mvc

    private static final String VIEW_TEST_NET = "view_permission_combinations.xml"
    private static final String VIEW_TEST_NET_NO_DEFAULT = "view_permission_combinations_no_default.xml"

    private static final String ASSIGN_TEST_NET = "assign_permission_combinations.xml"
    private static final String ASSIGN_TEST_NET_NO_DEFAULT = "assign_permission_combinations_no_default.xml"

    private static final String FINISH_TEST_NET = "finish_permission_combinations.xml"
    private static final String FINISH_TEST_NET_NO_DEFAULT = "finish_permission_combinations_no_default.xml"

    private static final String CANCEL_TEST_NET = "cancel_permission_combinations.xml"
    private static final String CANCEL_TEST_NET_NO_DEFAULT = "cancel_permission_combinations_no_default.xml"

    private static final String CORRECT_PERMISSIONS_CSV_FILEPATH = "src/test/resources/csv/permissions - correct.csv"
    private static final String CORRECT_PERMISSIONS_DEFAULT_DISABLED_CSV_FILEPATH = "src/test/resources/csv/permissions - correct default disabled.csv"

    private static Map<String, IUser> testUsers = [:]
    private static List<IUser> withRoles = []
    private static List<String> inUserRef = []
    private static Map<String, Map<String, List<String>>> correctResults = new HashMap<>()

    private static final Closure<String> TASK_ASSIGN_URL = { id -> "/api/task/assign/$id" as String }
    private static final Closure<String> TASK_FINISH_URL = { id -> "/api/task/finish/$id" as String }
    private static final Closure<String> TASK_CANCEL_URL = { id -> "/api/task/cancel/$id" as String }
    private static final String TASK_SEARCH_URL = "/api/task/search?sort=priority"
    private static final String ELASTIC_TASK_SEARCH_URL = "/api/task/search_es?sort=priority"

    @BeforeEach()
    void init() {
        testHelper.truncateDbs()
        actionDelegate.outcomes = []
        testUsers.clear()
        correctResults.clear()
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
    void testViewPermissions() {
        Case testCase = prepareTestCase("src/test/resources/petriNets/permissions/" + VIEW_TEST_NET, "t_001")
        Case testCaseNoDefault = prepareTestCase("src/test/resources/petriNets/permissions/" + VIEW_TEST_NET_NO_DEFAULT, "t_007")

        Thread.sleep(1000)

        def mapElastic = [:]
        def mapMongo = [:]
        ElasticTaskSearchRequest request = new ElasticTaskSearchRequest()
        request.useCase = [new TaskSearchCaseRequest(testCase.stringId, testCase.title)]

        ElasticTaskSearchRequest request2 = new ElasticTaskSearchRequest()
        request2.useCase = [new TaskSearchCaseRequest(testCaseNoDefault.stringId, testCaseNoDefault.title)]

        testUsers.forEach((key, value) -> {
//            Elastic task search
            Page<Task> tasks = elasticTaskService.search([request],
                    value.transformToLoggedUser(),
                    Pageable.unpaged(), LocaleContextHolder.getLocale(), false)
            List<String> list = new ArrayList<>(tasks.content).stream().map(task -> task.transitionId).collect(Collectors.toList()).sort()


            Page<Task> tasks2 = elasticTaskService.search([request2],
                    value.transformToLoggedUser(),
                    Pageable.unpaged(), LocaleContextHolder.getLocale(), false)
            List<String> list2 = new ArrayList<>(tasks2.content).stream().map(task -> task.transitionId).collect(Collectors.toList()).sort()

//            Mongo task search
            Page<Task> mongoTasksDefault = taskService.search([request], Pageable.unpaged(), value.transformToLoggedUser(), LocaleContextHolder.getLocale(), false)
            List<String> mongoListDefault = new ArrayList<>(mongoTasksDefault.content).stream().map(task -> task.transitionId).collect(Collectors.toList()).sort()
            Page<Task> mongoTasksNoDefault = taskService.search([request2], Pageable.unpaged(), value.transformToLoggedUser(), LocaleContextHolder.getLocale(), false)
            List<String> mongoListNoDefault = new ArrayList<>(mongoTasksNoDefault.content).stream().map(task -> task.transitionId).collect(Collectors.toList()).sort()

            mapElastic.put(key, [
                    "Default enabled" : list,
                    "Default DISabled": list2
            ])

            mapMongo.put(key, [
                    "Default enabled" : mongoListDefault,
                    "Default DISabled": mongoListNoDefault
            ])
        })
        compareTestResultsToExpected(mapElastic, "Elastic search", "View")
        compareTestResultsToExpected(mapMongo, "Mongo search", "View")
    }

    @Test
    void testViewPermissionsMVC() {
        Case testCase = prepareTestCase("src/test/resources/petriNets/permissions/" + VIEW_TEST_NET, "t_001")
        Case testCaseNoDefault = prepareTestCase("src/test/resources/petriNets/permissions/" + VIEW_TEST_NET_NO_DEFAULT, "t_007")

        Thread.sleep(1000)

        def mapElastic = [:]
        def mapMongo = [:]

        def content1 = JsonOutput.toJson([
                case: [
                        id: testCase.stringId
                ]
        ])
        def content2 = JsonOutput.toJson([
                case: [
                        id: testCaseNoDefault.stringId
                ]
        ])

        testUsers.forEach((key, value) -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(key, "password")
            auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
//            Elastic task search
            mapElastic.put(key, [
                    "Default enabled" : performSearch(ELASTIC_TASK_SEARCH_URL, content1, auth),
                    "Default DISabled": performSearch(ELASTIC_TASK_SEARCH_URL, content2, auth)
            ])

//            Mongo task search
            mapMongo.put(key, [
                    "Default enabled" : performSearch(TASK_SEARCH_URL, content1, auth),
                    "Default DISabled": performSearch(TASK_SEARCH_URL, content2, auth)
            ])
        })
        compareTestResultsToExpected(mapElastic, "Elastic search MVC", "View")
        compareTestResultsToExpected(mapMongo, "Mongo search MVC", "View")
    }

    @Test
    void testAssignPermission() {
        // TODO: NAE-2447 fix
        Case testCase = prepareTestCase("src/test/resources/petriNets/permissions/" + ASSIGN_TEST_NET, "t_001")
        Case testCaseNoDefault = prepareTestCase("src/test/resources/petriNets/permissions/" + ASSIGN_TEST_NET_NO_DEFAULT, "t_007")

        Thread.sleep(1000)

        def resultMap = [:]

        testUsers.forEach((key, value) -> {

            Authentication auth = new UsernamePasswordAuthenticationToken(key, "password")
            auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

            resultMap.put(key, [
                    "Default enabled" : performRequest(testCase, TASK_ASSIGN_URL, auth),
                    "Default DISabled": performRequest(testCaseNoDefault, TASK_ASSIGN_URL, auth)
            ])
        })
        compareTestResultsToExpected(resultMap, "MockMvc", "Assign")
    }

    @Test
    void testFinishPermission() {
        // TODO: NAE-2447 fix
        Case testCase = prepareTestCase("src/test/resources/petriNets/permissions/" + FINISH_TEST_NET, "t_001")
        Case testCaseNoDefault = prepareTestCase("src/test/resources/petriNets/permissions/" + FINISH_TEST_NET_NO_DEFAULT, "t_007")

        Thread.sleep(1000)

        def resultMap = [:]

        testUsers.forEach((key, value) -> {

            Authentication auth = new UsernamePasswordAuthenticationToken(key, "password")
            auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

            performRequest(testCase, TASK_ASSIGN_URL, auth)
            performRequest(testCaseNoDefault, TASK_ASSIGN_URL, auth)

            resultMap.put(key, [
                    "Default enabled" : performRequest(testCase, TASK_FINISH_URL, auth),
                    "Default DISabled": performRequest(testCaseNoDefault, TASK_FINISH_URL, auth)
            ])
        })
        compareTestResultsToExpected(resultMap, "MockMvc", "FINISH")
    }

    @Test
    void testCancelPermission() {
        // TODO: NAE-2447 fix
        Case testCase = prepareTestCase("src/test/resources/petriNets/permissions/" + CANCEL_TEST_NET, "t_001")
        Case testCaseNoDefault = prepareTestCase("src/test/resources/petriNets/permissions/" + CANCEL_TEST_NET_NO_DEFAULT, "t_007")

        Thread.sleep(1000)

        def resultMap = [:]

        testUsers.forEach((key, value) -> {

            Authentication auth = new UsernamePasswordAuthenticationToken(key, "password")
            auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))

            performRequest(testCase, TASK_ASSIGN_URL, auth)
            performRequest(testCaseNoDefault, TASK_ASSIGN_URL, auth)

            resultMap.put(key, [
                    "Default enabled" : performRequest(testCase, TASK_CANCEL_URL, auth),
                    "Default DISabled": performRequest(testCaseNoDefault, TASK_CANCEL_URL, auth)
            ])
        })
        compareTestResultsToExpected(resultMap, "MockMvc", "CANCEL")
    }

    List<String> performSearch(String searchUrl, String content, Authentication auth) {
        List<String> list = []
        MvcResult result = mvc.perform(post(searchUrl)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .locale(LocaleContextHolder.getLocale())
                .with(csrf().asHeader())
                .with(authentication(auth)))
                .andReturn()
        if (result.getResponse().getStatus() == 200) {
            def parsedResult = parseResult(result)._embedded
            parsedResult == null ?: list.addAll(parsedResult.tasks?.collect { it.transitionId }.sort())
        }
        return list
    }

    List<String> performRequest(Case testCase, Closure<String> request, Authentication auth) {
        List<String> resultTransitions = []
        testCase.tasks.forEach((taskPair) -> {
            MvcResult result = mvc.perform(get(request(taskPair.getTask()))
                    .accept(MediaTypes.HAL_JSON_VALUE)
                    .locale(LocaleContextHolder.getLocale())
                    .with(csrf().asHeader())
                    .with(authentication(auth)))
                    .andReturn()
            if (result.getResponse().getStatus() != 200) {
                return
            }
            resultTransitions.add(taskPair.getTransition())
        })
        return resultTransitions.sort()
    }

    Case prepareTestCase(String netFilePath, String transId) {
        PetriNet net = petriNetService.importPetriNet(new FileInputStream(netFilePath), VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
        Case testCase = workflowService.createCaseByIdentifier(net.identifier, "Test case", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
        withRoles.forEach {
            testUsers.put(it.getEmail(), userService.addRole(testUsers.get(it.getEmail()), net.roles.values().find { role -> role.importId == "process_role" }.stringId))
        }
        testCase = actionDelegate.setData(transId, testCase, [
                "users": [
                        "type" : "userList",
                        "value": inUserRef
                ]
        ]).getCase()
        return testCase
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
                                               Map<String, Map<String, List<String>>> expectedMap) {
        File csvFile = new File(csvFilePath)
        Map<String, String> userColumns = userColumns()

        List<String> lines = csvFile.readLines("UTF-8").findAll { it?.trim() }
        assertFalse(lines.isEmpty(), "CSV file is empty: ${csvFilePath}")
        List<String> header = lines.first().split(",", -1)*.trim()

        int transitionIdIndex = header.indexOf("Transition ID")
        assertTrue(transitionIdIndex >= 0, "Missing required column 'Transition ID' in ${csvFilePath}")

        lines.tail().each { line ->
            List<String> columns = line.split(",", -1)*.trim()
            String transitionId = columns[transitionIdIndex]

            userColumns.each { csvColumnName, userEmail ->
                int permissionIndex = header.indexOf(csvColumnName)
                assertTrue(permissionIndex >= 0, "Missing required column '${csvColumnName}' in ${csvFilePath}")

                String permissionValue = columns[permissionIndex]
                        .replace(".", "")
                        .trim()
                        .toUpperCase()

                if (permissionValue == "TRUE") {
                    expectedMap[userEmail][defaultRoleKey] << transitionId
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

    static void compareTestResultsToExpected(Map<String, Map<String, List<String>>> testResultMap, String searchType, String permissionType) {
        println("\n========== ${searchType} - ${permissionType} permissions comparison ==========")

        testUsers.keySet().each { String userEmail ->
            println("\nUser: ${userEmail}")

            ["Default enabled", "Default DISabled"].each { String defaultRoleKey ->
                Set<String> actualTransitionIds = new TreeSet<>(
                        testResultMap.get(userEmail)?.get(defaultRoleKey) ?: []
                )

                Set<String> expectedTransitionIds = new TreeSet<>(
                        correctResults.get(userEmail)?.get(defaultRoleKey) ?: []
                )

                Set<String> presentInBoth = new TreeSet<>(actualTransitionIds)
                presentInBoth.retainAll(expectedTransitionIds)

                Set<String> presentOnlyInMap = new TreeSet<>(actualTransitionIds)
                presentOnlyInMap.removeAll(expectedTransitionIds)

                Set<String> presentOnlyInCorrectResultsWithDefaultRoleMap = new TreeSet<>(expectedTransitionIds)
                presentOnlyInCorrectResultsWithDefaultRoleMap.removeAll(actualTransitionIds)

                println("\n${searchType} - ${defaultRoleKey}:")
                println("Present in both results (${presentInBoth.size()}): ${presentInBoth}")
                println("Present only in test results (${presentOnlyInMap.size()}): ${presentOnlyInMap}")
                println("Present only in correct results (${presentOnlyInCorrectResultsWithDefaultRoleMap.size()}): ${presentOnlyInCorrectResultsWithDefaultRoleMap}")

                assertTrue(presentInBoth.size() == actualTransitionIds.size() && presentInBoth.size() == expectedTransitionIds.size())
            }
        }

        println("\n=================================================")
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private def parseResult(MvcResult result) {
        return (new JsonSlurper()).parseText(result.response.getContentAsString(StandardCharsets.UTF_8))
    }
}