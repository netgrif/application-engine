package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedCreateCaseEventOutcome extends LocalisedEventOutcome {

    private Case aCase;

    public LocalisedCreateCaseEventOutcome(CreateCaseEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.aCase = outcome.getACase();
    }
}
