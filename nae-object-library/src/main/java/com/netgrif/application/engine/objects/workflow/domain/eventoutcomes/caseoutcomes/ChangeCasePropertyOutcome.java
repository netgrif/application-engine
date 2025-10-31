package com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.EventOutcome;

import java.util.List;

public class ChangeCasePropertyOutcome extends CaseEventOutcome {

    public ChangeCasePropertyOutcome() {
        super();
    }

    public ChangeCasePropertyOutcome(Case aCase) {
        super(aCase);
    }

    public ChangeCasePropertyOutcome(Case aCase, List<EventOutcome> outcomes) {
        super(aCase, outcomes);
    }

    public ChangeCasePropertyOutcome(I18nString message, Case aCase) {
        super(message, aCase);
    }

    public ChangeCasePropertyOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase) {
        super(message, outcomes, aCase);
    }
}
