package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedDelegateTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedDelegateTaskEventOutcome(DelegateTaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
    }
}
