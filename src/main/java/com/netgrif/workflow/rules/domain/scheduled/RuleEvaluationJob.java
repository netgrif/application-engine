package com.netgrif.workflow.rules.domain.scheduled;

import com.netgrif.workflow.rules.domain.facts.CaseRuleEvaluation;
import com.netgrif.workflow.rules.service.RuleEngineSessionService;
import org.kie.api.runtime.KieSession;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class RuleEvaluationJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationJob.class);

    @Autowired
    private RuleEngineSessionService ruleEngine;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        KieSession session = ruleEngine.createNewSession();

        session.insert(new CaseRuleEvaluation("test", LocalDateTime.now()));

        session.dispose();
        log.info("Job executed");
    }
}