package com.netgrif.workflow.petrinet.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class I18nString {

    private String defaultValue;

    /**
     * locale - translation
     */
    private Map<String, String> translations;

    public I18nString() {
        translations = new HashMap<>();
    }

    public I18nString(String defaultValue) {
        this();
        this.defaultValue = defaultValue;
    }

    public void addTranslation(String locale, String translation) {
        translations.put(locale, translation);
    }

    @Override
    public String toString() {
        return defaultValue;
    }
}