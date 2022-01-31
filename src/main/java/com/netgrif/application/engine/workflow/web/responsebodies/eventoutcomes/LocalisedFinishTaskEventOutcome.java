package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedFinishTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedFinishTaskEventOutcome(FinishTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
