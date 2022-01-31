package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CaseEventOutcome;

import java.util.List;
import java.util.Locale;

public abstract class LocalisedCaseEventOutcome extends LocalisedPetriNetEventOutcome {

    private Case aCase;

    protected LocalisedCaseEventOutcome(CaseEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.aCase = outcome.getCase();
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
