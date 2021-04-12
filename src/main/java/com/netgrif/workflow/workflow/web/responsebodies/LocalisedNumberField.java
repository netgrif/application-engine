package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.NumberField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedNumberField extends ValidableLocalisedField<NumberField> {

    private Double minValue;

    private Double maxValue;

    private Object defaultValue;

    public LocalisedNumberField(NumberField field, Locale locale) {
        super(field, locale);
        this.minValue = field.getMinValue();
        this.maxValue = field.getMaxValue();
        this.defaultValue = field.getDefaultValue();
    }
}
