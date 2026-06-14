package com.netgrif.application.engine.pfql;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.taskresource.TaskSearchService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class TaskSearchServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private TaskSearchService taskSearchService;

    @Autowired
    private ImportHelper importHelper;

    private String testTaskId;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
        Optional<PetriNet> createdNetOpt = importHelper.createNet("/query_lang_test.xml");
        assertTrue(createdNetOpt.isPresent());
        Case testCase = importHelper.createCase("test", createdNetOpt.get());
        testTaskId = testCase.getTasks().stream()
                .filter(taskPair -> taskPair.getTransition().equals("t1"))
                .map(TaskPair::getTask)
                .findFirst().get();
    }

    @Test
    public void queryResourceTypeTest() {
        assertEquals(QueryType.TASK, taskSearchService.getQueryResourceType());
    }

    @Test
    public void searchOneTest() {
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.searchOne((String) null));
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.searchOne("tasks: title eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.searchOne("process: identifier eq 'query_lang_test'"));

        Task result = taskSearchService.searchOne("task: id eq '" + testTaskId + "'");
        assertNotNull(result);
        assertEquals(testTaskId, result.getStringId());

        result = taskSearchService.searchOne("task: id eq '" + new ObjectId() + "'");
        assertNull(result);
    }

    @Test
    public void searchAllTest() {
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.searchAll((String) null));
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.searchAll("task: title eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.searchAll("processes: identifier eq 'query_lang_test'"));

        Page<Task> result = taskSearchService.searchAll("tasks: id eq '" + testTaskId + "'");
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testTaskId, result.getContent().get(0).getStringId());

        result = taskSearchService.searchAll("tasks: id eq '" + new ObjectId() + "'");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void countTest() {
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.count((String) null));
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.count("process: identifier eq 'query_lang_test'"));

        long result = taskSearchService.count("task: id eq '" + testTaskId + "'");
        assertEquals(1, result);

        result = taskSearchService.count("tasks: id eq '" + testTaskId + "'");
        assertEquals(1, result);

        result = taskSearchService.count("tasks: id eq '" + new ObjectId() + "'");
        assertEquals(0, result);
    }

    @Test
    public void existsTest() {
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.exists((String) null));
        assertThrows(IllegalArgumentException.class, () -> taskSearchService.exists("process: identifier eq 'query_lang_test'"));

        boolean result = taskSearchService.exists("task: id eq '" + testTaskId + "'");
        assertTrue(result);

        result = taskSearchService.exists("tasks: id eq '" + testTaskId + "'");
        assertTrue(result);

        result = taskSearchService.exists("tasks: id eq '" + new ObjectId() + "'");
        assertFalse(result);
    }

}
