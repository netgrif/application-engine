package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.eventoutcomes.caseoutcomes.CaseEventOutcome;

import java.util.List;
import java.util.Locale;

public abstract class LocalisedCaseEventOutcome extends LocalisedPetriNetEventOutcome {

    private Case aCase;

    protected LocalisedCaseEventOutcome(CaseEventOutcome outcome, Locale locale) {
        super(outcome, locale, new PetriNetReference(outcome.getACase().getPetriNet(), locale));
        this.aCase = outcome.getACase();
    }

    protected LocalisedCaseEventOutcome(String message, List<LocalisedEventOutcome> outcomes, Locale locale, Case aCase) {
        super(message, outcomes, new PetriNetReference(aCase.getPetriNet(), locale));
        this.aCase = aCase;
    }

    public Case getaCase() {
        return aCase;
    }

    public void setaCase(Case aCase) {
        this.aCase = aCase;
    }
}
