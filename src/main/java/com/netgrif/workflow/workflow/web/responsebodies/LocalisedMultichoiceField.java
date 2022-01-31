package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceField;
import lombok.Data;

import java.util.*;

@Data
public class LocalisedMultichoiceField extends LocalisedChoiceField {

    public LocalisedMultichoiceField(MultichoiceField field, Locale locale) {
        super(field, locale);
        this.setValue(new LinkedList<>());
        Collection<I18nString> values = field.getValue();
        if (values != null) {
            for (I18nString value : values) {
                ((List) this.getValue()).add(value.getTranslation(locale));
            }
        }
    }
}
