package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class I18nStringDeserializer extends StdDeserializer<I18nString> {

    public I18nStringDeserializer() {
        this(null);
    }

    public I18nStringDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public I18nString deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.isTextual()) {
            return new I18nString(node.asText());
        } else {
            String defaultValue = node.get("defaultValue").asText();
            I18nString text = new I18nString(defaultValue);
            if (!node.get("key").isNull()) {
                text.setKey(node.get("key").asText());
            }
            if (!node.get("translations").isNull()) {
                Map<String, String> translations = new HashMap<>();
                node.get("translations").fields().forEachRemaining(entry ->
                        translations.put(entry.getKey(), entry.getValue().asText())
                );
                text.setTranslations(translations);
            }
            return text;
        }
    }
}

