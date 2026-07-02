package com.netgrif.application.engine.objects.petrinet.domain;


import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.HashMap;
import java.util.Map;

public class I18nStringDeserializer extends StdDeserializer<I18nString> {

    public I18nStringDeserializer() {
        super(I18nString.class);
    }

    @Override
    public I18nString deserialize(JsonParser jp, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(jp);

        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isString()) {
            return new I18nString(node.asString());
        }

        if (!node.isObject()) {
            return context.reportInputMismatch(
                    I18nString.class,
                    "Cannot deserialize I18nString from JSON node type '%s'",
                    node.getNodeType()
            );
        }

        if (!node.hasNonNull("defaultValue")) {
            return context.reportInputMismatch(
                    I18nString.class,
                    "Missing required non-null property 'defaultValue' for I18nString"
            );
        }

        I18nString text = new I18nString(node.get("defaultValue").asString());

        JsonNode keyNode = node.get("key");
        if (keyNode != null && !keyNode.isNull()) {
            text.setKey(keyNode.asString());
        }

        JsonNode translationsNode = node.get("translations");
        if (translationsNode != null && !translationsNode.isNull()) {
            if (!translationsNode.isObject()) {
                return context.reportInputMismatch(
                        I18nString.class,
                        "Property 'translations' must be an object"
                );
            }

            Map<String, String> translations = new HashMap<>();
            translationsNode.properties().forEach(entry -> {
                JsonNode value = entry.getValue();
                translations.put(entry.getKey(), value == null || value.isNull() ? null : value.asString());
            });
            text.setTranslations(translations);
        }

        return text;
    }
}
