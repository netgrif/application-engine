package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedCaseEventOutcome;

import java.util.Locale;

public class LocalisedDeleteCaseEventOutcome extends LocalisedCaseEventOutcome {

    public LocalisedDeleteCaseEventOutcome(DeleteCaseEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
