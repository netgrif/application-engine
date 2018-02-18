package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceField;
import lombok.Data;

import java.util.*;

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

    private List<String> choices;

    private Object defaultValue;

    public LocalisedField(Field field, Locale locale) {
        stringId = field.getStringId();
        type = field.getType();
        name = field.getTranslatedName(locale);
        description = field.getTranslatedDescription(locale);
        placeholder = field.getTranslatedPlaceholder(locale);
        behavior = field.getBehavior();
        value = field.getValue();
        order = field.getOrder();
        if (field instanceof EnumerationField) {
            fromEnumeration((EnumerationField) field, locale);
        } else if (field instanceof MultichoiceField) {
            fromMultichoice((MultichoiceField) field, locale);
        }
    }

    private void fromEnumeration(EnumerationField field, Locale locale) {
        this.choices = new LinkedList<>();
        Set<I18nString> choices = field.getChoices();
        for (I18nString choice : choices) {
            this.choices.add(choice.getTranslation(locale));
        }

        I18nString defaultI18n = field.getDefaultValue();
        if (defaultI18n != null)
            this.defaultValue = defaultI18n.getTranslation(locale);

        value = field.getTranslatedValue(locale);
    }

    private void fromMultichoice(MultichoiceField field, Locale locale) {
        this.choices = new LinkedList<>();
        Set<I18nString> choices = field.getChoices();
        for (I18nString choice : choices) {
            this.choices.add(choice.getTranslation(locale));
        }

        this.defaultValue = new LinkedList<String>();
        Collection<I18nString> fieldDefaults = field.getDefaultValue();
        for (I18nString fieldDefault : fieldDefaults) {
            ((List<String>) this.defaultValue).add(fieldDefault.getTranslation(locale));
        }

        this.value = new LinkedList<>();
        Collection<I18nString> values = field.getValue();
        for (I18nString value : values) {
            ((List) this.value).add(value.getTranslation(locale));
        }
    }
}