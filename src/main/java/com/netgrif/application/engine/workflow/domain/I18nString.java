package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.netgrif.application.engine.petrinet.converter.I18nStringSerializer;
import com.netgrif.application.engine.workflow.domain.dataset.logic.Expression;
import lombok.Data;

import java.util.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Data
@JsonSerialize(using = I18nStringSerializer.class)
public class I18nString implements Serializable {

    private static final long serialVersionUID = 3815235231390109824L;

    private String defaultValue;

    private String key;

    private Expression<String> expression;

    /**
     * locale - translation
     */
    private Map<String, String> translations;

    public I18nString() {
        this.translations = new HashMap<>();
    }

    public I18nString(String defaultValue) {
        this();
        this.defaultValue = defaultValue;
    }

    public I18nString(String key, String defaultValue) {
        this(defaultValue);
        this.key = key;
    }

    public I18nString(String defaultValue, Map<String, String> translations) {
        this(defaultValue);
        this.translations = translations;
    }

    public void addTranslation(String locale, String translation) {
        translations.put(locale, translation);
    }

    public String getTranslation(String locale) {
        return translations.getOrDefault(locale, defaultValue);
    }

    public String getTranslation(Locale locale) {
        if (locale == null) {
            return defaultValue;
        }
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

    public boolean contains(String value) {
        if (defaultValue != null && defaultValue.equals(value))
            return true;
        for (String s : translations.values()) {
            if (s.equals(value))
                return true;
        }
        return false;
    }

    public List<String> collectTranslations() {
        List<String> translations = new ArrayList<>();
        translations.add(defaultValue);
        translations.addAll(getTranslations().values());
        return translations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        I18nString that = (I18nString) o;
        return Objects.equals(defaultValue, that.defaultValue) && Objects.equals(key, that.key) && Objects.equals(translations, that.translations);
    }

    @Override
    public I18nString clone() {
        I18nString clone = new I18nString();
        clone.setKey(this.key);
        clone.setDefaultValue(this.defaultValue);
        clone.setTranslations(new HashMap<>(this.translations));
        return clone;
    }
}