package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.Format;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedField {

    private String stringId;

    private FieldType type;

    private String name;

    private String description;

    private String placeholder;

    private ObjectNode behavior;

    private Object value;

    private Long order;

    private Format formatFilter;

    public LocalisedField() {
    }

    public LocalisedField(Field field, Locale locale) {
        stringId = field.getStringId();
        type = field.getType();
        name = field.getTranslatedName(locale);
        description = field.getTranslatedDescription(locale);
        placeholder = field.getTranslatedPlaceholder(locale);
        behavior = field.getBehavior();
        value = field.getValue();
        order = field.getOrder();
        formatFilter = field.getFormat();
    }
}