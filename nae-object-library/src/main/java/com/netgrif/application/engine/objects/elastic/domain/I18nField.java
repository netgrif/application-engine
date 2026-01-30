package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class I18nField extends TextField {

    protected Set<String> keyValue;
    protected Map<String, String> translations;

    public I18nField(I18nField field) {
        super(field);
        this.keyValue = field.keyValue == null ? null : new HashSet<>(field.keyValue);
        this.translations = field.translations == null ? null : new HashMap<>(field.translations);
    }

    public I18nField(Set<String> keys, Set<String> values, Map<String, String> translations) {
        super(new ArrayList<>(values));
        this.keyValue = keys;
        this.translations = translations;
    }

    @Override
    public Object getValue() {
        if (this.textValue != null && !this.textValue.isEmpty()) {
            return new I18nString(this.textValue.getFirst(), this.translations);
        }
        return null;
    }
}
