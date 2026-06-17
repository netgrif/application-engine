package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.ChangeCasePropertyOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedCaseEventOutcome;

import java.util.Locale;

public class LocalisedChangeCasePropertyOutcome extends LocalisedCaseEventOutcome {

    public LocalisedChangeCasePropertyOutcome(ChangeCasePropertyOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
