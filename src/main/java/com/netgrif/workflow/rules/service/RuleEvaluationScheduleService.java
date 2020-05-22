package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import com.netgrif.workflow.rules.domain.scheduled.CaseRuleEvaluationJob;
import com.netgrif.workflow.rules.domain.scheduled.PetriNetRuleEvaluationJob;
import com.netgrif.workflow.rules.service.interfaces.IRuleEvaluationScheduleService;
import com.netgrif.workflow.rules.service.throwable.RuleEvaluationScheduleException;
import com.netgrif.workflow.workflow.domain.Case;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RuleEvaluationScheduleService implements IRuleEvaluationScheduleService {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationScheduleService.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private RuleRepository ruleRepository;

    @Override
    public void scheduleRuleEvaluationForCase(Case useCase, String ruleIdentifier, Trigger trigger) throws RuleEvaluationScheduleException {
        scheduleRuleEvaluationForCase(useCase, Collections.singletonList(ruleIdentifier), trigger);
    }

    @Override
    public void scheduleRuleEvaluationForCase(Case useCase, List<String> ruleIdentifiers, Trigger trigger) throws RuleEvaluationScheduleException {
        List<StoredRule> storedRules = ruleRepository.findByIdentifierIn(ruleIdentifiers);

        for (StoredRule rule : storedRules) {
            log.info("Scheduling rule eval job for " + useCase.getStringId() + " " + rule.getIdentifier());

            JobDetail jobDetail = jobDetailBuilder(useCase.getStringId(), rule, CaseRuleEvaluationJob.class);
            jobDetail.getJobDataMap().put("caseId", useCase.getStringId());

            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                log.error("Failed to schedule Rule evaluation for " + useCase.getStringId() + " of rule " + rule.getStringId(), e);
                throw new RuleEvaluationScheduleException(e);
            }
        }
    }

    @Override
    public void scheduleRuleEvaluationForNet(PetriNet petriNet, String ruleIdentifier, Trigger trigger) throws RuleEvaluationScheduleException {
        scheduleRuleEvaluationForNet(petriNet, Collections.singletonList(ruleIdentifier), trigger);
    }

    @Override
    public void scheduleRuleEvaluationForNet(PetriNet petriNet, List<String> ruleIdentifiers, Trigger trigger) throws RuleEvaluationScheduleException {
        List<StoredRule> storedRules = ruleRepository.findByIdentifierIn(ruleIdentifiers);

        for (StoredRule rule : storedRules) {
            log.info("Scheduling rule eval job for " + petriNet.getStringId() + " " + rule.getIdentifier());

            JobDetail jobDetail = jobDetailBuilder(petriNet.getStringId(), rule, PetriNetRuleEvaluationJob.class);
            jobDetail.getJobDataMap().put("netId", petriNet.getStringId());

            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                log.error("Failed to schedule Rule evaluation for " + petriNet.getStringId() + " of rule " + rule.getStringId(), e);
                throw new RuleEvaluationScheduleException(e);
            }
        }

    }

    private <T extends Job> JobDetail jobDetailBuilder(String instanceStringId, StoredRule rule, Class<T> type) {
        JobDetail jobDetail = JobBuilder.newJob().ofType(type)
                .storeDurably(false)
                .withIdentity(instanceStringId + "-" + rule.getStringId() + "-" + UUID.randomUUID().toString())
                .withDescription("Scheduled eval for " + instanceStringId + " of rule " + rule.getStringId()).build();
        jobDetail.getJobDataMap().put("ruleIdentifier", rule.getIdentifier());
        return jobDetail;
    }

}
