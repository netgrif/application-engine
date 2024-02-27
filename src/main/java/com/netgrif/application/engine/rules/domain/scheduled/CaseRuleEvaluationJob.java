package com.netgrif.application.engine.rules.domain.scheduled;

import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CaseRuleEvaluationJob extends RuleJob {

    public static final String CASE_ID = "caseId";

    private static final Logger log = LoggerFactory.getLogger(CaseRuleEvaluationJob.class);

    private IRuleEngine ruleEngine;

    private IWorkflowService workflowService;

    @Autowired
    public void setRuleEngine(IRuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    @Autowired
    public void setWorkflowService(IWorkflowService workflowService) {
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