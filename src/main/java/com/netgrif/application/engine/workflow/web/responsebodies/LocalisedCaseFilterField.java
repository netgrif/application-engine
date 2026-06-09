package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseFilterField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedCaseFilterField extends LocalisedField {

    public LocalisedCaseFilterField(CaseFilterField field, Locale locale) {
        super(field, locale);
        setComponent(new Component("string_query"));
    }
}
