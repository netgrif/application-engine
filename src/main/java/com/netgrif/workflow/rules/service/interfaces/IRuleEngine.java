package com.netgrif.workflow.rules.service.interfaces;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.facts.CaseCreatedFact;
import com.netgrif.workflow.rules.domain.facts.NetImportedFact;
import com.netgrif.workflow.rules.domain.facts.TransitionEventFact;
import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IRuleEngine {

    void evaluateRules(Case useCase, CaseCreatedFact caseCreatedFact);

    void evaluateRules(Case useCase, TransitionEventFact transitionEventFact);

    void evaluateRules(PetriNet petriNet, NetImportedFact fact);
}
