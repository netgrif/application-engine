package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.web.responsebodies.Reference;
import lombok.Data;

import java.util.Locale;

@Data
public class DataFieldReference extends Reference {

    private String type;

    public DataFieldReference() {
        super();
    }

    public DataFieldReference(String stringId, String title, String type) {
        super(stringId, title);
        this.type = type;
    }

    public DataFieldReference(Field field, Locale locale) {
        this(field.getStringId(), field.getTranslatedName(locale), field.getType().getName());
    }
}
