package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.DateField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedDateField extends ValidableLocalisedField<DateField> {

    private String minDate;

    private String maxDate;

    private Object defaultValue;

    public LocalisedDateField(DateField field, Locale locale) {
        super(field, locale);
        this.minDate = field.getMinDate();
        this.maxDate = field.getMaxDate();
        this.defaultValue = field.getDefaultValue();
    }
}
