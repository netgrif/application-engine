package com.netgrif.application.engine.workflow.domain;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterDeserializerTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new SimpleModule().addDeserializer(Object.class, FilterDeserializer.getInstance()))
            .build();

    @Test
    void mapsNonListValuesToMap() throws Exception {
        Object parsed = mapper.readValue("{\"title\":\"My filter\",\"visible\":true}", Object.class);

        Map<?, ?> map = assertInstanceOf(Map.class, parsed);
        assertEquals("My filter", map.get("title"));
        assertEquals(Boolean.TRUE, map.get("visible"));
    }

    @Test
    void mapsNestedNonListValuesToMap() throws Exception {
        Object parsed = mapper.readValue("{\"title\":\"My filter\",\"metadata\":{\"owner\":\"admin\"}}", Object.class);

        Map<?, ?> map = assertInstanceOf(Map.class, parsed);
        Map<?, ?> metadata = assertInstanceOf(Map.class, map.get("metadata"));
        assertEquals("admin", metadata.get("owner"));
    }

    @Test
    void mapsConfiguredListValuesToList() throws Exception {
        Object parsed = mapper.readValue("{\"filter\":{\"id\":\"f1\"},\"allowedNet\":\"invoice\"}", Object.class);

        List<?> list = assertInstanceOf(List.class, parsed);
        assertEquals(2, list.size());
        assertEquals(Map.of("id", "f1"), list.get(0));
        assertEquals("invoice", list.get(1));
    }

    @Test
    void mapsEmptyObjectToEmptyMap() throws Exception {
        Object parsed = mapper.readValue("{}", Object.class);

        Map<?, ?> map = assertInstanceOf(Map.class, parsed);
        assertTrue(map.isEmpty());
    }

    @Test
    void mapsNestedListKeysWithoutLosingOrder() throws Exception {
        Object parsed = mapper.readValue(
                "{\"filter\":{\"id\":\"f1\"},\"predicate\":{\"field\":\"title\"},\"stringValue\":\"invoice\"}",
                Object.class
        );

        List<?> list = assertInstanceOf(List.class, parsed);
        assertEquals(3, list.size());
        assertEquals(Map.of("id", "f1"), list.get(0));
        assertEquals(Map.of("field", "title"), list.get(1));
        assertEquals("invoice", list.get(2));
    }
}
