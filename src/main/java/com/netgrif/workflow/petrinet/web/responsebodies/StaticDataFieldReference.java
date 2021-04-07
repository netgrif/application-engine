package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.web.responsebodies.DataFieldReference;
import lombok.Data;

import java.util.Locale;

@Data
public class StaticDataFieldReference<T> extends DataFieldReference {

    private T value;

    public StaticDataFieldReference(Field<T> field, Locale locale) {
        super(field, locale);
        this.value = field.getValue();
    }
}
