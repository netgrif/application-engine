package com.netgrif.application.engine.objects.petrinet.domain;


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
    public I18nString deserialize(JsonParser jp, DeserializationContext context) {
        JsonNode node = jp.objectReadContext().readTree(jp);

        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isString()) {
            return new I18nString(node.asString());
        }

        if (!node.isObject() || !node.hasNonNull("defaultValue")) {
            return new I18nString("");
        }

        I18nString text = new I18nString(node.get("defaultValue").asString());

        JsonNode keyNode = node.get("key");
        if (keyNode != null && !keyNode.isNull()) {
            text.setKey(keyNode.asString());
        }

        JsonNode translationsNode = node.get("translations");
        if (translationsNode != null && translationsNode.isObject()) {
            Map<String, String> translations = new HashMap<>();
            translationsNode.properties().forEach(entry ->
                    translations.put(entry.getKey(), entry.getValue().asString())
            );
            text.setTranslations(translations);
        }

        return text;
    }
}

