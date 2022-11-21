package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedCancelTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedCancelTaskEventOutcome(CancelTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
