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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.util.stream.Collectors

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

    private static final String TEST_NET = "view_permission_combinations.xml"
    private static final String TEST_NET_NO_DEFAULT = "view_permission_combinations_no_default.xml"
    private static final String CORRECT_PERMISSIONS_CSV_FILEPATH = "src/test/resources/csv/permissions - correct.csv"
    private static final String CORRECT_PERMISSIONS_DEFAULT_DISABLED_CSV_FILEPATH = "src/test/resources/csv/permissions - correct default disabled.csv"
    private static Case testCase
    private static Case testCaseNoDefault
    private static Map<String, IUser> testUsers = [:]
    private static Map<String, Map<String, List<String>>> correctResults = new HashMap<>()

    @BeforeEach()
    void init() {
        testHelper.truncateDbs()
        actionDelegate.outcomes = []

        PetriNet net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/" + TEST_NET), VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
        PetriNet netNoDefault = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/" + TEST_NET_NO_DEFAULT), VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()

        testCase = workflowService.createCaseByIdentifier(net.identifier, "Test case", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
        testCaseNoDefault = workflowService.createCaseByIdentifier(netNoDefault.identifier, "Test case with no default", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()

        [
                new User("no_permissions@mail.com", "password", "No", "Permissions"),
                new User("has_role@mail.com", "password", "Has", "Role"),
                new User("in_userRef@mail.com", "password", "In", "UserRef"),
                new User("both_permissions@mail.com", "password", "Both", "Permissions")
        ].each {
            it.setState(UserState.ACTIVE)
            testUsers.put(it.getEmail(), userService.saveNew(it))
        }

        List<IUser> withRoles = [testUsers.get("has_role@mail.com"), testUsers.get("both_permissions@mail.com")]
        List<String> inUserRef = [testUsers.get("in_userRef@mail.com").stringId, testUsers.get("both_permissions@mail.com").stringId]

        withRoles.forEach {
            testUsers.put(it.getEmail(), userService.addRole(testUsers.get(it.getEmail()), net.roles.values().find { role -> role.importId == "process_role" }.stringId))
            testUsers.put(it.getEmail(), userService.addRole(testUsers.get(it.getEmail()), netNoDefault.roles.values().find { role -> role.importId == "process_role" }.stringId))
        }

        testCase = actionDelegate.setData("t_001", testCase, [
                "users": [
                        "type" : "userList",
                        "value": inUserRef
                ]
        ]).getCase()

        testCaseNoDefault = actionDelegate.setData("t_007", testCaseNoDefault, [
                "users": [
                        "type" : "userList",
                        "value": inUserRef
                ]
        ]).getCase()

        correctResults = permissionsCsvToExpectedMap()
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
        List<String> header = lines.first().split(",", -1)*.trim()

        int transitionIdIndex = header.indexOf("Transition ID")

        lines.tail().each { line ->
            List<String> columns = line.split(",", -1)*.trim()
            String transitionId = columns[transitionIdIndex]

            userColumns.each { csvColumnName, userEmail ->
                int permissionIndex = header.indexOf(csvColumnName)

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

    @Test
    void testViewPermissions() {
        def mapElastic = [:]
        def mapMongo = [:]
//        todo test for both mongo and elastic
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
        compareTestResultsToExpected(mapElastic, "Elastic search")
        compareTestResultsToExpected(mapMongo, "Mongo search")
    }

    static void compareTestResultsToExpected(Map<String, Map<String, List<String>>> testResultMap, String searchType) {
        println("\n========== ${searchType} - View permissions comparison ==========")

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

                assert presentInBoth.size() == actualTransitionIds.size() && presentInBoth.size() == expectedTransitionIds.size()
            }
        }

        println("\n=================================================")
    }
}