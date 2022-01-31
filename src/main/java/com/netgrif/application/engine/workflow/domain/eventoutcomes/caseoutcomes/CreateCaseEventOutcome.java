package com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;

import java.util.List;

public class CreateCaseEventOutcome extends CaseEventOutcome {

    public CreateCaseEventOutcome() {
        super();
    }

    public CreateCaseEventOutcome(Case aCase) {
        super(aCase);
    }

    public CreateCaseEventOutcome(Case aCase, List<EventOutcome> outcomes) {
        super(aCase, outcomes);
    }

    public CreateCaseEventOutcome(I18nString message, Case aCase) {
        super(message, aCase);
    }

    public CreateCaseEventOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase) {
        super(message, outcomes, aCase);
    }
}
