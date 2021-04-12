package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.DateTimeField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedDateTimeField extends ValidableLocalisedField<DateTimeField> {

    private String minDate;

    private String maxDate;

    private Object defaultValue;

    public LocalisedDateTimeField(DateTimeField field, Locale locale) {
        super(field, locale);
        this.minDate = field.getMinDate();
        this.maxDate = field.getMaxDate();
        this.defaultValue = field.getDefaultValue();
    }
}
