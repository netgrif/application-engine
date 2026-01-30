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

    protected List<String> keyValue;
    protected Map<String, I18nString> keyValueTranslations;

    public MapField(MapField field) {
        super(field);
        this.keyValue = field.keyValue == null ? null : Arrays.copyOf(field.keyValue, field.keyValue.length);
        this.keyValueTranslations = field.keyValueTranslations == null ? null
                : field.keyValueTranslations.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new I18nString(entry.getValue())));
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
        this.keyValueTranslations = new HashMap<>();
        for (Map.Entry<String, I18nString> valueTranslationPair : valueTranslationPairs) {
            this.keyValue.add(valueTranslationPair.getKey());
            values.addAll(I18nStringUtils.collectTranslations(valueTranslationPair.getValue()));
            this.keyValueTranslations.put(valueTranslationPair.getKey(), valueTranslationPair.getValue());
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
}
