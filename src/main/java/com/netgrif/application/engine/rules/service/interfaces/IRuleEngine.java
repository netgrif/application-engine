package com.netgrif.application.engine.rules.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
import com.netgrif.application.engine.rules.domain.facts.ProcessImportedFact;
import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.application.engine.rules.domain.facts.TransitionEventFact;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;

public interface IRuleEngine {

    int evaluateRules(Case useCase, CaseCreatedFact caseCreatedFact);

    int evaluateRules(Case useCase, Task task, TransitionEventFact transitionEventFact);

    int evaluateRules(Case useOrTemplateCase, ScheduledRuleFact scheduledRuleFact);

    int evaluateRules(Case templateCase, ProcessImportedFact fact);
}
