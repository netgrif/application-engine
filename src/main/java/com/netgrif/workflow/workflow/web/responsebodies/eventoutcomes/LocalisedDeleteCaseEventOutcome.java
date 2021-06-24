package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

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
