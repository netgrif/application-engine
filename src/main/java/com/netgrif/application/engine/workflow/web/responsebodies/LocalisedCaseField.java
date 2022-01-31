package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class LocalisedCaseField extends LocalisedField {

    private List<String> allowedNets;

    public LocalisedCaseField(CaseField field, Locale locale) {
        super(field, locale);
        this.allowedNets = field.getAllowedNets();
    }

}
