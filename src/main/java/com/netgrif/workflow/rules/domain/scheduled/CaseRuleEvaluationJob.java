package com.netgrif.workflow.rules.domain.scheduled;

import com.netgrif.workflow.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.workflow.rules.service.interfaces.IRuleEngine;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CaseRuleEvaluationJob extends RuleJob {

    public static final String CASE_ID = "caseId";

    private static final Logger log = LoggerFactory.getLogger(CaseRuleEvaluationJob.class);

    @Autowired
    private IRuleEngine ruleEngine;

    @Autowired
    private IWorkflowService workflowService;

    @Override
    public void doExecute(JobExecutionContext context) {
        String caseId = getInstanceId(context);
        log.info("Executing CaseRuleEvaluationJob for case " + caseId + " of rule " + getRuleIdentifier(context));
        Case useCase = workflowService.findOne(caseId);
        ruleEngine.evaluateRules(useCase, new ScheduledRuleFact(caseId, getRuleIdentifier(context)));
    }

    @Override
    public String getInstanceId(JobExecutionContext context) {
        return (String) context.getJobDetail().getJobDataMap().get(CASE_ID);
    }
}