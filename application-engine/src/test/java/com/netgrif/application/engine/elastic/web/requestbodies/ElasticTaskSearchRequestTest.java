package com.netgrif.application.engine.elastic.web.requestbodies;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ElasticTaskSearchRequestTest {

    @Test
    void mapConstructorPopulatesSupportedFiltersAndSanitizesFullText() {
        Map<String, Object> request = new HashMap<>();
        request.put("role", List.of("manager"));
        request.put("useCase", List.of(Map.of("id", "case-1", "title", "Case title")));
        request.put("title", List.of("Approve task"));
        request.put("user", List.of("assignee"));
        request.put("process", List.of("invoice"));
        request.put("transitionId", List.of("approve"));
        request.put("fullText", "task + title");
        request.put("group", List.of("group-1"));
        request.put("users", List.of("candidate"));
        request.put("query", "status:open");

        ElasticTaskSearchRequest parsed = new ElasticTaskSearchRequest(request);

        assertEquals(List.of("manager"), parsed.role);
        assertEquals("case-1", parsed.useCase.get(0).id);
        assertEquals("Case title", parsed.useCase.get(0).title);
        assertEquals(List.of("Approve task"), parsed.title);
        assertEquals(List.of("assignee"), parsed.user);
        assertEquals("invoice", parsed.process.get(0).identifier);
        assertEquals(List.of("approve"), parsed.transitionId);
        assertEquals("task\\ \\+\\ title", parsed.fullText);
        assertEquals(List.of("group-1"), parsed.group);
        assertEquals(List.of("candidate"), parsed.users);
        assertEquals("status:open", parsed.query);
    }

    @Test
    void mapConstructorAllowsPartialUseCaseFilter() {
        Map<String, Object> request = Map.of("useCase", List.of(Map.of("title", "Only title")));

        ElasticTaskSearchRequest parsed = new ElasticTaskSearchRequest(request);

        assertNull(parsed.useCase.get(0).id);
        assertEquals("Only title", parsed.useCase.get(0).title);
    }

    @Test
    void mapConstructorIgnoresValuesWithUnexpectedTypes() {
        Map<String, Object> request = new HashMap<>();
        request.put("role", "manager");
        request.put("useCase", Map.of("id", "case-1"));
        request.put("process", "invoice");
        request.put("fullText", List.of("task + title"));
        request.put("query", List.of("status:open"));

        ElasticTaskSearchRequest parsed = new ElasticTaskSearchRequest(request);

        assertNull(parsed.role);
        assertNull(parsed.useCase);
        assertNull(parsed.process);
        assertNull(parsed.fullText);
        assertNull(parsed.query);
    }
}
