package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ImmediateField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String stringId;
    private I18nString name;
    private String type;

    public ImmediateField() {
    }

    public ImmediateField(Field<?> field) {
        this(field.getStringId(), field.getName(), field.getType().getName());
    }

    public ImmediateField(String stringId, I18nString name, String type) {
        this.stringId = stringId;
        this.name = name;
        this.type = type;
    }
}
