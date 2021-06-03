package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.localised.LocalisedCancelTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class CancelTaskEventOutcome extends TaskEventOutcome{

    public CancelTaskEventOutcome() {
    }

    public CancelTaskEventOutcome(Task task) {
        super(task);
    }

    @Override
    public LocalisedCancelTaskEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedCancelTaskEventOutcome(this, locale);
    }
}
