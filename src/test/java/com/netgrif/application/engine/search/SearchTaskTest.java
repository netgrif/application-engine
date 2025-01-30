package com.netgrif.application.engine.search;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.search.interfaces.ISearchService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class SearchTaskTest {
    public static final String TEST_TRANSITION_ID = "search_test_t1";
    public static final String TEST_TRANSITION2_ID = "search_test_t2";

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ISearchService searchService;

    @Autowired
    private ITaskService taskService;

    private Map<String, Authority> auths;

    @BeforeEach
    void setup() {
        testHelper.truncateDbs();
        auths = importHelper.createAuthorities(Map.of("user", Authority.user, "admin", Authority.admin));
    }

    private PetriNet importPetriNet(String fileName) {
        PetriNet testNet = importHelper.createNet(fileName).orElse(null);
        assert testNet != null;
        return testNet;
    }

    private IUser createUser(String name, String surname, String email, String authority) {
        User user = new User(email, "password", name, surname);
        Authority[] authorities = new Authority[]{auths.get(authority)};
        ProcessRole[] processRoles = new ProcessRole[]{};
        return importHelper.createUser(user, authorities, processRoles);
    }


    private static Task convertToTask(Object taskObject) {
        assert taskObject instanceof Task;
        return (Task) taskObject;
    }

    private static List<Task> convertToTaskList(Object taskListObject) {
        assert taskListObject instanceof List<?>;
        for (Object userObject : (List<?>) taskListObject) {
            assert userObject instanceof Task;
        }

        return (List<Task>) taskListObject;
    }

    private void compareTasks(Task actual, Task expected) {
        assert actual.getStringId().equals(expected.getStringId());
    }

    private void compareTasks(Task actual, List<Task> expected) {
        List<String> expectedStringIds = expected.stream().map(Task::getStringId).collect(Collectors.toList());

        assert expectedStringIds.contains(actual.getStringId());
    }

    private void compareTasks(List<Task> actual, List<Task> expected) {
        List<String> actualStringIds = actual.stream().map(Task::getStringId).collect(Collectors.toList());
        List<String> expectedStringIds = expected.stream().map(Task::getStringId).collect(Collectors.toList());

        assert actualStringIds.containsAll(expectedStringIds);
    }

    @Test
    public void testSearchById() {
        PetriNet net = importPetriNet("search/search_test.xml");

        Case caze = importHelper.createCase("Search Test", net);

        String taskId = caze.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);

        String query = String.format("task: id eq '%s'", taskId);

        long count = searchService.count(query);
        assert count == 1;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), task);
    }

    @Test
    public void testSearchByTransitionId() {
        PetriNet net = importPetriNet("search/search_test.xml");
        PetriNet net2 = importPetriNet("search/search_test2.xml");

        Case caze = importHelper.createCase("Search Test", net);
        Case caze2 = importHelper.createCase("Search Test2", net2);

        String taskId = caze.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);

        String task2Id = caze2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);

        String query = String.format("task: transitionId eq '%s'", TEST_TRANSITION_ID);
        String queryMore = String.format("tasks: transitionId eq '%s'", TEST_TRANSITION_ID);

        long count = searchService.count(query);
        assert count == 2;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), List.of(task, task2));

        Object foundTasks = searchService.search(queryMore);
        compareTasks(convertToTaskList(foundTasks), List.of(task, task2));
    }

    @Test
    public void testSearchByTitle() {
        PetriNet net = importPetriNet("search/search_test.xml");
        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net);

        String taskId = case1.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = case2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);
        String task3Id = case3.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task3 = taskService.findOne(task3Id);

        String query = String.format("task: title eq '%s'", task.getTitle());
        String queryMore = String.format("tasks: title eq '%s'", task.getTitle());

        long count = searchService.count(query);
        assert count == 3;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), List.of(task, task2, task3));

        Object foundTasks = searchService.search(queryMore);
        compareTasks(convertToTaskList(foundTasks), List.of(task, task2));
    }

    @Test
    public void testSearchByState() {
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net);

        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case2.getStringId(), user1.transformToLoggedUser());

        String taskId = case1.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = case2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);
        String task3Id = case3.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task3 = taskService.findOne(task3Id);
        String task4Id = case1.getTasks().get(TEST_TRANSITION2_ID).getTaskStringId();
        Task task4 = taskService.findOne(task4Id);
        String task5Id = case2.getTasks().get(TEST_TRANSITION2_ID).getTaskStringId();
        Task task5 = taskService.findOne(task5Id);
        String task6Id = case3.getTasks().get(TEST_TRANSITION2_ID).getTaskStringId();
        Task task6 = taskService.findOne(task6Id);

        String query = String.format("task: processId eq '%s' and state eq %s", net.getStringId(), "disabled");
        String queryOther = String.format("tasks: processId eq '%s' and state eq %s", net.getStringId(), "enabled");
        String queryMore = String.format("tasks: processId eq '%s' and state eq %s", net.getStringId(), "disabled");

        long count = searchService.count(query);
        assert count == 4;

        count = searchService.count(queryOther);
        assert count == 5;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), List.of(task, task2, task4, task5));

        Object foundTasks = searchService.search(queryOther);
        compareTasks(convertToTaskList(foundTasks), List.of(task3, task6));

        foundTasks = searchService.search(queryMore);
        compareTasks(convertToTaskList(foundTasks), List.of(task, task2, task4, task5));
    }

    @Test
    public void testSearchByUserId() {
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");
        IUser user2 = createUser("Name2", "Surname2", "Email2", "user");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net);

        String taskId = case1.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = case2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);
        String task3Id = case3.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task3 = taskService.findOne(task3Id);

        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case3.getStringId(), user2.transformToLoggedUser());

        String query = String.format("task: userId eq '%s'", user1.getStringId());
        String queryOther = String.format("task: userId eq '%s'", user2.getStringId());
        String queryMore = String.format("tasks: userId eq '%s'", user1.getStringId());

        long count = searchService.count(query);
        assert count == 2;

        count = searchService.count(queryOther);
        assert count == 1;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), List.of(task, task2));

        foundTask = searchService.search(queryOther);
        compareTasks(convertToTask(foundTask), task3);

        Object foundTasks = searchService.search(queryMore);
        compareTasks(convertToTaskList(foundTasks), List.of(task, task2));
    }

    @Test
    public void testSearchByCaseId() {
        PetriNet net = importPetriNet("search/search_test.xml");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);

        String taskId = case1.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = case1.getTasks().get(TEST_TRANSITION2_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);
        String task3Id = case2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task3 = taskService.findOne(task3Id);
        String task4Id = case2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task4 = taskService.findOne(task4Id);

        String query = String.format("task: caseId eq '%s'", case1.getStringId());
        String queryOther = String.format("task: caseId eq '%s'", case2.getStringId());
        String queryMore = String.format("tasks: caseId eq '%s'", case1.getStringId());

        long count = searchService.count(query);
        assert count == 3;

        count = searchService.count(queryOther);
        assert count == 3;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), List.of(task, task2));

        foundTask = searchService.search(queryOther);
        compareTasks(convertToTask(foundTask), List.of(task3, task4));

        Object foundTasks = searchService.search(queryMore);
        compareTasks(convertToTaskList(foundTasks), List.of(task, task2));
    }

    @Test
    public void testSearchByProcessId() {
        PetriNet net = importPetriNet("search/search_test.xml");
        PetriNet net2 = importPetriNet("search/search_test2.xml");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        Case case3 = importHelper.createCase("Search Test2", net2);

        String taskId = case1.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = case2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);
        String task3Id = case3.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task3 = taskService.findOne(task3Id);

        String query = String.format("task: processId eq '%s'", net.getStringId());
        String queryOther = String.format("task: processId eq '%s'", net2.getStringId());
        String queryMore = String.format("tasks: processId eq '%s'", net.getStringId());

        long count = searchService.count(query);
        assert count == 6;

        count = searchService.count(queryOther);
        assert count == 2;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), List.of(task, task2));

        foundTask = searchService.search(queryOther);
        compareTasks(convertToTask(foundTask), task3);

        Object foundTasks = searchService.search(queryMore);
        compareTasks(convertToTaskList(foundTasks), List.of(task, task2));
    }

    @Test
    public void testSearchByLastAssign() {
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);

        LocalDateTime before = LocalDateTime.now();
        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());

        String taskId = case1.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = case2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);

        String query = String.format("task: lastAssign eq %s", SearchUtils.toDateTimeString(task.getLastAssigned()));
        String queryBefore = String.format("task: lastAssign > %s", SearchUtils.toDateTimeString(before));
        String queryMore = String.format("tasks: lastAssign > %s", SearchUtils.toDateTimeString(before));

        long count = searchService.count(query);
        assert count == 1;

        count = searchService.count(queryBefore);
        assert count == 2;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), task);

        foundTask = searchService.search(queryBefore);
        compareTasks(convertToTask(foundTask), List.of(task, task2));

        Object foundTasks = searchService.search(queryMore);
        compareTasks(convertToTaskList(foundTasks), List.of(task, task2));
    }

    @Test
    public void testSearchByLastFinish() {
        PetriNet net = importPetriNet("search/search_test.xml");

        IUser user1 = createUser("Name1", "Surname1", "Email1", "user");

        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);

        LocalDateTime before = LocalDateTime.now();
        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        importHelper.assignTask("Test", case2.getStringId(), user1.transformToLoggedUser());
        importHelper.finishTask("Test", case2.getStringId(), user1.transformToLoggedUser());

        String taskId = case1.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = case2.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);

        String query = String.format("task: lastFinish eq %s", SearchUtils.toDateTimeString(task.getLastFinished()));
        String queryBefore = String.format("task: lastFinish > %s", SearchUtils.toDateTimeString(before));
        String queryMore = String.format("tasks: lastFinish > %s", SearchUtils.toDateTimeString(before));

        long count = searchService.count(query);
        assert count == 1;

        count = searchService.count(queryBefore);
        assert count == 2;

        Object foundTask = searchService.search(query);
        compareTasks(convertToTask(foundTask), task);

        foundTask = searchService.search(queryBefore);
        compareTasks(convertToTask(foundTask), List.of(task, task2));

        Object foundTasks = searchService.search(queryMore);
        compareTasks(convertToTaskList(foundTasks), List.of(task, task2));
    }
}
