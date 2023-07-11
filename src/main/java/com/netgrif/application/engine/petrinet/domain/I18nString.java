package com.netgrif.application.engine.petrinet.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Data
public class I18nString {

    private String defaultValue;

    private String key;

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

    public I18nString(I18nString other) {
        this(other.defaultValue);
        this.key = other.key;
        this.translations.putAll(other.translations);
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

    public boolean contains(String value) {
        if (defaultValue != null && defaultValue.equals(value))
            return true;
        for (String s : translations.values()) {
            if (s.equals(value))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof String)
            return o.equals(this.defaultValue);
        if (o == null || getClass() != o.getClass()) return false;
        I18nString that = (I18nString) o;
        return (this.key == null ? that.key == null : this.key.equals(that.key)) &&
                (this.defaultValue == null ? that.defaultValue == null : this.defaultValue.equals(that.defaultValue)) &&
                this.translations.equals(that.translations);
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