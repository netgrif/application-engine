package com.netgrif.application.engine.cron.service;

import com.netgrif.application.engine.cron.domain.CreateCaseJob;
import com.netgrif.application.engine.cron.service.interfaces.ICronJobService;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CronJobService implements ICronJobService {

    public static final String GROUP_NAME = "nae";
    @Autowired
    Scheduler scheduler;

    @Override
    public void createJob(Case useCase) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(CreateCaseJob.class)
                .withIdentity(useCase.getStringId(), GROUP_NAME).build();
        Map<String, String> data = new HashMap<>();
        data.put(CreateCaseJob.CASE_ID, useCase.getStringId());
        job.getJobDataMap().putAll(data);

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(useCase.getFieldValue("cron_description").toString()))
                .forJob(job)
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    @Override
    public boolean deactivateJob(Case useCase) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(useCase.getStringId(), GROUP_NAME);
        if (scheduler.checkExists(jobKey)) {
            return scheduler.deleteJob(jobKey);
        }
        return true;
    }
}
