package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import static org.junit.jupiter.api.Assertions.*;

class I18nStringDeserializerTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new SimpleModule().addDeserializer(I18nString.class, new I18nStringDeserializer()))
            .build();

    @Test
    void deserializesTextValueAsDefaultValue() throws Exception {
        I18nString value = mapper.readValue("\"Hello\"", I18nString.class);

        assertEquals("Hello", value.getDefaultValue());
        assertNull(value.getKey());
        assertEquals(0, value.getTranslations().size());
    }

    @Test
    void deserializesFullObjectIncludingKeyAndTranslations() throws Exception {
        I18nString value = mapper.readValue(
                "{\"key\":\"field.title\",\"defaultValue\":\"Title\",\"translations\":{\"sk\":\"Nadpis\",\"de\":\"Titel\"}}",
                I18nString.class
        );

        assertEquals("field.title", value.getKey());
        assertEquals("Title", value.getDefaultValue());
        assertEquals("Nadpis", value.getTranslations().get("sk"));
        assertEquals("Titel", value.getTranslations().get("de"));
    }

    @Test
    void preservesNullTranslationValues() throws Exception {
        I18nString value = mapper.readValue(
                "{\"defaultValue\":\"Title\",\"translations\":{\"sk\":null}}",
                I18nString.class
        );

        assertTrue(value.getTranslations().containsKey("sk"));
        assertNull(value.getTranslations().get("sk"));
    }

    @Test
    void ignoresNullKeyAndTranslationsObject() throws Exception {
        I18nString value = mapper.readValue(
                "{\"key\":null,\"defaultValue\":\"Title\",\"translations\":null}",
                I18nString.class
        );

        assertNull(value.getKey());
        assertEquals("Title", value.getDefaultValue());
        assertTrue(value.getTranslations().isEmpty());
    }

    @Test
    void deserializesNullAsNull() throws Exception {
        assertNull(mapper.readValue("null", I18nString.class));
    }

    @Test
    void rejectsObjectWithoutDefaultValue() {
        assertThrows(JacksonException.class, () ->
                mapper.readValue("{\"key\":\"field.title\"}", I18nString.class)
        );
    }

    @Test
    void rejectsNonObjectTranslations() {
        assertThrows(JacksonException.class, () ->
                mapper.readValue("{\"defaultValue\":\"Title\",\"translations\":[\"bad\"]}", I18nString.class)
        );
    }

    @Test
    void rejectsArrayValue() {
        assertThrows(JacksonException.class, () ->
                mapper.readValue("[\"bad\"]", I18nString.class)
        );
    }
}
