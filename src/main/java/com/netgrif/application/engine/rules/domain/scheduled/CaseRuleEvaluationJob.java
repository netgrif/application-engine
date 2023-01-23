package com.netgrif.application.engine.rules.domain.scheduled;

import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CaseRuleEvaluationJob extends RuleJob {

    public static final String CASE_ID = "caseId";

    private final IRuleEngine ruleEngine;

    private final IWorkflowService workflowService;

    public CaseRuleEvaluationJob(IRuleEngine ruleEngine, IWorkflowService workflowService) {
        this.ruleEngine = ruleEngine;
        this.workflowService = workflowService;
    }

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