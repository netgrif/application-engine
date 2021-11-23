package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedFinishTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedFinishTaskEventOutcome(FinishTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
