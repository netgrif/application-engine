//package com.netgrif.application.engine.rules.service.interfaces;
//
//import com.netgrif.adapter.petrinet.domain.PetriNet;
//import com.netgrif.application.engine.rules.domain.facts.CaseCreatedFact;
//import com.netgrif.application.engine.rules.domain.facts.NetImportedFact;
//import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
//import com.netgrif.application.engine.rules.domain.facts.TransitionEventFact;
//import com.netgrif.adapter.workflow.domain.Case;
//import com.netgrif.adapter.workflow.domain.Task;
//
//public interface IRuleEngine {
//
//    int evaluateRules(Case useCase, CaseCreatedFact caseCreatedFact);
//
//    int evaluateRules(Case useCase, Task task, TransitionEventFact transitionEventFact);
//
//    int evaluateRules(Case useCase, ScheduledRuleFact scheduledRuleFact);
//
//    int evaluateRules(PetriNet petriNet, NetImportedFact fact);
//
//    int evaluateRules(PetriNet petriNet, ScheduledRuleFact scheduledRuleFact);
//}
