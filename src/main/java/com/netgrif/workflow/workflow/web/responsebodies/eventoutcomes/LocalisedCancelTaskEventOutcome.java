package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

public class LocalisedCancelTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedCancelTaskEventOutcome(CancelTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
