//package com.netgrif.application.engine.rules.service.interfaces;
//
//import com.netgrif.adapter.petrinet.domain.PetriNet;
//import com.netgrif.application.engine.rules.domain.scheduled.ScheduleOutcome;
//import com.netgrif.application.engine.rules.service.throwable.RuleEvaluationScheduleException;
//import com.netgrif.adapter.workflow.domain.Case;
//import org.quartz.Trigger;
//import org.quartz.TriggerBuilder;
//
//import java.util.List;
//import java.util.Map;
//
//public interface IRuleEvaluationScheduleService {
//
//    ScheduleOutcome scheduleRuleEvaluationForCase(Case useCase, String ruleIdentifier, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;
//
//    Map<String, ScheduleOutcome> scheduleRuleEvaluationForCase(Case useCase, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;
//
//    ScheduleOutcome scheduleRuleEvaluationForNet(PetriNet petriNet, String ruleIdentifier, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;
//
//    Map<String, ScheduleOutcome> scheduleRuleEvaluationForNet(PetriNet petriNet, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;
//}
