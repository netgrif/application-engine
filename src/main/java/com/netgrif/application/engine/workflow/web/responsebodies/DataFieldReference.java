package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.web.responsebodies.Reference;
import lombok.Data;

import java.util.Locale;

@Data
public class DataFieldReference extends Reference {

    private DataType type;

    public DataFieldReference() {
        super();
    }

    public DataFieldReference(String stringId, String title, DataType type) {
        super(stringId, title);
        this.type = type;
    }

    public DataFieldReference(Field field, Locale locale) {
        this(field.getStringId(), field.getTranslatedName(locale), field.getType());
    }
}
