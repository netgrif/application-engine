package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ImmediateField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String stringId;
    private I18nString name;
    private FieldType type;

    public ImmediateField() {
    }

    public ImmediateField(Field<?> field) {
        this.stringId = field.getStringId();
        this.name = field.getName();
        this.type = field.getType();
    }
}
