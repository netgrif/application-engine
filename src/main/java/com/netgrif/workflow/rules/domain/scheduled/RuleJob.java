package com.netgrif.workflow.rules.domain.scheduled;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class RuleJob implements Job {

    public static final String RULE_IDENTIFIER = "ruleIdentifier";

    private static final Logger log = LoggerFactory.getLogger(RuleJob.class);

    public boolean validate(JobExecutionContext context) {
        return getInstanceId(context) != null && getRuleIdentifier(context) != null;
    }

    public abstract String getInstanceId(JobExecutionContext context);

    public String getRuleIdentifier(JobExecutionContext context) {
        return (String) context.getJobDetail().getJobDataMap().get(RULE_IDENTIFIER);
    }
}