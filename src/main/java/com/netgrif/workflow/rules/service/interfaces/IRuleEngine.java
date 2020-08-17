package com.netgrif.workflow.rules.service.interfaces;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.facts.CaseCreatedFact;
import com.netgrif.workflow.rules.domain.facts.NetImportedFact;
import com.netgrif.workflow.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.workflow.rules.domain.facts.TransitionEventFact;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.stereotype.Service;

@Service
public interface IRuleEngine {

    void evaluateRules(Case useCase, CaseCreatedFact caseCreatedFact);

    void evaluateRules(Case useCase, Task task, TransitionEventFact transitionEventFact);

    void evaluateRules(Case useCase, ScheduledRuleFact scheduledRuleFact);

    void evaluateRules(PetriNet petriNet, NetImportedFact fact);

    void evaluateRules(PetriNet petriNet, ScheduledRuleFact scheduledRuleFact);
}
