package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedDateField extends LocalisedField {

    private String minDate;

    private String maxDate;

    public LocalisedDateField(DateField field, Locale locale) {
//        super(field, locale); TODO: NAE-1645
//        TODO: NAE-1645 unused?
//        this.minDate = field.getMinDate();
//        this.maxDate = field.getMaxDate();
    }
}
