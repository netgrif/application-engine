package com.netgrif.application.engine.rules.domain.scheduled;

import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.rules.domain.facts.ScheduledRuleFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.ITemplateCaseService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
// RuleJobs need autowired fields otherwise AutowiringSpringBeanJobFactory::createJobInstance will fail
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class ProcessRuleEvaluationJob extends RuleJob {

    public static final String NET_ID = "templateCaseId";

    @Autowired
    private IRuleEngine ruleEngine;

    @Autowired
    private ITemplateCaseService templateCaseService;

    @Override
    public void doExecute(JobExecutionContext context) {
        // todo 2026 toto asi bude rozjebane
        String templateCaseId = getInstanceId(context);
        log.info("Executing ProcessRuleEvaluationJob for template case [{}] of rule {}", templateCaseId, getRuleIdentifier(context));
        Case templateCase = templateCaseService.findOne(templateCaseId);
        ruleEngine.evaluateRules(templateCase, new ScheduledRuleFact(templateCaseId, getRuleIdentifier(context)));
    }

    @Override
    public String getInstanceId(JobExecutionContext context) {
        return (String) context.getJobDetail().getJobDataMap().get(NET_ID);
    }
}