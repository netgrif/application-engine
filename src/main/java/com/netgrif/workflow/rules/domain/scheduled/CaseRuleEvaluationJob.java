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

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String caseId = getInstanceId(context);
        String ruleIdentifier = getRuleIdentifier(context);

        if (!validate(context)) {
            log.warn("Job does not have caseId or ruleIdentifier! " + caseId + ", " + ruleIdentifier);
            return;
        }

        log.info("Executing CaseRuleEvaluationJob for case " + caseId + " of rule " + ruleIdentifier);
        try {
            Case useCase = workflowService.findOne(caseId);
            ruleEngine.evaluateRules(useCase, new ScheduledRuleFact(caseId, ruleIdentifier));
            workflowService.save(useCase);
        } catch (Exception e) {
            log.error("Failed scheduled rule evaluation", e);
            throw new JobExecutionException(e);
        }
    }

    @Override
    public String getInstanceId(JobExecutionContext context) {
        return (String) context.getJobDetail().getJobDataMap().get(CASE_ID);
    }
}