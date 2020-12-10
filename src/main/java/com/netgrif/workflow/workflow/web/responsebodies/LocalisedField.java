package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.Component;
import com.netgrif.workflow.petrinet.domain.Format;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldLayout;
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

    private FieldLayout layout;

    private Object value;

    private Long order;

    private Format formatFilter;

    private Integer length;

    private Component component;

    public LocalisedField() {
    }

    public LocalisedField(Field field, Locale locale) {
        stringId = field.getStringId();
        type = field.getType();
        name = field.getTranslatedName(locale);
        description = field.getTranslatedDescription(locale);
        placeholder = field.getTranslatedPlaceholder(locale);
        behavior = field.getBehavior();
        layout = field.getLayout();
        value = field.getValue();
        order = field.getOrder();
        formatFilter = field.getFormat();
        length = field.getLength();
        component = field.getComponent();
    }
}