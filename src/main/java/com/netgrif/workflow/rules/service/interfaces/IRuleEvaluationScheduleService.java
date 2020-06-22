package com.netgrif.workflow.rules.service.interfaces;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.service.throwable.RuleEvaluationScheduleException;
import com.netgrif.workflow.workflow.domain.Case;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IRuleEvaluationScheduleService {

    void scheduleRuleEvaluationForCase(Case useCase, String ruleIdentifier, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;

    void scheduleRuleEvaluationForCase(Case useCase, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;

    void scheduleRuleEvaluationForNet(PetriNet petriNet, String ruleIdentifier, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;

    void scheduleRuleEvaluationForNet(PetriNet petriNet, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> trigger) throws RuleEvaluationScheduleException;
}
