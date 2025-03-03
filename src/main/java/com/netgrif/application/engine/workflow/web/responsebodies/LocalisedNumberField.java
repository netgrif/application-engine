package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.core.petrinet.domain.dataset.NumberField;
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
