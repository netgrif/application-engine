package com.netgrif.application.engine.workflow.scheduler;

import com.netgrif.application.engine.configuration.properties.TimeTriggerProperties;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TimeTriggerSchedulingService {

    private static final String GROUP = "timeTriggers";
    private static final String DUE_AT_KEY = "dueAt";
    private static final String ATTEMPTS_KEY = "attempts";
    private static final int MAX_SCHEDULING_ATTEMPTS = 3;
    private static final long SCHEDULING_RETRY_DELAY_MS = 50;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private TimeTriggerMetrics metrics;

    @Autowired
    private TimeTriggerProperties timeTriggerProperties;

    public boolean schedule(Task task, LocalDateTime startDate) {
        long dueAt = toEpochMillis(startDate);
        JobDetail jobDetail = buildJobDetail(task, dueAt);
        SimpleTrigger trigger = buildTrigger(task.getStringId(), dueAt);

        try {
            if (scheduleWithRetry(jobDetail, trigger)) {
                metrics.scheduled();
                return true;
            }
            metrics.scheduleFailed();
            return false;
        } catch (ObjectAlreadyExistsException e) {
            log.info("Time trigger for task [{}] is already scheduled", task.getStringId());
            return true;
        }
    }

    public boolean reschedule(Task task, LocalDateTime startDate) {
        String taskId = task.getStringId();
        long dueAt = toEpochMillis(startDate);
        JobDetail jobDetail = buildJobDetail(task, dueAt);
        SimpleTrigger trigger = buildTrigger(taskId, dueAt);

        try {
            deleteRetryJobs(taskId);
        } catch (SchedulerException e) {
            log.error("Failed to delete pending time trigger redeliveries of task [{}] in case [{}] before reschedule", taskId, task.getCaseId(), e);
            metrics.scheduleFailed();
            return false;
        }

        try {
            scheduler.addJob(jobDetail, true, true);
        } catch (SchedulerException e) {
            log.error("Failed to replace time trigger job data for task [{}] in case [{}]", taskId, task.getCaseId(), e);
            metrics.scheduleFailed();
            return false;
        }

        try {
            if (scheduler.rescheduleJob(new TriggerKey(taskId, GROUP), trigger) == null) {
                scheduler.deleteJob(jobDetail.getKey());
                scheduler.scheduleJob(jobDetail, trigger);
            }
            metrics.scheduled();
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to reschedule time trigger for task [{}] in case [{}]", taskId, task.getCaseId(), e);
            metrics.scheduleFailed();
            return false;
        }
    }

    private long toEpochMillis(LocalDateTime startDate) {
        return startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private JobDetail buildJobDetail(Task task, long dueAt) {
        return jobBuilder(task.getStringId(), task.getCaseId(), task.getStringId(), task.getTransitionId(), dueAt).build();
    }

    private JobBuilder jobBuilder(String jobName, String caseId, String taskId, String transitionId, long dueAt) {
        return JobBuilder.newJob(TimeTriggerJob.class)
                .withIdentity(new JobKey(jobName, GROUP))
                .storeDurably(false)
                .requestRecovery(true)
                .usingJobData("caseId", caseId)
                .usingJobData("taskId", taskId)
                .usingJobData("transitionId", transitionId)
                .usingJobData(DUE_AT_KEY, dueAt);
    }

    private SimpleTrigger buildTrigger(String jobName, long startAtMillis) {
        return TriggerBuilder.newTrigger()
                .withIdentity(new TriggerKey(jobName, GROUP))
                .forJob(new JobKey(jobName, GROUP))
                .startAt(new Date(startAtMillis))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }

    public TimeTriggerRedeliveryResult scheduleRedelivery(String caseId, String taskId, String transitionId, long dueAt, int attempt) {
        try {
            if (isSupersededByReschedule(taskId, dueAt)) {
                log.info("Time trigger redelivery for task [{}] of transition [{}] in case [{}] superseded by a reschedule (attempt [{}])", taskId, transitionId, caseId, attempt);
                return TimeTriggerRedeliveryResult.SUPERSEDED;
            }
        } catch (SchedulerException e) {
            log.error("Failed to read time trigger job of task [{}] in case [{}] before redelivery", taskId, caseId, e);
            return TimeTriggerRedeliveryResult.FAILED;
        }

        String retryKey = taskId + "#r" + attempt;
        long delay = timeTriggerProperties.getRedeliver().getDelay().toMillis();
        JobDetail jobDetail = jobBuilder(retryKey, caseId, taskId, transitionId, dueAt)
                .usingJobData(ATTEMPTS_KEY, attempt)
                .build();
        SimpleTrigger trigger = buildTrigger(retryKey, System.currentTimeMillis() + delay);
        try {
            return scheduleWithRetry(jobDetail, trigger) ? TimeTriggerRedeliveryResult.SCHEDULED : TimeTriggerRedeliveryResult.FAILED;
        } catch (ObjectAlreadyExistsException e) {
            log.info("Time trigger redelivery for task [{}] of transition [{}] in case [{}] already scheduled (attempt [{}])", taskId, transitionId, caseId, attempt);
            return TimeTriggerRedeliveryResult.SCHEDULED;
        }
    }

    public boolean isSuperseded(String taskId, long dueAt) {
        try {
            return isSupersededByReschedule(taskId, dueAt);
        } catch (SchedulerException e) {
            log.warn("Failed to read time trigger job of task [{}] to check for a reschedule, assuming it is not superseded", taskId, e);
            return false;
        }
    }

    private boolean isSupersededByReschedule(String taskId, long dueAt) throws SchedulerException {
        JobDetail canonicalJob = scheduler.getJobDetail(new JobKey(taskId, GROUP));
        if (canonicalJob == null) {
            return false;
        }
        JobDataMap canonicalData = canonicalJob.getJobDataMap();
        return !canonicalData.containsKey(DUE_AT_KEY) || canonicalData.getLong(DUE_AT_KEY) != dueAt;
    }

    private void deleteRetryJobs(String taskId) throws SchedulerException {
        int maxAttempts = timeTriggerProperties.getRedeliver().getMaxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            scheduler.deleteJob(new JobKey(taskId + "#r" + attempt, GROUP));
        }
    }

    private boolean scheduleWithRetry(JobDetail jobDetail, Trigger trigger) throws ObjectAlreadyExistsException {
        String taskId = jobDetail.getJobDataMap().getString("taskId");
        String caseId = jobDetail.getJobDataMap().getString("caseId");
        for (int attempt = 1; attempt <= MAX_SCHEDULING_ATTEMPTS; attempt++) {
            try {
                scheduler.scheduleJob(jobDetail, trigger);
                return true;
            } catch (ObjectAlreadyExistsException e) {
                throw e;
            } catch (SchedulerException e) {
                if (attempt == MAX_SCHEDULING_ATTEMPTS) {
                    log.error("Failed to schedule time trigger for task [{}] in case [{}] after [{}] attempts", taskId, caseId, attempt, e);
                    return false;
                }
                try {
                    Thread.sleep(SCHEDULING_RETRY_DELAY_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    log.warn("Time trigger scheduling interrupted for task [{}] in case [{}] after [{}] attempts", taskId, caseId, attempt, interruptedException);
                    return false;
                }
            }
        }
        return false;
    }

    public void unschedule(String taskId) {
        unscheduleAll(List.of(taskId));
    }

    public void unscheduleAll(Collection<String> taskIds) {
        if (taskIds.isEmpty()) {
            return;
        }
        int maxAttempts = timeTriggerProperties.getRedeliver().getMaxAttempts();
        List<JobKey> keys = new ArrayList<>();
        for (String taskId : taskIds) {
            keys.add(new JobKey(taskId, GROUP));
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                keys.add(new JobKey(taskId + "#r" + attempt, GROUP));
            }
        }
        try {
            scheduler.deleteJobs(keys);
        } catch (SchedulerException e) {
            log.error("Failed to unschedule time triggers for tasks {}", taskIds, e);
        }
    }
}
