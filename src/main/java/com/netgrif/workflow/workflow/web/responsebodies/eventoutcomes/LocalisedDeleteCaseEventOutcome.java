package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedCaseEventOutcome;

import java.util.Locale;

public class LocalisedDeleteCaseEventOutcome extends LocalisedCaseEventOutcome {

    public LocalisedDeleteCaseEventOutcome(DeleteCaseEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
