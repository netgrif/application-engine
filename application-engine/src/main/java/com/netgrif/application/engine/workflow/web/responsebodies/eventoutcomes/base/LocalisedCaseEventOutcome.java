package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.application.engine.objects.dto.response.workflow.CaseDto;
import com.netgrif.application.engine.objects.dto.response.petrinet.PetriNetReferenceDto;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CaseEventOutcome;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Locale;

public abstract class LocalisedCaseEventOutcome extends LocalisedPetriNetEventOutcome {

    @Getter
    @Setter
    private CaseDto aCase;

    protected LocalisedCaseEventOutcome(CaseEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.aCase = CaseDto.fromCase(outcome.getCase(), locale);
    }

    protected LocalisedCaseEventOutcome(String message, List<LocalisedEventOutcome> outcomes, Locale locale, Case aCase) {
        super(message, outcomes, PetriNetReferenceDto.fromPetriNet(aCase.getPetriNet(), locale));
        this.aCase = CaseDto.fromCase(aCase, locale);
    }
}
