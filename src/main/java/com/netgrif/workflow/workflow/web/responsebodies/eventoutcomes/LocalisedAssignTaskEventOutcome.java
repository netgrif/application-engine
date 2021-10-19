package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

public class LocalisedAssignTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedAssignTaskEventOutcome(AssignTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
