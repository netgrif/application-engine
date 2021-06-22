package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedAssignTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedAssignTaskEventOutcome(AssignTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
