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
import groovy.json.JsonBuilder

import java.util.stream.Collectors

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class TaskPermissionsTest {

    @Autowired
    private IElasticTaskService elasticTaskService

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
    private static Case testCase
    private static Case testCaseNoDefault
    private static Map<String, IUser> testUsers = [:]

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
            testUsers.put(it.getEmail(), userService.addRole(testUsers.get(it.getEmail()), netNoDefault.roles.values().find { role -> role.importId == "process_role_no_default" }.stringId))
        }

        testCase = actionDelegate.setData("t_001", testCase, [
                "users": [
                        "type": "userList",
                        "value": inUserRef
                ]
        ]).getCase()

        testCaseNoDefault = actionDelegate.setData("t_007", testCaseNoDefault, [
                "users": [
                        "type": "userList",
                        "value": inUserRef
                ]
        ]).getCase()
        def a = []
    }

    @Test
    void testViewPermissions() {
        def map = [:]
        ElasticTaskSearchRequest request = new ElasticTaskSearchRequest()
        request.useCase = [new TaskSearchCaseRequest(testCase.stringId, testCase.title)]

        ElasticTaskSearchRequest request2 = new ElasticTaskSearchRequest()
        request2.useCase = [new TaskSearchCaseRequest(testCaseNoDefault.stringId, testCaseNoDefault.title)]

        testUsers.forEach( (key, value) -> {
            Page<Task> tasks = elasticTaskService.search([request],
                    value.transformToLoggedUser(),
                    Pageable.unpaged(), LocaleContextHolder.getLocale(), false)
            List<String> list = new ArrayList<>(tasks.content).stream().map(task ->  task.transitionId).collect(Collectors.toList()).sort()


            Page<Task> tasks2 = elasticTaskService.search([request2],
                    value.transformToLoggedUser(),
                    Pageable.unpaged(), LocaleContextHolder.getLocale(), false)
            List<String> list2 = new ArrayList<>(tasks2.content).stream().map(task ->  task.transitionId).collect(Collectors.toList()).sort()

            map.put(key, [
                    "Default enabled": "$key -> number of found tasks with default role ENABLED: " + list.size() + "; tasks found: $list",
                    "Default DISabled": "$key -> number of found tasks with default role DISABLED: " + list2.size() + "; tasks found: $list2"
            ])
        })
        println(new JsonBuilder(map).toPrettyString())
    }
}
