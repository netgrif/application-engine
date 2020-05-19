package com.netgrif.workflow.rules.domain.scheduled;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class RuleEvaluationJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationJob.class);


    public void execute(JobExecutionContext context) throws JobExecutionException {
        Object o = context.getJobDetail().getJobDataMap().get("id");
        log.info("Job executed " + o);
    }
}