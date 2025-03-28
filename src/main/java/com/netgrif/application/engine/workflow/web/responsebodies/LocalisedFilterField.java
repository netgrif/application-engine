package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.core.petrinet.domain.dataset.FilterField;
import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class LocalisedFilterField extends LocalisedField {

    private List<String> allowedNets;
    private Object filterMetadata;

    public LocalisedFilterField(FilterField field, Locale locale) {
        super(field, locale);
        this.allowedNets = field.getAllowedNets();
        this.filterMetadata = field.getFilterMetadata();
    }
}
