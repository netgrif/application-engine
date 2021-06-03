package com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.localised;

import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedDeleteCaseEventOutcome extends LocalisedEventOutcome {

    private String stringId;

    public LocalisedDeleteCaseEventOutcome(DeleteCaseEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.stringId = outcome.getStringId();
    }
}
