package com.netgrif.application.engine.search;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.netgrif.application.engine.search.SearchUtils.evaluateQuery;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class QueryLangTest {

    // todo NAE-1997:: simple queries logical predicate comparison
    // todo NAE-1997:: complex queries logical predicate comparison

    // todo NAE-1997:: all attributes success
    // todo NAE-1997:: all comparison type fail

    @Test
    public void testProcessQueriesFail() {
        // using case, task, user attributes
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: processId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: processIdentifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: author eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: places.p1.marking eq 1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: tasks.t1.state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: tasks.t1.userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: data.field1.value eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: data.field1.options contains 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: transitionId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: caseId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: lastAssign eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: lastFinish eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: name eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: surname eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("process: email eq 'test'"));
    }

    @Test
    public void testCaseQueriesFail() {
        // using process, task, user attributes
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: identifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: version eq 1.1.1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: transitionId eq 1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: caseId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: lastAssign eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: lastFinish eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: name eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: surname eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: email eq 'test'"));
    }

    @Test
    public void testTaskQueriesFail() {
        // using process, case, user attributes
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: identifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: version eq 1.1.1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: processIdentifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: places.p1.marking eq 1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: tasks.t1.state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: tasks.t1.userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: data.field1.value eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: data.field1.options contains 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: name eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: surname eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("task: email eq 'test'"));
    }

    @Test
    public void testUserQueriesFail() {
        // using process, case, task attributes
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: identifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("case: version eq 1.1.1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: processId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: processIdentifier eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: author eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: places.p1.marking eq 1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: tasks.t1.state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: tasks.t1.userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: data.field1.value eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: data.field1.options contains 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: transitionId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: state eq enabled"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: userId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: caseId eq 'test'"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: lastAssign eq 2020-03-03"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> evaluateQuery("user: lastFinish eq 2020-03-03"));
    }
}
