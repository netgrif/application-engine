package com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ProcessEventOutcome;

import java.util.List;

public abstract class CaseEventOutcome extends ProcessEventOutcome {

    private Case aCase;

    protected CaseEventOutcome() {
        super();
    }

    protected CaseEventOutcome(Case aCase) {
        this.aCase = aCase;
    }

    protected CaseEventOutcome(Case aCase, List<EventOutcome> outcomes) {
        this(aCase);
        this.setOutcomes(outcomes);
    }

    protected CaseEventOutcome(I18nString message, Case aCase) {
        super(message, null, null);
        this.aCase = aCase;
    }

    protected CaseEventOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase) {
        super(message, outcomes, null, null);
        this.aCase = aCase;
    }

    public Case getCase() {
        return aCase;
    }

    public void setCase(Case aCase) {
        this.aCase = aCase;
    }
}
