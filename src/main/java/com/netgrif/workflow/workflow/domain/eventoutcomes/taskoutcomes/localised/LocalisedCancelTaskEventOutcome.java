package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.localised;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedCancelTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedCancelTaskEventOutcome(CancelTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
