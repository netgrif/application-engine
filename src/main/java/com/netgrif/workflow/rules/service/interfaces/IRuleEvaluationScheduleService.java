package com.netgrif.workflow.rules.service.interfaces;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.service.throwable.RuleEvaluationScheduleException;
import com.netgrif.workflow.workflow.domain.Case;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IRuleEvaluationScheduleService {

    void scheduleRuleEvaluationForCase(Case useCase, String ruleIdentifier, ScheduleBuilder<Trigger> trigger) throws RuleEvaluationScheduleException;

    void scheduleRuleEvaluationForCase(Case useCase, List<String> ruleIdentifiers, ScheduleBuilder<Trigger> trigger) throws RuleEvaluationScheduleException;

    void scheduleRuleEvaluationForNet(PetriNet petriNet, String ruleIdentifier, ScheduleBuilder<Trigger> trigger) throws RuleEvaluationScheduleException;

    void scheduleRuleEvaluationForNet(PetriNet petriNet, List<String> ruleIdentifiers, ScheduleBuilder<Trigger> trigger) throws RuleEvaluationScheduleException;
}
