package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.localised.LocalisedDelegateTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class DelegateTaskEventOutcome extends TaskEventOutcome{

    public DelegateTaskEventOutcome() {
    }

    public DelegateTaskEventOutcome(Task task) {
        super(task);
    }

    @Override
    public LocalisedDelegateTaskEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedDelegateTaskEventOutcome(this, locale);
    }
}
