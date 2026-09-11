
package com.netgrif.application.engine.pfql.formatters;

import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.TaskField;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.pfql.service.formatters.QueryLangPlaceholderHandler;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.evaluateQuery;
import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.formatPlaceholders;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class QueryLangPlaceholderHandlerTest {

    @Autowired
    private QueryLangPlaceholderHandler placeholderHandler;

    // =========================================================================
    // CASE queries
    // =========================================================================

    @Test
    public void testCase_String() {
        String query = formatPlaceholders("case: title eq {}", placeholderHandler, "my-title");
        assertEquals("case: title eq 'my-title'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_StringList() {
        String query = formatPlaceholders("case: title in {}", placeholderHandler,
                List.of("title-a", "title-b", "title-c"));
        assertEquals("case: title in ('title-a', 'title-b', 'title-c')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_Boolean() {
        String query = formatPlaceholders("case: data.active.value eq {}", placeholderHandler, true);
        assertEquals("case: data.active.value eq true", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_Number() {
        String query = formatPlaceholders("case: data.count.value eq {}", placeholderHandler, 42);
        assertEquals("case: data.count.value eq 42", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_NumberDouble() {
        String query = formatPlaceholders("case: data.price.value gt {}", placeholderHandler, 3.14);
        assertEquals("case: data.price.value gt 3.14", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_NumberList() {
        String query = formatPlaceholders("case: data.score.value in {}", placeholderHandler,
                List.of(1, 2, 3));
        assertEquals("case: data.score.value in (1, 2, 3)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_ObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439011");
        String query = formatPlaceholders("case: id eq {}", placeholderHandler, id);
        assertEquals("case: id eq '507f1f77bcf86cd799439011'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_ObjectIdList() {
        ObjectId id1 = new ObjectId("507f1f77bcf86cd799439011");
        ObjectId id2 = new ObjectId("507f1f77bcf86cd799439012");
        String query = formatPlaceholders("case: id in {}", placeholderHandler, List.of(id1, id2));
        assertEquals("case: id in ('507f1f77bcf86cd799439011', '507f1f77bcf86cd799439012')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_DateTime_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 15, 10, 30, 0, 500_000_000);
        String query = formatPlaceholders("case: creationDate gt {}", placeholderHandler, dt);
        assertEquals("case: creationDate gt 2024-03-15T10:30:00.5", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_DateTime_String() {
        String query = formatPlaceholders("case: creationDate lte {}", placeholderHandler, "2024-03-15T10:30:00");
        assertEquals("case: creationDate lte 2024-03-15T10:30:00", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_DateTimeList() {
        LocalDateTime dt1 = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime dt2 = LocalDateTime.of(2024, 6, 1, 12, 0, 0);
        Date dt3 = new GregorianCalendar(2024, Calendar.DECEMBER, 1, 12, 0).getTime();
        String query = formatPlaceholders("case: creationDate in {}", placeholderHandler, List.of(dt1, dt2, dt3));
        assertEquals("case: creationDate in (2024-01-01T00:00:00.0, 2024-06-01T12:00:00.0, 2024-12-01T12:00:00.0)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_Date_LocalDate() {
        LocalDate date = LocalDate.of(2024, 5, 20);
        String query = formatPlaceholders("case: creationDate gte {}", placeholderHandler, date);
        assertEquals("case: creationDate gte 2024-05-20", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_Date_String() {
        String query = formatPlaceholders("case: creationDate lt {}", placeholderHandler, "2024-05-20");
        assertEquals("case: creationDate lt 2024-05-20", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_DateList() {
        LocalDate d1 = LocalDate.of(2024, 1, 10);
        LocalDate d2 = LocalDate.of(2024, 2, 20);
        LocalDate d3 = LocalDate.of(2024, 3, 30);
        String query = formatPlaceholders("case: creationDate in {}", placeholderHandler, List.of(d1, d2, d3));
        assertEquals("case: creationDate in (2024-01-10, 2024-02-20, 2024-03-30)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_CaseRef() {
        CaseField caseField = new CaseField();
        caseField.setValue(List.of("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012"));
        String query = formatPlaceholders("case: id in {}", placeholderHandler, caseField);
        assertEquals("case: id in ('507f1f77bcf86cd799439011', '507f1f77bcf86cd799439012')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));

        EnumerationMapField caseOptionField = new EnumerationMapField();
        caseOptionField.setComponent(new Component("caseref"));
        caseOptionField.setOptions(Map.of("507f1f77bcf86cd799439011", new I18nString(), "507f1f77bcf86cd799439012", new I18nString()));
        String query2 = formatPlaceholders("case: id in {}", placeholderHandler, caseOptionField);
        assertEquals("case: id in ('507f1f77bcf86cd799439011', '507f1f77bcf86cd799439012')", query2);
        assertDoesNotThrow(() -> evaluateQuery(query2));
    }

    @Test
    public void testCasePlaceholder_MultiplePlaceholders() {
        String query = formatPlaceholders(
                "case: processIdentifier eq {} and title eq {} and data.count.value gt {}",
                placeholderHandler,
                "my-process", "My Case", 5
        );
        assertEquals("case: processIdentifier eq 'my-process' and title eq 'My Case' and data.count.value gt 5", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_MultiplePlaceholders_ObjectIdAndDateAndStringList() {
        ObjectId oid = new ObjectId("507f1f77bcf86cd799439011");
        LocalDate date = LocalDate.of(2023, 6, 1);
        String query = formatPlaceholders(
                "case: id eq {} and creationDate gte {} and title in {}",
                placeholderHandler,
                oid, date, List.of("Alpha", "Beta")
        );
        assertEquals(
                "case: id eq '507f1f77bcf86cd799439011' and creationDate gte 2023-06-01 and title in ('Alpha', 'Beta')",
                query
        );
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testCase_MultiplePlaceholders_DateTimeRange() {
        LocalDateTime from = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        String query = formatPlaceholders(
                "case: creationDate in ({} : {})",
                placeholderHandler,
                from, to
        );
        assertEquals("case: creationDate in (2023-01-01T00:00:00.0 : 2024-01-01T00:00:00.0)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    // =========================================================================
    // TASK queries
    // =========================================================================

    @Test
    public void testTask_String() {
        String query = formatPlaceholders("task: transitionId eq {}", placeholderHandler, "t1");
        assertEquals("task: transitionId eq 't1'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_StringList() {
        String query = formatPlaceholders("task: transitionId in {}", placeholderHandler,
                List.of("t1", "t2", "t3"));
        assertEquals("task: transitionId in ('t1', 't2', 't3')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_ObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439022");
        String query = formatPlaceholders("task: id eq {}", placeholderHandler, id);
        assertEquals("task: id eq '507f1f77bcf86cd799439022'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_ObjectIdList() {
        ObjectId id1 = new ObjectId("507f1f77bcf86cd799439022");
        ObjectId id2 = new ObjectId("507f1f77bcf86cd799439033");
        String query = formatPlaceholders("task: id in {}", placeholderHandler, List.of(id1, id2));
        assertEquals("task: id in ('507f1f77bcf86cd799439022', '507f1f77bcf86cd799439033')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_DateTime_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 10, 8, 0, 0);
        String query = formatPlaceholders("task: lastAssign gt {}", placeholderHandler, dt);
        assertEquals("task: lastAssign gt 2024-06-10T08:00:00.0", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_DateTime_String() {
        String query = formatPlaceholders("task: lastFinish lte {}", placeholderHandler, "2024-11-30T23:59:59");
        assertEquals("task: lastFinish lte 2024-11-30T23:59:59", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_DateTimeList() {
        LocalDateTime dt1 = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        LocalDateTime dt2 = LocalDateTime.of(2024, 12, 31, 12, 0, 0);
        String query = formatPlaceholders("task: lastAssign in {}", placeholderHandler, List.of(dt1, dt2));
        assertEquals("task: lastAssign in (2024-01-01T12:00:00.0, 2024-12-31T12:00:00.0)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_Date_LocalDate() {
        LocalDate date = LocalDate.of(2024, 9, 1);
        String query = formatPlaceholders("task: lastAssign gte {}", placeholderHandler, date);
        assertEquals("task: lastAssign gte 2024-09-01", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_Date_String() {
        String query = formatPlaceholders("task: lastFinish lt {}", placeholderHandler, "2024-12-31");
        assertEquals("task: lastFinish lt 2024-12-31", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_DateList() {
        LocalDate d1 = LocalDate.of(2024, 3, 1);
        LocalDate d2 = LocalDate.of(2024, 9, 1);
        String query = formatPlaceholders("task: lastFinish in {}", placeholderHandler, List.of(d1, d2));
        assertEquals("task: lastFinish in (2024-03-01, 2024-09-01)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_TaskRef() {
        TaskField taskField = new TaskField();
        taskField.setValue(List.of("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012", "507f1f77bcf86cd799439013"));
        String query = formatPlaceholders("task: id in {}", placeholderHandler, taskField);
        assertEquals("task: id in ('507f1f77bcf86cd799439011', '507f1f77bcf86cd799439012', '507f1f77bcf86cd799439013')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_MultiplePlaceholders_StringAndObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439022");
        String query = formatPlaceholders(
                "task: processId eq {} and id eq {}",
                placeholderHandler,
                "507f1f77bcf86cd799439011", id
        );
        assertEquals("task: processId eq '507f1f77bcf86cd799439011' and id eq '507f1f77bcf86cd799439022'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_MultiplePlaceholders_ThreeStrings() {
        String query = formatPlaceholders(
                "task: processId eq {} and userId eq {} and transitionId eq {}",
                placeholderHandler,
                "507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012", "t1"
        );
        assertEquals("task: processId eq '507f1f77bcf86cd799439011' and userId eq '507f1f77bcf86cd799439012' and transitionId eq 't1'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testTask_MultiplePlaceholders_DateTimeRange() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        String query = formatPlaceholders(
                "task: transitionId eq {} and lastAssign in ({} : {})",
                placeholderHandler,
                "transition-1", from, to
        );
        assertEquals(
                "task: transitionId eq 'transition-1' and lastAssign in (2024-01-01T00:00:00.0 : 2024-12-31T23:59:59.0)",
                query
        );
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    // =========================================================================
    // PROCESS queries
    // =========================================================================

    @Test
    public void testProcess_String() {
        String query = formatPlaceholders("process: identifier eq {}", placeholderHandler, "my-process");
        assertEquals("process: identifier eq 'my-process'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_StringList() {
        String query = formatPlaceholders("process: identifier in {}", placeholderHandler,
                List.of("proc-a", "proc-b"));
        assertEquals("process: identifier in ('proc-a', 'proc-b')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_Number() {
        String query = formatPlaceholders("process: version eq {}.{}.{}", placeholderHandler, 1, 2, 3);
        assertEquals("process: version eq 1.2.3", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_NumberList() {
        String query = formatPlaceholders("process: version in ({}.{}.{}, {}.{}.{})", placeholderHandler,
                1, 0, 0, 2, 0, 0);
        assertEquals("process: version in (1.0.0, 2.0.0)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_Version() {
        String query = formatPlaceholders("process: version eq {}", placeholderHandler, new Version(1, 2, 3));
        assertEquals("process: version eq 1.2.3", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_VersionList() {
        String query = formatPlaceholders("process: version in {}", placeholderHandler,
                List.of(new Version(1, 0, 0), new Version(2, 0, 0)));
        assertEquals("process: version in (1.0.0, 2.0.0)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_ObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439055");
        String query = formatPlaceholders("process: id eq {}", placeholderHandler, id);
        assertEquals("process: id eq '507f1f77bcf86cd799439055'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_ObjectIdList() {
        ObjectId id1 = new ObjectId("507f1f77bcf86cd799439055");
        ObjectId id2 = new ObjectId("507f1f77bcf86cd799439066");
        String query = formatPlaceholders("process: id in {}", placeholderHandler, List.of(id1, id2));
        assertEquals("process: id in ('507f1f77bcf86cd799439055', '507f1f77bcf86cd799439066')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_DateTime_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2023, 11, 1, 9, 0, 0);
        String query = formatPlaceholders("process: creationDate eq {}", placeholderHandler, dt);
        assertEquals("process: creationDate eq 2023-11-01T09:00:00.0", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_DateTime_String() {
        String query = formatPlaceholders("process: creationDate gte {}", placeholderHandler, "2023-01-01T00:00:00");
        assertEquals("process: creationDate gte 2023-01-01T00:00:00", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_DateTimeList() {
        LocalDateTime dt1 = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        LocalDateTime dt2 = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        String query = formatPlaceholders("process: creationDate in {}", placeholderHandler, List.of(dt1, dt2));
        assertEquals("process: creationDate in (2022-01-01T00:00:00.0, 2023-01-01T00:00:00.0)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_Date_LocalDate() {
        LocalDate date = LocalDate.of(2024, 12, 1);
        String query = formatPlaceholders("process: creationDate lte {}", placeholderHandler, date);
        assertEquals("process: creationDate lte 2024-12-01", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_Date_String() {
        String query = formatPlaceholders("process: creationDate lt {}", placeholderHandler, "2024-12-31");
        assertEquals("process: creationDate lt 2024-12-31", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_DateList() {
        LocalDate d1 = LocalDate.of(2023, 3, 1);
        LocalDate d2 = LocalDate.of(2023, 6, 1);
        String query = formatPlaceholders("process: creationDate in {}", placeholderHandler, List.of(d1, d2));
        assertEquals("process: creationDate in (2023-03-01, 2023-06-01)", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_MultiplePlaceholders_TwoStrings() {
        String query = formatPlaceholders(
                "process: identifier eq {} and title eq {}",
                placeholderHandler,
                "my-process", "My Process Title"
        );
        assertEquals("process: identifier eq 'my-process' and title eq 'My Process Title'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_MultiplePlaceholders_ObjectIdAndDateRange() {
        ObjectId oid = new ObjectId("507f1f77bcf86cd799439099");
        LocalDateTime from = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        String query = formatPlaceholders(
                "process: id eq {} and creationDate in ({} : {})",
                placeholderHandler,
                oid, from, to
        );
        assertEquals(
                "process: id eq '507f1f77bcf86cd799439099' and creationDate in (2023-01-01T00:00:00.0 : 2024-01-01T00:00:00.0)",
                query
        );
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testProcess_MultiplePlaceholders_StringListAndDateList() {
        LocalDate d1 = LocalDate.of(2023, 1, 1);
        LocalDate d2 = LocalDate.of(2024, 1, 1);
        String query = formatPlaceholders(
                "process: identifier in {} and creationDate in {}",
                placeholderHandler,
                List.of("proc-a", "proc-b"), List.of(d1, d2)
        );
        assertEquals(
                "process: identifier in ('proc-a', 'proc-b') and creationDate in (2023-01-01, 2024-01-01)",
                query
        );
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    // =========================================================================
    // USER queries
    // =========================================================================

    @Test
    public void testUser_String() {
        String query = formatPlaceholders("user: email eq {}", placeholderHandler, "user@example.com");
        assertEquals("user: email eq 'user@example.com'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testUser_StringContains() {
        String query = formatPlaceholders("user: name contains {}", placeholderHandler, "John");
        assertEquals("user: name contains 'John'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testUser_StringList() {
        String query = formatPlaceholders("user: email in {}", placeholderHandler,
                List.of("a@example.com", "b@example.com", "c@example.com"));
        assertEquals("user: email in ('a@example.com', 'b@example.com', 'c@example.com')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testUser_ObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439077");
        String query = formatPlaceholders("user: id eq {}", placeholderHandler, id);
        assertEquals("user: id eq '507f1f77bcf86cd799439077'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testUser_ObjectIdList() {
        ObjectId id1 = new ObjectId("507f1f77bcf86cd799439077");
        ObjectId id2 = new ObjectId("507f1f77bcf86cd799439088");
        String query = formatPlaceholders("user: id in {}", placeholderHandler, List.of(id1, id2));
        assertEquals("user: id in ('507f1f77bcf86cd799439077', '507f1f77bcf86cd799439088')", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testUser_MultiplePlaceholders_ThreeStrings() {
        String query = formatPlaceholders(
                "user: name eq {} and surname eq {} and email eq {}",
                placeholderHandler,
                "John", "Doe", "john.doe@example.com"
        );
        assertEquals("user: name eq 'John' and surname eq 'Doe' and email eq 'john.doe@example.com'", query);
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    @Test
    public void testUser_MultiplePlaceholders_ObjectIdAndStringList() {
        ObjectId oid = new ObjectId("507f1f77bcf86cd799439077");
        String query = formatPlaceholders(
                "user: id eq {} and email in {}",
                placeholderHandler,
                oid, List.of("alice@example.com", "bob@example.com")
        );
        assertEquals(
                "user: id eq '507f1f77bcf86cd799439077' and email in ('alice@example.com', 'bob@example.com')",
                query
        );
        assertDoesNotThrow(() -> evaluateQuery(query));
    }

    // =========================================================================
    // Edge cases
    // =========================================================================

    @Test
    public void testUnsupportedType_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> placeholderHandler.format(new Object()));
    }

    @Test
    public void testNoPlaceholders_QueryUnchanged() {
        String original = "case: title eq 'fixed-title'";
        String result = formatPlaceholders(original, placeholderHandler);
        assertEquals(original, result);
        assertDoesNotThrow(() -> evaluateQuery(result));
    }
}