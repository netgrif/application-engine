package com.netgrif.workflow.rules.service.interfaces;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.facts.TransitionEvent;
import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.stereotype.Service;

@Service
public interface IRuleEngine {
    void evaluateRules(Case useCase);

    void evaluateRules(Case useCase, TransitionEvent transitionEvent);

    void evaluateRules(PetriNet petriNet);
}
