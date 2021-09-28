package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedDelegateTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedDelegateTaskEventOutcome(DelegateTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
