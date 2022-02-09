package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Data
@NoArgsConstructor
public class LocalisedChoiceField extends LocalisedField {

    protected List<String> choices;

    public LocalisedChoiceField(ChoiceField field, Locale locale) {
        super(field, locale);
        this.choices = new LinkedList<>();
        Set<I18nString> choices = field.getChoices();
        for (I18nString choice : choices) {
            this.choices.add(choice.getTranslation(locale));
        }
    }
}
