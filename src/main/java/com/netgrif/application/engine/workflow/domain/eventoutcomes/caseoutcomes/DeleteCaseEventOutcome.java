package com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class DeleteCaseEventOutcome extends CaseEventOutcome {

    public DeleteCaseEventOutcome(Case aCase) {
        super(aCase);
    }

    public DeleteCaseEventOutcome(Case aCase, List<EventOutcome> outcomes) {
        super(aCase, outcomes);
    }

    public DeleteCaseEventOutcome(I18nString message,Case aCase) {
        super(message,aCase);
    }

    public DeleteCaseEventOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase) {
        super(message, outcomes, aCase);
    }
}
