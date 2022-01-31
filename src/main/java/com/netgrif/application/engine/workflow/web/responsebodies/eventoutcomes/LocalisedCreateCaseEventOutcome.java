package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedCaseEventOutcome;

import java.util.Locale;

public class LocalisedCreateCaseEventOutcome extends LocalisedCaseEventOutcome {

    public LocalisedCreateCaseEventOutcome(CreateCaseEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
