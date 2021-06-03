package com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.localised.LocalisedCreateCaseEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class CreateCaseEventOutcome extends EventOutcome {

    private Case aCase;

    @Override
    public LocalisedEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedCreateCaseEventOutcome(this, locale);
    }
}
