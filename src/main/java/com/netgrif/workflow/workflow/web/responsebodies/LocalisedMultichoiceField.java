package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceField;
import lombok.Data;

import java.util.*;

@Data
public class LocalisedMultichoiceField extends LocalisedField {

    private List<String> choices;

    private Object defaultValue;

    public LocalisedMultichoiceField(MultichoiceField field, Locale locale) {
        super(field, locale);
        this.choices = new LinkedList<>();
        Set<I18nString> choices = field.getChoices();
        for (I18nString choice : choices) {
            this.choices.add(choice.getTranslation(locale));
        }

        this.defaultValue = new LinkedList<String>();
        Collection<I18nString> fieldDefaults = field.getDefaultValue();
        if (fieldDefaults != null) {
            for (I18nString fieldDefault : fieldDefaults) {
                ((List<String>) this.defaultValue).add(fieldDefault.getTranslation(locale));
            }
        }

        this.setValue(new LinkedList<>());
        Collection<I18nString> values = field.getValue();
        if (values != null) {
            for (I18nString value : values) {
                ((List) this.getValue()).add(value.getTranslation(locale));
            }
        }
    }
}
