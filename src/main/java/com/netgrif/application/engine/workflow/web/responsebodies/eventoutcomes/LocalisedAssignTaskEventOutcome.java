package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedAssignTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedAssignTaskEventOutcome(AssignTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
