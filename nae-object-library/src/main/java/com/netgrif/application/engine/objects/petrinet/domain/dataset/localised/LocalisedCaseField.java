package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.CaseField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedCaseField extends LocalisedField {

    private List<String> allowedNets;

    public LocalisedCaseField(CaseField field, Locale locale) {
        super(field, locale);
        this.allowedNets = field.getAllowedNets();
    }

}
