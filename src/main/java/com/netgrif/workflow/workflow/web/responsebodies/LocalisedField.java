package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.importer.model.Component;
import com.netgrif.workflow.petrinet.domain.Format;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.workflow.petrinet.domain.views.View;
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

    private View view;

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
        view = field.getView();
        length = field.getLength();
        component = field.getComponent();
    }
}