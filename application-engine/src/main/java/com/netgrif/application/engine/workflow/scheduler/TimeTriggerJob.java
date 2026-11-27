package com.netgrif.application.engine.workflow.scheduler;

import com.netgrif.application.engine.configuration.properties.TimeTriggerProperties;
import com.netgrif.application.engine.workflow.service.TimeTriggerOutcome;
import com.netgrif.application.engine.workflow.service.TimeTriggerRetryableException;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DisallowConcurrentExecution
public class TimeTriggerJob implements Job {

    private static final String DUE_AT_KEY = "dueAt";
    private static final String ATTEMPTS_KEY = "attempts";

    @Autowired
    private ITaskService taskService;

    @Autowired
    private TimeTriggerSchedulingService schedulingService;

    @Autowired
    private TimeTriggerProperties timeTriggerProperties;

    @Autowired
    private TimeTriggerMetrics metrics;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        String caseId = data.getString("caseId");
        String taskId = data.getString("taskId");
        String transitionId = data.getString("transitionId");

        if (isSupersededRedelivery(data, taskId)) {
            log.info("Time trigger redelivery for task [{}] of transition [{}] in case [{}] skipped, the time trigger was rescheduled", taskId, transitionId, caseId);
            return;
        }

        try {
            Long dueAt = data.containsKey(DUE_AT_KEY) ? data.getLong(DUE_AT_KEY) : null;
            TimeTriggerOutcome outcome = taskService.executeTimeTrigger(taskId, dueAt);
            metrics.executed(outcome);
            if (outcome == TimeTriggerOutcome.FIRED) {
                if (data.containsKey(DUE_AT_KEY)) {
                    long lateness = context.getFireTime().getTime() - data.getLongValue(DUE_AT_KEY);
                    if (lateness >= 0) {
                        metrics.lateness(lateness);
                    } else {
                        log.debug("Skipping negative time trigger lateness [{}] ms for task [{}]", lateness, taskId);
                    }
                }
            } else if (outcome == TimeTriggerOutcome.RESUME_FAILED) {
                metrics.failed();
            }
        } catch (TimeTriggerRetryableException e) {
            redeliver(data, caseId, taskId, transitionId);
        } catch (RuntimeException e) {
            metrics.failed();
            log.error("Time trigger execution failed for task [{}] of transition [{}] in case [{}]", taskId, transitionId, caseId, e);
        }
    }

    private boolean isSupersededRedelivery(JobDataMap data, String taskId) {
        boolean isRedelivery = data.containsKey(ATTEMPTS_KEY) && data.getIntValue(ATTEMPTS_KEY) > 0;
        return isRedelivery && data.containsKey(DUE_AT_KEY) && schedulingService.isSuperseded(taskId, data.getLongValue(DUE_AT_KEY));
    }

    private void redeliver(JobDataMap data, String caseId, String taskId, String transitionId) {
        int attempts = data.containsKey(ATTEMPTS_KEY) ? data.getIntValue(ATTEMPTS_KEY) : 0;
        if (attempts + 1 >= timeTriggerProperties.getRedeliver().getMaxAttempts()) {
            metrics.failed();
            log.error("Time trigger redelivery exhausted for task [{}] of transition [{}] in case [{}] after [{}] attempts", taskId, transitionId, caseId, attempts + 1);
            return;
        }

        TimeTriggerRedeliveryResult result = schedulingService.scheduleRedelivery(caseId, taskId, transitionId, data.getLongValue(DUE_AT_KEY), attempts + 1);
        if (result == TimeTriggerRedeliveryResult.SCHEDULED) {
            metrics.redelivered();
            log.info("Time trigger redelivery scheduled for task [{}] of transition [{}] in case [{}] (attempt [{}])", taskId, transitionId, caseId, attempts + 1);
        } else if (result == TimeTriggerRedeliveryResult.FAILED) {
            metrics.scheduleFailed();
            log.error("Time trigger redelivery could not be stored for task [{}] of transition [{}] in case [{}] (attempt [{}]), the time trigger will not be redelivered", taskId, transitionId, caseId, attempts + 1);
        }
    }
}
