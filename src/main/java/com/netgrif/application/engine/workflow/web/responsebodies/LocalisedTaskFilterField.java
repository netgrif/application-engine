package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.TaskFilterField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedTaskFilterField extends LocalisedField {

    public LocalisedTaskFilterField(TaskFilterField field, Locale locale) {
        super(field, locale);
    }
}
