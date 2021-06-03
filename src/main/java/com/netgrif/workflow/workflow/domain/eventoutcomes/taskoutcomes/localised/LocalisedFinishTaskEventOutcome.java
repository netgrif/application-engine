package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.localised;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedFinishTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedFinishTaskEventOutcome(FinishTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
