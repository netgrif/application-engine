package com.netgrif.workflow.rules.domain.scheduled;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class RuleJob implements Job {

    public static final String RULE_IDENTIFIER = "ruleIdentifier";

    private static final Logger log = LoggerFactory.getLogger(RuleJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String instanceId = getInstanceId(context);
        String ruleIdentifier = getRuleIdentifier(context);

        if (!validate(context)) {
            log.warn("Job does not have instanceId or ruleIdentifier! " + instanceId + ", " + ruleIdentifier);
            return;
        }

        try {
            doExecute(context);
        } catch (Exception e) {
            log.error("Failed scheduled rule evaluation", e);
            throw new JobExecutionException(e);
        }
    }

    public boolean validate(JobExecutionContext context) {
        return getInstanceId(context) != null && getRuleIdentifier(context) != null;
    }

    public String getRuleIdentifier(JobExecutionContext context) {
        return (String) context.getJobDetail().getJobDataMap().get(RULE_IDENTIFIER);
    }

    public abstract void doExecute(JobExecutionContext context);

    public abstract String getInstanceId(JobExecutionContext context);
}