package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import com.netgrif.workflow.rules.domain.scheduled.CaseRuleEvaluationJob;
import com.netgrif.workflow.rules.domain.scheduled.PetriNetRuleEvaluationJob;
import com.netgrif.workflow.rules.domain.scheduled.RuleJob;
import com.netgrif.workflow.rules.domain.scheduled.ScheduleOutcome;
import com.netgrif.workflow.rules.service.interfaces.IRuleEvaluationScheduleService;
import com.netgrif.workflow.rules.service.throwable.RuleEvaluationScheduleException;
import com.netgrif.workflow.workflow.domain.Case;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RuleEvaluationScheduleService implements IRuleEvaluationScheduleService {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationScheduleService.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private RuleRepository ruleRepository;

    @Override
    public ScheduleOutcome scheduleRuleEvaluationForCase(Case useCase, String ruleIdentifier, TriggerBuilder<? extends Trigger> triggerBuilder) throws RuleEvaluationScheduleException {
        return scheduleRuleEvaluationForCase(useCase, Collections.singletonList(ruleIdentifier), triggerBuilder).values().iterator().next();
    }

    @Override
    public Map<String, ScheduleOutcome> scheduleRuleEvaluationForCase(Case useCase, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> triggerBuilder) throws RuleEvaluationScheduleException {
        Map<String, String> data = new HashMap<>();
        data.put(CaseRuleEvaluationJob.CASE_ID, useCase.getStringId());
        return scheduleRuleEvaluation(useCase.getStringId(), data, ruleIdentifiers, triggerBuilder);
    }

    @Override
    public ScheduleOutcome scheduleRuleEvaluationForNet(PetriNet petriNet, String ruleIdentifier, TriggerBuilder<? extends Trigger> triggerBuilder) throws RuleEvaluationScheduleException {
        return scheduleRuleEvaluationForNet(petriNet, Collections.singletonList(ruleIdentifier), triggerBuilder).values().iterator().next();
    }

    @Override
    public Map<String, ScheduleOutcome> scheduleRuleEvaluationForNet(PetriNet petriNet, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> triggerBuilder) throws RuleEvaluationScheduleException {
        Map<String, String> data = new HashMap<>();
        data.put(PetriNetRuleEvaluationJob.NET_ID, petriNet.getStringId());
        return scheduleRuleEvaluation(petriNet.getStringId(), data, ruleIdentifiers, triggerBuilder);
    }

    private void schedule(String stringId, String ruleId, JobDetail jobDetail, Trigger trigger) throws RuleEvaluationScheduleException {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Failed to schedule Rule evaluation for " + stringId + " of rule " + ruleId, e);
            throw new RuleEvaluationScheduleException(e);
        }
    }

    public Map<String, ScheduleOutcome> scheduleRuleEvaluation(String instanceId, Map<String, String> jobData, List<String> ruleIdentifiers, TriggerBuilder<? extends Trigger> triggerBuilder) throws RuleEvaluationScheduleException {
        List<StoredRule> storedRules = ruleRepository.findByIdentifierIn(ruleIdentifiers);

        Map<String, ScheduleOutcome> outcomes = new HashMap<>();
        for (StoredRule rule : storedRules) {
            log.info("Scheduling rule eval job for " + instanceId + " " + rule.getIdentifier());

            JobDetail jobDetail = buildJobDetail(instanceId, rule, CaseRuleEvaluationJob.class);
            Trigger trigger = buildTrigger(instanceId, triggerBuilder, jobDetail);
            jobDetail.getJobDataMap().putAll(jobData);

            schedule(instanceId, rule.getStringId(), jobDetail, trigger);
            outcomes.put(rule.getIdentifier(), new ScheduleOutcome(jobDetail, trigger));
        }

        return outcomes;
    }

    protected  <T extends Job> JobDetail buildJobDetail(String instanceStringId, StoredRule rule, Class<T> type) {
        JobDetail jobDetail = JobBuilder.newJob().ofType(type)
                .storeDurably(false)
                .withIdentity(instanceStringId + "-" + rule.getStringId() + "-" + UUID.randomUUID().toString())
                .withDescription("Scheduled eval for " + instanceStringId + " of rule " + rule.getStringId()).build();
        jobDetail.getJobDataMap().put(RuleJob.RULE_IDENTIFIER, rule.getIdentifier());
        return jobDetail;
    }

    protected Trigger buildTrigger(String instanceStringId, TriggerBuilder<? extends Trigger> triggerBuilder, JobDetail jobDetail) {
        return triggerBuilder
                .withIdentity("trigger" + instanceStringId + "-" + jobDetail.getKey().toString() + "-" + UUID.randomUUID().toString())
                .withDescription("Trigger for " + instanceStringId + " for job " + jobDetail.getKey().toString())
                .build();
    }

}
