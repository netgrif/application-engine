package com.netgrif.application.engine.workflow.web.responsebodies;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.Format;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
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

    private List<LocalizedValidation> validations;

    private String parentTaskId;

    private String parentCaseId;

    public LocalisedField() {}

    public LocalisedField(Field field, Locale locale) {
        this();
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
        validations = loadValidations(field, locale);
        parentTaskId = field.getParentTaskId();
        parentCaseId = field.getParentCaseId();
    }

    private List<LocalizedValidation> loadValidations(Field field, Locale locale) {
        List<LocalizedValidation> locVal = new ArrayList<>();
        if (field.getValidations() != null) {
            field.getValidations().forEach(val -> locVal.add(((Validation) val).getLocalizedValidation(locale)));
        }
        return locVal;
    }
}