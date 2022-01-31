package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedDelegateTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedDelegateTaskEventOutcome(DelegateTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
