package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.localised.LocalisedAssignTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class AssignTaskEventOutcome extends TaskEventOutcome{

    public AssignTaskEventOutcome() {
    }

    public AssignTaskEventOutcome(Task task  ) {
        super(task);
    }

    @Override
    public LocalisedAssignTaskEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedAssignTaskEventOutcome(this, locale);
    }
}
