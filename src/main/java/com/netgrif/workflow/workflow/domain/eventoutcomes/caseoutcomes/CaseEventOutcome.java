package com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.PetriNetEventOutcome;

import java.util.List;

public abstract class CaseEventOutcome extends PetriNetEventOutcome {

    private Case aCase;

    protected CaseEventOutcome() {
        super();
    }

    protected CaseEventOutcome(Case aCase) {
        super(aCase.getPetriNet());
        this.aCase = aCase;
    }

    protected CaseEventOutcome(I18nString message, Case aCase) {
        super(message, aCase.getPetriNet());
        this.aCase = aCase;
    }

    protected CaseEventOutcome(I18nString message, List<EventOutcome> outcomes, Case aCase) {
        super(message, outcomes, aCase.getPetriNet());
        this.aCase = aCase;
    }

    public Case getACase() {
        return aCase;
    }

    public void setACase(Case aCase) {
        this.aCase = aCase;
        setNet(aCase.getPetriNet());
    }
}
