package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class I18nField extends TextField {

    public String[] keyValue;

    public Map<String, String> translations;

    public I18nField(I18nField field) {
        super(field);
        this.keyValue = field.keyValue == null ? null : Arrays.copyOf(field.keyValue, field.keyValue.length);
        this.translations = field.translations == null ? null : new HashMap<>(field.translations);
    }

    public I18nField(Set<String> keys, Set<String> values, Map<String, String> translations) {
        super(new String[0]);
        this.keyValue = keys.toArray(new String[0]);
        this.textValue = values.toArray(new String[0]);
        this.translations = translations;
    }

    @Override
    public Object getValue() {
        if (textValue != null && textValue.length > 0) {
            return new I18nString(textValue[0], translations);
        }
        return null;
    }
}
