package com.netgrif.workflow.petrinet.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Locale;
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

    public String getTranslation(String locale) {
        return translations.getOrDefault(locale, defaultValue);
    }

    public String getTranslation(Locale locale) {
        if (locale == null)
            return defaultValue;
        return getTranslation(locale.getLanguage());
    }

    @Override
    public String toString() {
        return defaultValue;
    }

    public boolean equals(String string) {
        if (defaultValue == null)
            return false;
        return defaultValue.equals(string);
    }
}