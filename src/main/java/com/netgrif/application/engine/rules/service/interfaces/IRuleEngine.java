package com.netgrif.application.engine.rules.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.domain.facts.NetImportedFact;
import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.application.engine.rules.domain.facts.TransitionEventFact;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;

public interface IRuleEngine {

    void evaluateRules(Case useCase, CaseCreatedFact caseCreatedFact);

    void evaluateRules(Case useCase, Task task, TransitionEventFact transitionEventFact);

    void evaluateRules(Case useCase, ScheduledRuleFact scheduledRuleFact);

    void evaluateRules(PetriNet petriNet, NetImportedFact fact);

    void evaluateRules(PetriNet petriNet, ScheduledRuleFact scheduledRuleFact);
}
