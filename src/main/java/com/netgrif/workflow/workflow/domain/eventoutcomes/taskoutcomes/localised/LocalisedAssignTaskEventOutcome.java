package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.localised;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedAssignTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedAssignTaskEventOutcome(AssignTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
