package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.localised.LocalisedFinishTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class FinishTaskEventOutcome extends TaskEventOutcome{

    public FinishTaskEventOutcome() {
    }

    public FinishTaskEventOutcome(Task task) {
        super(task);
    }

    @Override
    public LocalisedFinishTaskEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedFinishTaskEventOutcome(this, locale);
    }
}
