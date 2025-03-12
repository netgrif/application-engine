package com.netgrif.application.engine.search;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.search.interfaces.ISearchService;
import com.netgrif.application.engine.search.utils.SearchUtils;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.State;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.search.utils.SearchTestUtils.*;
import static com.netgrif.application.engine.search.utils.SearchUtils.toDateTimeString;

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

    private IUser createUser(String name, String surname, String email) {
        User user = new User(email, "password", name, surname);
        Authority[] authorities = new Authority[]{auths.get("user")};
        ProcessRole[] processRoles = new ProcessRole[]{};
        return importHelper.createUser(user, authorities, processRoles);
    }

    private void searchAndCompare(String query, Task expectedResult) {
        long count = searchService.count(query);
        assert count == 1;

        Object actual = searchService.search(query);
        compareById(convertToObject(actual, Task.class), expectedResult, Task::getStringId);
    }

    private void searchAndCompare(String query, List<Task> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObject(actual, Task.class), expected, Task::getStringId);
    }

    private void searchAndCompareAsList(String query, List<Task> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObjectList(actual, Task.class), expected, Task::getStringId);
    }

    private void searchAndCompareAsListInOrder(String query, List<Task> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareByIdInOrder(convertToObjectList(actual, Task.class), expected, Task::getStringId);
    }

    @Test
    public void testSearchById() {
        PetriNet net = importPetriNet("search/search_test.xml");
        Case caze = importHelper.createCase("Search Test", net);
        String taskId = caze.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = caze.getTasks().get(TEST_TRANSITION2_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);

        String query = String.format("task: id eq '%s'", taskId);

        searchAndCompare(query, task);

        // in list
        String queryInList = String.format("tasks: id in ('%s', '%s')", task.getStringId(), task2.getStringId());

        searchAndCompareAsList(queryInList, List.of(task, task2));

        // sort
        String querySort = String.format("tasks: caseId eq '%s' sort by id", caze.getStringId());
        String querySort2 = String.format("tasks: caseId eq '%s' sort by id desc", caze.getStringId());

        List<Task> case1Tasks = caze.getTasks().values().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTaskStringId()))
                .collect(Collectors.toList());
        List<Task> asc = case1Tasks.stream()
                .sorted(Comparator.comparing(Task::getStringId))
                .collect(Collectors.toList());
        List<Task> desc = case1Tasks.stream()
                .sorted(Comparator.comparing(Task::getStringId).reversed())
                .collect(Collectors.toList());

        searchAndCompareAsListInOrder(querySort, asc);
        searchAndCompareAsListInOrder(querySort2, desc);
    }

    @Test
    public void testSearchByTransitionId() {
        PetriNet net = importPetriNet("search/search_test.xml");
        Case case1 = importHelper.createCase("Search Test", net);
        String taskId = case1.getTasks().get(TEST_TRANSITION_ID).getTaskStringId();
        Task task = taskService.findOne(taskId);
        String task2Id = case1.getTasks().get(TEST_TRANSITION2_ID).getTaskStringId();
        Task task2 = taskService.findOne(task2Id);

        String query = String.format("task: transitionId eq '%s'", TEST_TRANSITION_ID);
        String queryMore = String.format("tasks: transitionId eq '%s'", TEST_TRANSITION_ID);

        searchAndCompare(query, task);
        searchAndCompareAsList(queryMore, List.of(task));

        // in list
        String queryInList = String.format("tasks: transitionId in ('%s', '%s')", TEST_TRANSITION_ID, TEST_TRANSITION2_ID);

        searchAndCompareAsList(queryInList, List.of(task, task2));

        // in range
        String queryInRange = String.format("tasks: transitionId in ('%s' : '%s']", TEST_TRANSITION_ID, TEST_TRANSITION2_ID);

        searchAndCompareAsList(queryInRange, List.of(task2));

        // sort
        String querySort = String.format("tasks: caseId eq '%s' sort by transitionId", case1.getStringId());
        String querySort2 = String.format("tasks: caseId eq '%s' sort by transitionId desc", case1.getStringId());

        List<Task> case1Tasks = case1.getTasks().values().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTaskStringId()))
                .collect(Collectors.toList());
        List<Task> asc = case1Tasks.stream()
                .sorted(Comparator.comparing(Task::getTransitionId))
                .collect(Collectors.toList());
        List<Task> desc = case1Tasks.stream()
                .sorted(Comparator.comparing(Task::getTransitionId).reversed())
                .collect(Collectors.toList());

        searchAndCompareAsListInOrder(querySort, asc);
        searchAndCompareAsListInOrder(querySort2, desc);
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
        task3.setTitle(new I18nString("Zzz"));
        taskService.save(task3);

        String query = String.format("task: title eq '%s'", task.getTitle().getDefaultValue());
        String queryMore = String.format("tasks: title eq '%s'", task.getTitle().getDefaultValue());

        searchAndCompare(query, List.of(task, task2));
        searchAndCompareAsList(queryMore, List.of(task, task2));

        // in list
        String queryInList = String.format("tasks: transitionId eq '%s' and title in ('%s', '%s')", TEST_TRANSITION_ID, task.getTitle().getDefaultValue(), task3.getTitle().getDefaultValue());

        searchAndCompareAsList(queryInList, List.of(task, task2, task3));

        // in range
        String queryInRange = String.format("tasks: transitionId eq '%s' and title in ('%s' : '%s']", TEST_TRANSITION_ID, task.getTitle().getDefaultValue(), task3.getTitle().getDefaultValue());

        searchAndCompareAsList(queryInRange, List.of(task3));

        // sort
        String querySort = String.format("tasks: transitionId eq '%s' sort by title", TEST_TRANSITION_ID);
        String querySort2 = String.format("tasks: transitionId eq '%s' sort by title desc", TEST_TRANSITION_ID);

        searchAndCompareAsListInOrder(querySort, List.of(task, task2, task3));
        searchAndCompareAsListInOrder(querySort2, List.of(task3, task, task2));
    }

    @Test
    public void testSearchByState() {
        PetriNet net = importPetriNet("search/search_test.xml");
        IUser user1 = createUser("Name1", "Surname1", "Email1");
        Case case1 = importHelper.createCase("Search Test", net);
        importHelper.assignTask("Test", case1.getStringId(), user1.transformToLoggedUser());
        case1 = importHelper.finishTask("Test", case1.getStringId(), user1.transformToLoggedUser()).getCase();
        List<Task> case1Tasks = case1.getTasks().values().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTaskStringId()))
                .collect(Collectors.toList());
        List<Task> disabled = case1Tasks.stream()
                .filter(task -> task.getState().equals(State.DISABLED))
                .sorted(Comparator.comparing(Task::getStringId))
                .collect(Collectors.toList());
        List<Task> enabled = case1Tasks.stream()
                .filter(task -> task.getState().equals(State.ENABLED))
                .sorted(Comparator.comparing(Task::getStringId))
                .collect(Collectors.toList());

        String query = String.format("task: processId eq '%s' and state eq %s", net.getStringId(), "disabled");
        String queryOther = String.format("tasks: processId eq '%s' and state eq %s", net.getStringId(), "enabled");
        String queryMore = String.format("tasks: processId eq '%s' and state eq %s", net.getStringId(), "disabled");

        searchAndCompare(query, disabled);
        searchAndCompareAsList(queryOther, enabled);
        searchAndCompareAsList(queryMore, disabled);

        // sort
        String querySort = String.format("tasks: processId eq '%s' sort by state", net.getStringId());
        String querySort2 = String.format("tasks: processId eq '%s' sort by state desc", net.getStringId());

        List<Task> asc = new ArrayList<>();
        asc.addAll(disabled);
        asc.addAll(enabled);
        List<Task> desc = new ArrayList<>();
        desc.addAll(enabled);
        desc.addAll(disabled);
        searchAndCompareAsListInOrder(querySort, asc);
        searchAndCompareAsListInOrder(querySort2, desc);
    }

    @Test
    public void testSearchByUserId() {
        PetriNet net = importPetriNet("search/search_test.xml");
        IUser user1 = createUser("Name1", "Surname1", "Email1");
        IUser user2 = createUser("Name2", "Surname2", "Email2");
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

        searchAndCompare(query, List.of(task, task2));
        searchAndCompare(queryOther, List.of(task3));
        searchAndCompareAsList(queryMore, List.of(task, task2));

        // in list
        String queryInList = String.format("tasks: userId in ('%s', '%s')", user1.getStringId(), user2.getStringId());

        searchAndCompareAsList(queryInList, List.of(task, task2, task3));

        // sort
        String querySort = String.format("tasks: transitionId eq '%s' sort by userId", TEST_TRANSITION_ID);
        String querySort2 = String.format("tasks: transitionId eq '%s' sort by userId desc", TEST_TRANSITION_ID);

        searchAndCompareAsListInOrder(querySort, List.of(task, task2, task3));
        searchAndCompareAsListInOrder(querySort2, List.of(task3, task, task2));
    }

    @Test
    public void testSearchByCaseId() {
        PetriNet net = importPetriNet("search/search_test.xml");
        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test", net);
        List<Task> case1Tasks = case1.getTasks().values().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTaskStringId()))
                .sorted(Comparator.comparing(Task::getStringId))
                .collect(Collectors.toList());
        List<Task> case2Tasks = case2.getTasks().values().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTaskStringId()))
                .sorted(Comparator.comparing(Task::getStringId))
                .collect(Collectors.toList());

        String query = String.format("task: caseId eq '%s'", case1.getStringId());
        String queryOther = String.format("task: caseId eq '%s'", case2.getStringId());
        String queryMore = String.format("tasks: caseId eq '%s'", case1.getStringId());

        searchAndCompare(query, case1Tasks);
        searchAndCompare(queryOther, case2Tasks);
        searchAndCompareAsList(queryMore, case1Tasks);

        // in list
        String queryInList = String.format("tasks: caseId in ('%s', '%s')", case1.getStringId(), case2.getStringId());

        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(case1Tasks);
        allTasks.addAll(case2Tasks);
        searchAndCompareAsList(queryInList, allTasks);

        // sort
        String querySort = String.format("tasks: processId eq '%s' sort by caseId", net.getStringId());
        String querySort2 = String.format("tasks: processId eq '%s' sort by caseId desc", net.getStringId());

        List<Task> asc = new ArrayList<>();
        asc.addAll(case1Tasks);
        asc.addAll(case2Tasks);
        List<Task> desc = new ArrayList<>();
        desc.addAll(case2Tasks);
        desc.addAll(case1Tasks);

        searchAndCompareAsListInOrder(querySort, asc);
        searchAndCompareAsListInOrder(querySort2, desc);
    }

    @Test
    public void testSearchByProcessId() {
        PetriNet net = importPetriNet("search/search_test.xml");
        PetriNet net2 = importPetriNet("search/search_test2.xml");
        Case case1 = importHelper.createCase("Search Test", net);
        Case case2 = importHelper.createCase("Search Test2", net2);
        List<Task> netTasks = case1.getTasks().values().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTaskStringId()))
                .sorted(Comparator.comparing(Task::getStringId))
                .collect(Collectors.toList());
        List<Task> net2Tasks = case2.getTasks().values().stream()
                .map(taskPair -> taskService.findOne(taskPair.getTaskStringId()))
                .sorted(Comparator.comparing(Task::getStringId))
                .collect(Collectors.toList());

        String query = String.format("task: processId eq '%s'", net.getStringId());
        String queryOther = String.format("task: processId eq '%s'", net2.getStringId());
        String queryMore = String.format("tasks: processId eq '%s'", net.getStringId());

        searchAndCompare(query, netTasks);
        searchAndCompare(queryOther, net2Tasks);
        searchAndCompareAsList(queryMore, netTasks);

        // in list
        String queryInList = String.format("tasks: processId in ('%s', '%s')", net.getStringId(), net2.getStringId());

        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(netTasks);
        allTasks.addAll(net2Tasks);
        searchAndCompareAsList(queryInList, allTasks);

        // sort
        String querySort = String.format("tasks: processId in ('%s', '%s') sort by processId", net.getStringId(), net2.getStringId());
        String querySort2 = String.format("tasks: processId in ('%s', '%s') sort by processId desc", net.getStringId(), net2.getStringId());

        List<Task> asc = new ArrayList<>();
        asc.addAll(netTasks);
        asc.addAll(net2Tasks);
        List<Task> desc = new ArrayList<>();
        desc.addAll(net2Tasks);
        desc.addAll(netTasks);

        searchAndCompareAsListInOrder(querySort, asc);
        searchAndCompareAsListInOrder(querySort2, desc);
    }

    @Test
    public void testSearchByLastAssign() {
        PetriNet net = importPetriNet("search/search_test.xml");
        IUser user1 = createUser("Name1", "Surname1", "Email1");
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

        searchAndCompare(query, task);
        searchAndCompare(queryBefore, List.of(task, task2));
        searchAndCompareAsList(queryMore, List.of(task, task2));

        // in list
        String queryInList = String.format("tasks: transitionId eq '%s' and lastAssign in (%s, %s)", TEST_TRANSITION_ID, toDateTimeString(task.getLastAssigned()), toDateTimeString(task2.getLastAssigned()));
        searchAndCompareAsList(queryInList, List.of(task, task2));

        // in range
        String queryInRange = String.format("tasks: transitionId eq '%s' and lastAssign in [%s : %s)", TEST_TRANSITION_ID, toDateTimeString(task.getLastAssigned()), toDateTimeString(task2.getLastAssigned()));
        searchAndCompareAsList(queryInRange, List.of(task));

        // sort
        String querySort = String.format("tasks: transitionId eq '%s' sort by lastAssign", TEST_TRANSITION_ID);
        String querySort2 = String.format("tasks: transitionId eq '%s' sort by lastAssign desc", TEST_TRANSITION_ID);

        searchAndCompareAsListInOrder(querySort, List.of(task, task2));
        searchAndCompareAsListInOrder(querySort2, List.of(task2, task));
    }

    @Test
    public void testSearchByLastFinish() {
        PetriNet net = importPetriNet("search/search_test.xml");
        IUser user1 = createUser("Name1", "Surname1", "Email1");
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
        String queryBefore = String.format("task: lastFinish gt %s", SearchUtils.toDateTimeString(before));
        String queryMore = String.format("tasks: lastFinish gt %s", SearchUtils.toDateTimeString(before));

        searchAndCompare(query, task);
        searchAndCompare(queryBefore, List.of(task, task2));
        searchAndCompareAsList(queryMore, List.of(task, task2));

        // in list
        String queryInList = String.format("tasks: transitionId eq '%s' and lastFinish in (%s, %s)", TEST_TRANSITION_ID, toDateTimeString(task.getLastFinished()), toDateTimeString(task2.getLastFinished()));
        searchAndCompareAsList(queryInList, List.of(task, task2));

        // in range
        String queryInRange = String.format("tasks: transitionId eq '%s' and lastFinish in [%s : %s)", TEST_TRANSITION_ID, toDateTimeString(task.getLastFinished()), toDateTimeString(task2.getLastFinished()));
        searchAndCompareAsList(queryInRange, List.of(task));

        // sort
        String querySort = String.format("tasks: transitionId eq '%s' sort by lastFinish", TEST_TRANSITION_ID);
        String querySort2 = String.format("tasks: transitionId eq '%s' sort by lastFinish desc", TEST_TRANSITION_ID);

        searchAndCompareAsListInOrder(querySort, List.of(task, task2));
        searchAndCompareAsListInOrder(querySort2, List.of(task2, task));
    }

    @Test
    public void testPagination() {
        PetriNet net = importPetriNet("search/search_test.xml");
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Case c = importHelper.createCase("Search Test", net);
            tasks.addAll(c.getTasks().values().stream()
                    .map(taskPair -> taskService.findOne(taskPair.getTaskStringId()))
                            .sorted(Comparator.comparing(Task::getStringId))
                    .collect(Collectors.toList()));
        }

        String queryOne = String.format("task: processId eq '%s'", net.getStringId());
        String queryMore = String.format("tasks: processId eq '%s'", net.getStringId());
        String queryMoreCustomPagination = String.format("tasks: processId eq '%s' page 1 size 5", net.getStringId());

        long count = searchService.count(queryOne);
        assert count == 30;

        Object actual = searchService.search(queryOne);
        compareById(convertToObject(actual, Task.class), tasks.get(0), Task::getStringId);

        actual = searchService.search(queryMore);
        compareById(convertToObjectList(actual, Task.class), tasks.subList(0, 19), Task::getStringId);

        actual = searchService.search(queryMoreCustomPagination);
        compareById(convertToObjectList(actual, Task.class), tasks.subList(5, 9), Task::getStringId);
    }
}
