package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class MapField extends TextField {

    public static final String NONE_OPTION_KEY = "none";

    protected List<String> keyValue;
    protected Map<String, I18nString> keyValueTranslations;

    public MapField(MapField field) {
        super(field);
        this.keyValue = field.keyValue == null ? null : new ArrayList<>(field.keyValue);
        this.keyValueTranslations = field.keyValueTranslations == null ? null
                : field.keyValueTranslations.entrySet().stream()
                .collect(Collectors.toMap(entry ->
                        resolveTranslationPairKey(entry.getKey()),
                        entry -> new I18nString(entry.getValue()),
                        (existing, replacement) -> replacement, LinkedHashMap::new));
    }

    public MapField(Map.Entry<String, I18nString> valueTranslationPair) {
        this(List.of(valueTranslationPair));
    }

    public MapField(List<Map.Entry<String, I18nString>> valueTranslationPairs) {
        if (valueTranslationPairs == null || valueTranslationPairs.isEmpty()) {
            return;
        }
        List<String> values = new ArrayList<>();
        this.keyValue = new ArrayList<>();
        this.keyValueTranslations = new LinkedHashMap<>();
        for (Map.Entry<String, I18nString> valueTranslationPair : valueTranslationPairs) {
            this.keyValue.add(resolveTranslationPairKey(valueTranslationPair.getKey()));
            values.addAll(I18nStringUtils.collectTranslations(valueTranslationPair.getValue()));
            this.keyValueTranslations.put(resolveTranslationPairKey(valueTranslationPair.getKey()), valueTranslationPair.getValue());
        }
        this.textValue = values;
        this.fulltextValue = values;
    }

    public Object getValue() {
        if (this.keyValue != null && this.keyValue.size() == 1) {
            return this.keyValue.getFirst();
        } else if (this.keyValue != null && this.keyValue.size() > 1) {
            return new LinkedHashSet<>(this.keyValue);
        }
        return null;
    }

    private String resolveTranslationPairKey(String key) {
        return key == null || key.isBlank() ? NONE_OPTION_KEY : key;
    }
}
