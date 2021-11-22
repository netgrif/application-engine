package com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
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
