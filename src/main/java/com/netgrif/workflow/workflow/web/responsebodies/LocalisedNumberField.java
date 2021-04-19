package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.NumberField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedNumberField extends LocalisedField {

    private Double minValue;

    private Double maxValue;

    public LocalisedNumberField(NumberField field, Locale locale) {
        super(field, locale);
        this.minValue = field.getMinValue();
        this.maxValue = field.getMaxValue();
    }
}
