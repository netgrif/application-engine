package com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.localised.LocalisedDeleteCaseEventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class DeleteCaseEventOutcome extends EventOutcome {

    private String stringId;

    @Override
    public LocalisedDeleteCaseEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedDeleteCaseEventOutcome(this, locale);
    }
}
