package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedCaseEventOutcome;

import java.util.Locale;

public class LocalisedCreateCaseEventOutcome extends LocalisedCaseEventOutcome {

    public LocalisedCreateCaseEventOutcome(CreateCaseEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
