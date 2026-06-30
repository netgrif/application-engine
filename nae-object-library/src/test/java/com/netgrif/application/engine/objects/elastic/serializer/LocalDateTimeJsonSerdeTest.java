package com.netgrif.application.engine.objects.elastic.serializer;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LocalDateTimeJsonSerdeTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new SimpleModule()
                    .addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer())
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer()))
            .build();

    @Test
    void serializesLocalDateTimeWithMilliseconds() throws Exception {
        DateTimeHolder holder = new DateTimeHolder(LocalDateTime.of(2026, Month.JUNE, 29, 12, 3, 4, 5_000_000));

        assertEquals("{\"value\":\"2026-06-29T12:03:04.005\"}", mapper.writeValueAsString(holder));
    }

    @Test
    void deserializesLocalDateTimeWithoutMilliseconds() throws Exception {
        DateTimeHolder holder = mapper.readValue(
                "{\"value\":\"2026-06-29T12:03:04\"}",
                DateTimeHolder.class
        );

        assertEquals(LocalDateTime.of(2026, Month.JUNE, 29, 12, 3, 4), holder.value);
    }

    @Test
    void deserializesLocalDateTimeWithOneToThreeMillisecondDigits() throws Exception {
        DateTimeHolder oneDigit = mapper.readValue(
                "{\"value\":\"2026-06-29T12:03:04.5\"}",
                DateTimeHolder.class
        );
        DateTimeHolder twoDigits = mapper.readValue(
                "{\"value\":\"2026-06-29T12:03:04.12\"}",
                DateTimeHolder.class
        );
        DateTimeHolder threeDigits = mapper.readValue(
                "{\"value\":\"2026-06-29T12:03:04.123\"}",
                DateTimeHolder.class
        );

        assertEquals(LocalDateTime.of(2026, Month.JUNE, 29, 12, 3, 4, 500_000_000), oneDigit.value);
        assertEquals(LocalDateTime.of(2026, Month.JUNE, 29, 12, 3, 4, 120_000_000), twoDigits.value);
        assertEquals(LocalDateTime.of(2026, Month.JUNE, 29, 12, 3, 4, 123_000_000), threeDigits.value);
    }

    @Test
    void deserializesEmptyStringAsNull() throws Exception {
        DateTimeHolder holder = mapper.readValue("{\"value\":\"\"}", DateTimeHolder.class);

        assertNull(holder.value);
    }

    static class DateTimeHolder {
        public LocalDateTime value;

        DateTimeHolder() {
        }

        DateTimeHolder(LocalDateTime value) {
            this.value = value;
        }
    }
}
