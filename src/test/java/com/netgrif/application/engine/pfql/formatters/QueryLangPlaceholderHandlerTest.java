
package com.netgrif.application.engine.pfql.formatters;

import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TaskField;
import com.netgrif.application.engine.pfql.service.formatters.QueryLangPlaceholderHandler;
import com.netgrif.application.engine.pfql.service.utils.SearchUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

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
    public void testCasePlaceholder_String() {
        String query = SearchUtils.formatPlaceholders("case: title eq {}", placeholderHandler, "my-title");
        assertEquals("case: title eq 'my-title'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_StringList() {
        String query = SearchUtils.formatPlaceholders("case: title in {}", placeholderHandler,
                List.of("title-a", "title-b", "title-c"));
        assertEquals("case: title in ('title-a', 'title-b', 'title-c')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_Boolean() {
        String query = SearchUtils.formatPlaceholders("case: data.active.value eq {}", placeholderHandler, true);
        assertEquals("case: data.active.value eq true", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_Number() {
        String query = SearchUtils.formatPlaceholders("case: data.count.value eq {}", placeholderHandler, 42);
        assertEquals("case: data.count.value eq 42", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_NumberDouble() {
        String query = SearchUtils.formatPlaceholders("case: data.price.value gt {}", placeholderHandler, 3.14);
        assertEquals("case: data.price.value gt 3.14", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_NumberList() {
        String query = SearchUtils.formatPlaceholders("case: data.score.value in {}", placeholderHandler,
                List.of(1, 2, 3));
        assertEquals("case: data.score.value in (1, 2, 3)", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_ObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439011");
        String query = SearchUtils.formatPlaceholders("case: id eq {}", placeholderHandler, id);
        assertEquals("case: id eq '507f1f77bcf86cd799439011'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_ObjectIdList() {
        ObjectId id1 = new ObjectId("507f1f77bcf86cd799439011");
        ObjectId id2 = new ObjectId("507f1f77bcf86cd799439012");
        String query = SearchUtils.formatPlaceholders("case: id in {}", placeholderHandler, List.of(id1, id2));
        assertEquals("case: id in ('507f1f77bcf86cd799439011', '507f1f77bcf86cd799439012')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_DateTime_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 15, 10, 30, 0, 5);
        String query = SearchUtils.formatPlaceholders("case: creationDate gt {}", placeholderHandler, dt);
        assertEquals("case: creationDate gt 2024-03-15T10:30:00.5", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_DateTime_String() {
        String query = SearchUtils.formatPlaceholders("case: creationDate lte {}", placeholderHandler, "2024-03-15T10:30:00");
        assertEquals("case: creationDate lte 2024-03-15T10:30:00", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_DateTimeList() {
        LocalDateTime dt1 = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime dt2 = LocalDateTime.of(2024, 6, 1, 0, 0, 0);
        Date dt3 = new Date(dt2.plusDays(1).toEpochSecond(ZoneOffset.UTC));
        String query = SearchUtils.formatPlaceholders("case: creationDate in {}", placeholderHandler, List.of(dt1, dt2, dt3));
        assertEquals("case: creationDate in (2024-01-01T00:00:00.0, 2024-06-01T00:00:00.0, 2024-06-02T00:00:00.0)", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_Date_LocalDate() {
        LocalDate date = LocalDate.of(2024, 5, 20);
        String query = SearchUtils.formatPlaceholders("case: creationDate gte {}", placeholderHandler, date);
        assertEquals("case: creationDate gte 2024-05-20", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_Date_String() {
        String query = SearchUtils.formatPlaceholders("case: creationDate lt {}", placeholderHandler, "2024-05-20");
        assertEquals("case: creationDate lt 2024-05-20", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_DateList() {
        LocalDate d1 = LocalDate.of(2024, 1, 10);
        LocalDate d2 = LocalDate.of(2024, 2, 20);
        LocalDate d3 = LocalDate.of(2024, 3, 30);
        String query = SearchUtils.formatPlaceholders("case: creationDate in {}", placeholderHandler, List.of(d1, d2, d3));
        assertEquals("case: creationDate in (2024-01-10, 2024-02-20, 2024-03-30)", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_CaseRef_WithValues() {
        CaseField caseField = new CaseField();
        caseField.setValue(List.of("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012"));
        String query = SearchUtils.formatPlaceholders("case: id in {}", placeholderHandler, caseField);
        assertEquals("case: id in ('507f1f77bcf86cd799439011', '507f1f77bcf86cd799439012')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_CaseRef_Null() {
        CaseField caseField = new CaseField();
        caseField.setValue(null);
        String formatted = placeholderHandler.format(caseField);
        assertEquals("", formatted);
    }

    @Test
    public void testCasePlaceholder_MultiplePlaceholders() {
        String query = SearchUtils.formatPlaceholders(
                "case: processIdentifier eq {} and title eq {} and data.count.value gt {}",
                placeholderHandler,
                "my-process", "My Case", 5
        );
        assertEquals("case: processIdentifier eq 'my-process' and title eq 'My Case' and data.count.value gt 5", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testCasePlaceholder_MultiplePlaceholders_MixedTypes() {
        ObjectId oid = new ObjectId("507f1f77bcf86cd799439011");
        LocalDate date = LocalDate.of(2023, 6, 1);
        String query = SearchUtils.formatPlaceholders(
                "case: id eq {} and creationDate gte {} and title in {}",
                placeholderHandler,
                oid, date, List.of("Alpha", "Beta")
        );
        assertEquals(
                "case: id eq '507f1f77bcf86cd799439011' and creationDate gte 2023-06-01 and title in ('Alpha', 'Beta')",
                query
        );
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    // =========================================================================
    // TASK queries
    // =========================================================================

    @Test
    public void testTaskPlaceholder_String() {
        String query = SearchUtils.formatPlaceholders("task: transitionId eq {}", placeholderHandler, "t1");
        assertEquals("task: transitionId eq 't1'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testTaskPlaceholder_StringList() {
        String query = SearchUtils.formatPlaceholders("task: transitionId in {}", placeholderHandler,
                List.of("t1", "t2", "t3"));
        assertEquals("task: transitionId in ('t1', 't2', 't3')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testTaskPlaceholder_Boolean() {
        // Boolean formats to 'true'/'false' which can appear in caseId eq context
        String formatted = placeholderHandler.format(false);
        assertEquals("false", formatted);
    }

    @Test
    public void testTaskPlaceholder_Number() {
        String query = SearchUtils.formatPlaceholders("task: caseId eq {}", placeholderHandler, 100);
        assertEquals("task: caseId eq 100", query);
        // caseId is a string field so this won't parse correctly, but we verify formatting
        String formatted = placeholderHandler.format(100);
        assertEquals("100", formatted);
    }

    @Test
    public void testTaskPlaceholder_NumberList() {
        String formatted = placeholderHandler.format(List.of(10L, 20L, 30L));
        assertEquals("(10, 20, 30)", formatted);
    }

    @Test
    public void testTaskPlaceholder_ObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439022");
        String query = SearchUtils.formatPlaceholders("task: id eq {}", placeholderHandler, id);
        assertEquals("task: id eq '507f1f77bcf86cd799439022'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testTaskPlaceholder_ObjectIdList() {
        ObjectId id1 = new ObjectId("507f1f77bcf86cd799439022");
        ObjectId id2 = new ObjectId("507f1f77bcf86cd799439033");
        String query = SearchUtils.formatPlaceholders("task: id in {}", placeholderHandler, List.of(id1, id2));
        assertEquals("task: id in ('507f1f77bcf86cd799439022', '507f1f77bcf86cd799439033')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testTaskPlaceholder_DateTime_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 10, 8, 0, 0);
        String query = SearchUtils.formatPlaceholders("task: lastAssign gt {}", placeholderHandler, dt);
        assertEquals("task: lastAssign gt 2024-06-10T08:00:00.0", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testTaskPlaceholder_DateTime_String() {
        String query = SearchUtils.formatPlaceholders("task: lastFinish lte {}", placeholderHandler, "2024-11-30T23:59:59");
        assertEquals("task: lastFinish lte 2024-11-30T23:59:59", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testTaskPlaceholder_DateTimeList() {
        LocalDateTime dt1 = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        LocalDateTime dt2 = LocalDateTime.of(2024, 12, 31, 12, 0, 0);
        String query = SearchUtils.formatPlaceholders("task: lastAssign in {}", placeholderHandler, List.of(dt1, dt2));
        assertEquals("task: lastAssign in (2024-01-01T12:00:00.0, 2024-12-31T12:00:00.0)", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testTaskPlaceholder_TaskRef_WithValues() {
        TaskField taskField = new TaskField();
        taskField.setValue(List.of("task-id-1", "task-id-2", "task-id-3"));
        String formatted = placeholderHandler.format(taskField);
        assertEquals("('task-id-1', 'task-id-2', 'task-id-3')", formatted);
    }

    @Test
    public void testTaskPlaceholder_TaskRef_Null() {
        TaskField taskField = new TaskField();
        taskField.setValue(null);
        String formatted = placeholderHandler.format(taskField);
        assertEquals("", formatted);
    }

    @Test
    public void testTaskPlaceholder_MultiplePlaceholders() {
        String query = SearchUtils.formatPlaceholders(
                "task: processId eq {} and userId eq {} and transitionId eq {}",
                placeholderHandler,
                "proc-123", "user-456", "t1"
        );
        assertEquals("task: processId eq 'proc-123' and userId eq 'user-456' and transitionId eq 't1'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testTaskPlaceholder_MultiplePlaceholders_DateAndString() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        String query = SearchUtils.formatPlaceholders(
                "task: transitionId eq {} and lastAssign in ({} : {})",
                placeholderHandler,
                "transition-1", from, to
        );
        assertEquals(
                "task: transitionId eq 'transition-1' and lastAssign in (2024-01-01T00:00:00.0 : 2024-12-31T23:59:59.0)",
                query
        );
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    // =========================================================================
    // PROCESS queries
    // =========================================================================

    @Test
    public void testProcessPlaceholder_String() {
        String query = SearchUtils.formatPlaceholders("process: identifier eq {}", placeholderHandler, "my-process");
        assertEquals("process: identifier eq 'my-process'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_StringList() {
        String query = SearchUtils.formatPlaceholders("process: identifier in {}", placeholderHandler,
                List.of("proc-a", "proc-b"));
        assertEquals("process: identifier in ('proc-a', 'proc-b')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_Boolean() {
        String formatted = placeholderHandler.format(true);
        assertEquals("true", formatted);
    }

    @Test
    public void testProcessPlaceholder_Number_Integer() {
        String formatted = placeholderHandler.format(7);
        assertEquals("7", formatted);
    }

    @Test
    public void testProcessPlaceholder_Number_Long() {
        String formatted = placeholderHandler.format(1000L);
        assertEquals("1000", formatted);
    }

    @Test
    public void testProcessPlaceholder_NumberList() {
        String formatted = placeholderHandler.format(List.of(1, 2, 3));
        assertEquals("(1, 2, 3)", formatted);
    }

    @Test
    public void testProcessPlaceholder_ObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439055");
        String query = SearchUtils.formatPlaceholders("process: id eq {}", placeholderHandler, id);
        assertEquals("process: id eq '507f1f77bcf86cd799439055'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_ObjectIdList() {
        ObjectId id1 = new ObjectId("507f1f77bcf86cd799439055");
        ObjectId id2 = new ObjectId("507f1f77bcf86cd799439066");
        String query = SearchUtils.formatPlaceholders("process: id in {}", placeholderHandler, List.of(id1, id2));
        assertEquals("process: id in ('507f1f77bcf86cd799439055', '507f1f77bcf86cd799439066')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_DateTime_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2023, 11, 1, 9, 0, 0);
        String query = SearchUtils.formatPlaceholders("process: creationDate eq {}", placeholderHandler, dt);
        assertEquals("process: creationDate eq 2023-11-01T09:00:00.0", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_DateTime_String() {
        String query = SearchUtils.formatPlaceholders("process: creationDate gte {}", placeholderHandler, "2023-01-01T00:00:00");
        assertEquals("process: creationDate gte 2023-01-01T00:00:00", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_DateTimeList() {
        LocalDateTime dt1 = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        LocalDateTime dt2 = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        String query = SearchUtils.formatPlaceholders("process: creationDate in {}", placeholderHandler, List.of(dt1, dt2));
        assertEquals("process: creationDate in (2022-01-01T00:00:00.0, 2023-01-01T00:00:00.0)", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_Date_LocalDate() {
        LocalDate date = LocalDate.of(2024, 12, 1);
        String query = SearchUtils.formatPlaceholders("process: creationDate lte {}", placeholderHandler, date);
        assertEquals("process: creationDate lte 2024-12-01", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_Date_String() {
        String query = SearchUtils.formatPlaceholders("process: creationDate lt {}", placeholderHandler, "2024-12-31");
        assertEquals("process: creationDate lt 2024-12-31", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_DateList() {
        LocalDate d1 = LocalDate.of(2023, 3, 1);
        LocalDate d2 = LocalDate.of(2023, 6, 1);
        String query = SearchUtils.formatPlaceholders("process: creationDate in {}", placeholderHandler, List.of(d1, d2));
        assertEquals("process: creationDate in (2023-03-01, 2023-06-01)", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_MultiplePlaceholders() {
        String query = SearchUtils.formatPlaceholders(
                "process: identifier eq {} and title eq {}",
                placeholderHandler,
                "my-process", "My Process Title"
        );
        assertEquals("process: identifier eq 'my-process' and title eq 'My Process Title'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testProcessPlaceholder_MultiplePlaceholders_IdAndDateRange() {
        ObjectId oid = new ObjectId("507f1f77bcf86cd799439099");
        LocalDateTime from = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        String query = SearchUtils.formatPlaceholders(
                "process: id eq {} and creationDate in ({} : {})",
                placeholderHandler,
                oid, from, to
        );
        assertEquals(
                "process: id eq '507f1f77bcf86cd799439099' and creationDate in (2023-01-01T00:00:00.0 : 2024-01-01T00:00:00.0)",
                query
        );
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    // =========================================================================
    // USER queries
    // =========================================================================

    @Test
    public void testUserPlaceholder_String() {
        String query = SearchUtils.formatPlaceholders("user: email eq {}", placeholderHandler, "user@example.com");
        assertEquals("user: email eq 'user@example.com'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testUserPlaceholder_StringContains() {
        String query = SearchUtils.formatPlaceholders("user: name contains {}", placeholderHandler, "John");
        assertEquals("user: name contains 'John'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testUserPlaceholder_StringList() {
        String query = SearchUtils.formatPlaceholders("user: email in {}", placeholderHandler,
                List.of("a@example.com", "b@example.com"));
        assertEquals("user: email in ('a@example.com', 'b@example.com')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testUserPlaceholder_Boolean() {
        String formatted = placeholderHandler.format(Boolean.FALSE);
        assertEquals("false", formatted);
    }

    @Test
    public void testUserPlaceholder_Number() {
        String formatted = placeholderHandler.format(99);
        assertEquals("99", formatted);
    }

    @Test
    public void testUserPlaceholder_NumberList() {
        String formatted = placeholderHandler.format(List.of(10, 20));
        assertEquals("(10, 20)", formatted);
    }

    @Test
    public void testUserPlaceholder_ObjectId() {
        ObjectId id = new ObjectId("507f1f77bcf86cd799439077");
        String query = SearchUtils.formatPlaceholders("user: id eq {}", placeholderHandler, id);
        assertEquals("user: id eq '507f1f77bcf86cd799439077'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testUserPlaceholder_ObjectIdList() {
        ObjectId id1 = new ObjectId("507f1f77bcf86cd799439077");
        ObjectId id2 = new ObjectId("507f1f77bcf86cd799439088");
        String query = SearchUtils.formatPlaceholders("user: id in {}", placeholderHandler, List.of(id1, id2));
        assertEquals("user: id in ('507f1f77bcf86cd799439077', '507f1f77bcf86cd799439088')", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testUserPlaceholder_DateTime_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 7, 4, 12, 0, 0);
        String formatted = placeholderHandler.format(dt);
        assertEquals("2024-07-04T12:00:00.0", formatted);
    }

    @Test
    public void testUserPlaceholder_DateTime_String() {
        String formatted = placeholderHandler.format("2024-07-04T12:00:00");
        assertEquals("2024-07-04T12:00:00", formatted);
    }

    @Test
    public void testUserPlaceholder_DateTimeList() {
        LocalDateTime dt1 = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime dt2 = LocalDateTime.of(2024, 6, 15, 0, 0, 0);
        String formatted = placeholderHandler.format(List.of(dt1, dt2));
        assertEquals("(2024-01-01T00:00:00.0, 2024-06-15T00:00:00.0)", formatted);
    }

    @Test
    public void testUserPlaceholder_Date_LocalDate() {
        LocalDate date = LocalDate.of(2024, 8, 22);
        String formatted = placeholderHandler.format(date);
        assertEquals("2024-08-22", formatted);
    }

    @Test
    public void testUserPlaceholder_Date_String() {
        String formatted = placeholderHandler.format("2024-08-22");
        assertEquals("2024-08-22", formatted);
    }

    @Test
    public void testUserPlaceholder_DateList() {
        LocalDate d1 = LocalDate.of(2024, 4, 1);
        LocalDate d2 = LocalDate.of(2024, 8, 1);
        String formatted = placeholderHandler.format(List.of(d1, d2));
        assertEquals("(2024-04-01, 2024-08-01)", formatted);
    }

    @Test
    public void testUserPlaceholder_MultiplePlaceholders() {
        String query = SearchUtils.formatPlaceholders(
                "user: name eq {} and surname eq {} and email eq {}",
                placeholderHandler,
                "John", "Doe", "john.doe@example.com"
        );
        assertEquals("user: name eq 'John' and surname eq 'Doe' and email eq 'john.doe@example.com'", query);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    @Test
    public void testUserPlaceholder_MultiplePlaceholders_IdAndNameList() {
        ObjectId oid = new ObjectId("507f1f77bcf86cd799439077");
        String query = SearchUtils.formatPlaceholders(
                "user: id eq {} and email in {}",
                placeholderHandler,
                oid, List.of("alice@example.com", "bob@example.com")
        );
        assertEquals(
                "user: id eq '507f1f77bcf86cd799439077' and email in ('alice@example.com', 'bob@example.com')",
                query
        );
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(query));
    }

    // =========================================================================
    // Unsupported type
    // =========================================================================

    @Test
    public void testUnsupportedType_ThrowsException() {
        Object unsupported = new Object();
        assertThrows(IllegalArgumentException.class, () -> placeholderHandler.format(unsupported));
    }

    // =========================================================================
    // No placeholders
    // =========================================================================

    @Test
    public void testNoPlaceholders_QueryUnchanged() {
        String original = "case: title eq 'fixed-title'";
        String result = SearchUtils.formatPlaceholders(original, placeholderHandler);
        assertEquals(original, result);
        assertDoesNotThrow(() -> SearchUtils.evaluateQuery(result));
    }

    @Test
    public void testNullArgs_QueryUnchanged() {
        String original = "case: title eq 'fixed-title'";
        String result = SearchUtils.formatPlaceholders(original, placeholderHandler, (Object[]) null);
        assertEquals(original, result);
    }
}