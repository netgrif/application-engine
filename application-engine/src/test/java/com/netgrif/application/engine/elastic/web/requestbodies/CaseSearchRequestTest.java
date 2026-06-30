package com.netgrif.application.engine.elastic.web.requestbodies;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaseSearchRequestTest {

    @Test
    void createsRequestFromLegacyMapAndSanitizesFullText() {
        CaseSearchRequest request = new CaseSearchRequest(Map.of(
                "process", List.of("invoice"),
                "processIdentifier", List.of("legacy"),
                "author", List.of(Map.of("id", "user-1", "name", "John", "username", "john", "realm", "default")),
                "data", Map.of("status", "open"),
                "fullText", "invoice:title",
                "transition", List.of("approve"),
                "role", List.of("manager"),
                "query", "status:open",
                "stringId", List.of("case-1"),
                "group", List.of("group-1")
        ));

        assertEquals("invoice", request.process.getFirst().identifier);
        assertEquals(List.of("legacy"), request.processIdentifier);
        assertEquals("user-1", request.author.getFirst().id);
        assertEquals("John", request.author.getFirst().name);
        assertEquals("john", request.author.getFirst().username);
        assertEquals("default", request.author.getFirst().realm);
        assertEquals(Map.of("status", "open"), request.data);
        assertEquals("invoice\\:title", request.fullText);
        assertEquals(List.of("approve"), request.transition);
        assertEquals(List.of("manager"), request.role);
        assertEquals("status:open", request.query);
        assertEquals(List.of("case-1"), request.stringId);
        assertEquals(List.of("group-1"), request.group);
    }

    @Test
    void rejectsAuthorFilterWithoutSupportedAttributes() {
        Map<String, Object> request = Map.of("author", List.of(Map.of("unsupported", "value")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CaseSearchRequest(request)
        );

        assertEquals("Author filter must contain at least one of: id, name, username, realm", exception.getMessage());
    }

    @Test
    void ignoresValuesWithUnexpectedTypes() {
        CaseSearchRequest request = new CaseSearchRequest(Map.of(
                "process", "invoice",
                "author", Map.of("id", "user-1"),
                "fullText", List.of("bad"),
                "query", 42
        ));

        assertEquals(null, request.process);
        assertEquals(null, request.author);
        assertEquals(null, request.fullText);
        assertEquals(null, request.query);
    }
}
