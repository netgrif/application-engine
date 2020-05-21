package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.CaseField;
import lombok.Data;

import java.util.Locale;
import java.util.Set;

@Data
public class LocalisedCaseField extends LocalisedField {

    private Set<String> allowedNets;

    public LocalisedCaseField(CaseField field, Locale locale) {
        super(field, locale);
        this.allowedNets = field.getAllowedNets();
    }

}
