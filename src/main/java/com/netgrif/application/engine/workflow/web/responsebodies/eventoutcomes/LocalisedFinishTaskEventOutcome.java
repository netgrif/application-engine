package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;
import lombok.Getter;

import java.util.Locale;

public class LocalisedFinishTaskEventOutcome extends LocalisedTaskEventOutcome {

    @Getter
    protected Boolean isTaskStillExecutable;

    public LocalisedFinishTaskEventOutcome(FinishTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        if (outcome != null) {
            this.isTaskStillExecutable = outcome.isTaskStillExecutable();
        }
    }
}
