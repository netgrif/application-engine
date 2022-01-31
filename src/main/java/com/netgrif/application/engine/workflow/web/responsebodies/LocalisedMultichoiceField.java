package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceField;
import lombok.Data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
