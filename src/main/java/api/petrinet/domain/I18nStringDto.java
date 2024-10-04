package api.petrinet.domain;

import java.util.Map;

public final class I18nStringDto {

    private String defaultValue;

    private String key;

    private Map<String, String> translations;

    public I18nStringDto() {
    }

    public I18nStringDto(String defaultValue, String key, Map<String, String> translations) {
        this.defaultValue = defaultValue;
        this.key = key;
        this.translations = translations;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, String> translations) {
        this.translations = translations;
    }

    @Override
    public String toString() {
        return defaultValue;
    }
}
