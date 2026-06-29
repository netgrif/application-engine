package com.netgrif.application.engine.elastic.web.requestbodies;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaseSearchRequestTest {

    @Test
    void mapConstructorPopulatesSupportedFiltersAndSanitizesFullText() {
        Map<String, Object> request = new HashMap<>();
        request.put("process", List.of("invoice"));
        request.put("processIdentifier", List.of("legacy-invoice"));
        request.put("author", List.of(Map.of(
                "id", "user-1",
                "name", "Test User",
                "username", "tester",
                "realm", "default"
        )));
        request.put("data", Map.of("priority", "high"));
        request.put("fullText", "title:test");
        request.put("transition", List.of("approve"));
        request.put("role", List.of("manager"));
        request.put("query", "status:open");
        request.put("stringId", List.of("case-1"));
        request.put("group", List.of("group-1"));

        CaseSearchRequest parsed = new CaseSearchRequest(request);

        assertEquals("invoice", parsed.process.get(0).identifier);
        assertEquals(List.of("legacy-invoice"), parsed.processIdentifier);
        assertEquals("user-1", parsed.author.get(0).id);
        assertEquals("Test User", parsed.author.get(0).name);
        assertEquals("tester", parsed.author.get(0).username);
        assertEquals("default", parsed.author.get(0).realm);
        assertEquals(Map.of("priority", "high"), parsed.data);
        assertEquals("title\\:test", parsed.fullText);
        assertEquals(List.of("approve"), parsed.transition);
        assertEquals(List.of("manager"), parsed.role);
        assertEquals("status:open", parsed.query);
        assertEquals(List.of("case-1"), parsed.stringId);
        assertEquals(List.of("group-1"), parsed.group);
    }

    @Test
    void mapConstructorAllowsPartialAuthorFilter() {
        Map<String, Object> request = Map.of("author", List.of(Map.of("username", "tester")));

        CaseSearchRequest parsed = new CaseSearchRequest(request);

        assertNull(parsed.author.get(0).id);
        assertNull(parsed.author.get(0).name);
        assertEquals("tester", parsed.author.get(0).username);
        assertNull(parsed.author.get(0).realm);
    }

    @Test
    void mapConstructorRejectsEmptyAuthorFilter() {
        Map<String, Object> request = Map.of("author", List.of(Map.of()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CaseSearchRequest(request)
        );

        assertEquals("Author filter must contain at least one of: id, name, username, realm", exception.getMessage());
    }

    @Test
    void mapConstructorIgnoresValuesWithUnexpectedTypes() {
        Map<String, Object> request = new HashMap<>();
        request.put("process", "invoice");
        request.put("author", Map.of("username", "tester"));
        request.put("data", List.of("priority"));
        request.put("fullText", List.of("title:test"));

        CaseSearchRequest parsed = new CaseSearchRequest(request);

        assertNull(parsed.process);
        assertNull(parsed.author);
        assertNull(parsed.data);
        assertNull(parsed.fullText);
    }
}
