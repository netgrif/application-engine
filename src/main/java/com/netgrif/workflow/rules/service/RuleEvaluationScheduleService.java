package com.netgrif.workflow.rules.service;

import com.netgrif.workflow.rules.domain.RuleRepository;
import com.netgrif.workflow.rules.domain.StoredRule;
import com.netgrif.workflow.rules.domain.scheduled.CaseRuleEvaluationJob;
import com.netgrif.workflow.rules.service.interfaces.IRuleEvaluationScheduleService;
import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public void scheduleRuleEvaluationForCase(Case useCase, String ruleIdentifier) {
        scheduleRuleEvaluationForCase(useCase, Collections.singletonList(ruleIdentifier));
    }

    @Override
    public void scheduleRuleEvaluationForCase(Case useCase, List<String> ruleIdentifiers) {
        List<StoredRule> storedRules = ruleRepository.findByIdentifierIn(ruleIdentifiers);

        // TODO implement
        storedRules.forEach(rule -> {
            log.info("Scheduling rule eval job for " + useCase.getStringId() + " " + rule.getIdentifier());

            JobDetail jobDetail = JobBuilder.newJob().ofType(CaseRuleEvaluationJob.class)
                    .storeDurably(false)
                    .withIdentity(useCase.getStringId() + "-" + rule.getStringId() + "-" + UUID.randomUUID().toString())
                    .withDescription("Scheduled eval for " + useCase.getStringId() + " of rule " + rule.getStringId())
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail)
                    .withIdentity(useCase.getStringId() + "-" + rule.getStringId() + "-" + UUID.randomUUID().toString())
                    .withDescription("Rule eval trigger for " + useCase.getStringId() + " " + rule.getStringId())
                    .startAt(DateUtils.localDateTimeToDate(LocalDateTime.now().plusSeconds(10)))
                    .build();

            jobDetail.getJobDataMap().put("caseId", useCase.getStringId());
            jobDetail.getJobDataMap().put("ruleIdentifier", rule.getIdentifier());

            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                log.error("Failed to schedule Rule evaluation for " + useCase.getStringId() + " of rule " + rule.getStringId(), e);
            }
        });

    }

}
