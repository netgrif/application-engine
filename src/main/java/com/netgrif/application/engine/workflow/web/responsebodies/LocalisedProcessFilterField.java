package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.dataset.ProcessFilterField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedProcessFilterField extends LocalisedField {

    public LocalisedProcessFilterField(ProcessFilterField field, Locale locale) {
        super(field, locale);
        setComponent(new Component("string_query"));
    }
}
