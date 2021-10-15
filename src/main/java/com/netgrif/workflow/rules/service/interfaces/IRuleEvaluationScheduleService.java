package com.netgrif.workflow.rules.service.interfaces;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.scheduled.ScheduleOutcome;
import com.netgrif.workflow.rules.service.throwable.RuleEvaluationScheduleException;
import com.netgrif.workflow.workflow.domain.Case;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.List;
import java.util.Map;

public interface IRuleEvaluationScheduleService {

    ScheduleOutcome scheduleRuleEvaluationForCase(Case useCase, String ruleIdentifier, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;

    Map<String, ScheduleOutcome> scheduleRuleEvaluationForCase(Case useCase, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;

    ScheduleOutcome scheduleRuleEvaluationForNet(PetriNet petriNet, String ruleIdentifier, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;

    Map<String, ScheduleOutcome> scheduleRuleEvaluationForNet(PetriNet petriNet, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;
}
