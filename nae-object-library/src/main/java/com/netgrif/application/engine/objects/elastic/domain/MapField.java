package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class MapField extends TextField {

    public String[] keyValue;
    public Map<String, I18nString> keyValueTranslations;

    public MapField(Map.Entry<String, I18nString> valueTranslationPair) {
        super(new String[0]);
        List<String> values = I18nStringUtils.collectTranslations(valueTranslationPair.getValue());
        this.keyValue = new String[1];
        this.keyValue[0] = valueTranslationPair.getKey();
        this.textValue = values.toArray(new String[0]);
        this.fulltextValue = values.toArray(new String[0]);
        this.keyValueTranslations = new HashMap<>();
        this.keyValueTranslations.put(valueTranslationPair.getKey(), valueTranslationPair.getValue());
    }

    public MapField(List<Map.Entry<String, I18nString>> valueTranslationPairs) {
        super(new String[0]);
        this.keyValue = new String[valueTranslationPairs.size()];
        List<String> values = new ArrayList<>();
        this.keyValueTranslations = new HashMap<>();
        for (int i = 0; i < valueTranslationPairs.size(); i++) {
            this.keyValue[i] = valueTranslationPairs.get(i).getKey();
            values.addAll(I18nStringUtils.collectTranslations(valueTranslationPairs.get(i).getValue()));
            this.keyValueTranslations.put(valueTranslationPairs.get(i).getKey(), valueTranslationPairs.get(i).getValue());
        }
        this.textValue = values.toArray(new String[0]);
        this.fulltextValue = values.toArray(new String[0]);
    }

    public Object getValue() {
        if (keyValue != null && keyValue.length == 1) {
            return keyValue[0];
        } else if (keyValue != null && keyValue.length > 1) {
            return new LinkedHashSet<>(Arrays.asList(keyValue));
        }
        return null;
    }
}
