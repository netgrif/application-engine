package com.netgrif.application.engine.utils;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleItemAsListDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializesSingleValueIntoOneItemList() throws Exception {
        IntegerList wrapper = mapper.readValue("1", IntegerList.class);

        assertEquals(List.of(1), wrapper.getList());
    }

    @Test
    void deserializesArrayIntoList() throws Exception {
        IntegerList wrapper = mapper.readValue("[1,2,3]", IntegerList.class);

        assertEquals(List.of(1, 2, 3), wrapper.getList());
    }

    @Test
    void requiresContentTypeAnnotationOnWrapperClass() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SingleItemAsListDeserializer(MissingAnnotationList.class)
        );

        assertTrue(exception.getMessage().contains("@JsonDeserialize"));
    }

    @Test
    void reportsBadRequestWhenSingleItemCannotBeConverted() {
        Exception exception = assertThrows(Exception.class, () ->
                mapper.readValue("{\"name\":\"missing id\"}", StrictItemList.class)
        );

        assertTrue(throwableDetails(exception).contains("id is required"));
    }

    @Test
    void reportsBadRequestWhenArrayItemCannotBeConverted() {
        Exception exception = assertThrows(Exception.class, () ->
                mapper.readValue("[{\"id\":\"ok\"},{\"name\":\"missing id\"}]", StrictItemList.class)
        );

        assertTrue(throwableDetails(exception).contains("id is required"));
    }

    private String throwableDetails(Throwable throwable) {
        StringBuilder details = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            details.append(current.getClass().getName()).append(": ").append(current.getMessage()).append('\n');
            for (Throwable suppressed : current.getSuppressed()) {
                details.append(suppressed.getClass().getName()).append(": ").append(suppressed.getMessage()).append('\n');
            }
            current = current.getCause();
        }
        return details.toString();
    }

    @JsonDeserialize(using = SingleItemAsListDeserializer.class, contentAs = Integer.class)
    static class IntegerList extends SingleItemAsList<Integer> {
    }

    static class MissingAnnotationList extends SingleItemAsList<Integer> {
    }

    @JsonDeserialize(using = SingleItemAsListDeserializer.class, contentAs = StrictItem.class)
    static class StrictItemList extends SingleItemAsList<StrictItem> {
    }

    record StrictItem(String id) {
        StrictItem {
            if (id == null) {
                throw new IllegalArgumentException("id is required");
            }
        }
    }
}
