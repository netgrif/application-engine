package com.netgrif.application.engine.workflow.scheduler;

import com.netgrif.application.engine.configuration.properties.TimeTriggerProperties;
import com.netgrif.application.engine.workflow.service.TimeTriggerOutcome;
import com.netgrif.application.engine.workflow.service.TimeTriggerRetryableException;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeTriggerJobTest {

    private static final String CASE_ID = "case-456";
    private static final String TASK_ID = "task-123";
    private static final String TRANSITION_ID = "transition-789";
    private static final long DUE_AT = 1_735_000_000_000L;

    @Mock
    private ITaskService taskService;

    @Mock
    private TimeTriggerSchedulingService schedulingService;

    @Mock
    private TimeTriggerMetrics metrics;

    @Mock
    private JobExecutionContext context;

    private TimeTriggerProperties properties;
    private TimeTriggerJob job;
    private JobDataMap jobData;

    @BeforeEach
    void setUp() {
        properties = new TimeTriggerProperties();
        properties.getRedeliver().setDelay(Duration.ofMinutes(1));
        job = new TimeTriggerJob();
        ReflectionTestUtils.setField(job, "taskService", taskService);
        ReflectionTestUtils.setField(job, "schedulingService", schedulingService);
        ReflectionTestUtils.setField(job, "timeTriggerProperties", properties);
        ReflectionTestUtils.setField(job, "metrics", metrics);

        jobData = new JobDataMap();
        jobData.put("caseId", CASE_ID);
        jobData.put("taskId", TASK_ID);
        jobData.put("transitionId", TRANSITION_ID);
        jobData.put("dueAt", DUE_AT);
    }

    @Test
    void redeliveryLimitDefaultsToThreeAttempts() {
        assertEquals(3, properties.getRedeliver().getMaxAttempts());
    }

    @Test
    void executesTaskAndRecordsItsOutcome() {
        stubContextJobData();
        when(context.getFireTime()).thenReturn(new Date(DUE_AT + 1_000));
        when(taskService.executeTimeTrigger(TASK_ID, DUE_AT)).thenReturn(TimeTriggerOutcome.FIRED);

        job.execute(context);

        verify(taskService).executeTimeTrigger(TASK_ID, DUE_AT);
        verify(metrics).executed(TimeTriggerOutcome.FIRED);
        verify(metrics).lateness(1_000L);
        verify(metrics, never()).failed();
    }

    @Test
    void recordsLatenessAsFireTimeMinusDueAt() {
        SimpleMeterRegistry registry = useRealMetrics();
        stubContextJobData();
        when(context.getFireTime()).thenReturn(new Date(DUE_AT + 2_500));
        when(taskService.executeTimeTrigger(TASK_ID, DUE_AT)).thenReturn(TimeTriggerOutcome.FIRED);

        job.execute(context);

        Timer lateness = registry.find("netgrif.engine.time_trigger.lateness").timer();
        assertNotNull(lateness);
        assertEquals(1, lateness.count());
        assertEquals(2_500d, lateness.totalTime(TimeUnit.MILLISECONDS), 0.001);
    }

    @Test
    void doesNotRecordLatenessWhenFireTimeIsBeforeDueAtAndStillCompletes() {
        SimpleMeterRegistry registry = useRealMetrics();
        stubContextJobData();
        when(context.getFireTime()).thenReturn(new Date(DUE_AT - 1_500));
        when(taskService.executeTimeTrigger(TASK_ID, DUE_AT)).thenReturn(TimeTriggerOutcome.FIRED);

        assertDoesNotThrow(() -> job.execute(context));

        assertNull(registry.find("netgrif.engine.time_trigger.lateness").timer());
        assertEquals(1, registry.get("netgrif.engine.time_trigger.executed").tag("outcome", "fired").counter().count());
        assertNull(registry.find("netgrif.engine.time_trigger.failed").counter());
        verify(taskService).executeTimeTrigger(TASK_ID, DUE_AT);
    }

    @Test
    void recordsZeroLatenessWhenFireTimeEqualsDueAt() {
        SimpleMeterRegistry registry = useRealMetrics();
        stubContextJobData();
        when(context.getFireTime()).thenReturn(new Date(DUE_AT));
        when(taskService.executeTimeTrigger(TASK_ID, DUE_AT)).thenReturn(TimeTriggerOutcome.FIRED);

        job.execute(context);

        Timer lateness = registry.find("netgrif.engine.time_trigger.lateness").timer();
        assertNotNull(lateness);
        assertEquals(1, lateness.count());
        assertEquals(0d, lateness.totalTime(TimeUnit.MILLISECONDS), 0.001);
    }

    @Test
    void doesNotRecordLatenessWhenDueAtIsMissing() {
        SimpleMeterRegistry registry = useRealMetrics();
        jobData.remove("dueAt");
        stubContextJobData();
        when(taskService.executeTimeTrigger(TASK_ID, null)).thenReturn(TimeTriggerOutcome.FIRED);

        job.execute(context);

        assertNull(registry.find("netgrif.engine.time_trigger.lateness").timer());
        assertEquals(1, registry.get("netgrif.engine.time_trigger.executed").tag("outcome", "fired").counter().count());
    }

    @Test
    void recordsScheduleFailedWhenRedeliveryCannotBeWritten() {
        givenRetryableExecutionFailure();
        when(schedulingService.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, DUE_AT, 1)).thenReturn(TimeTriggerRedeliveryResult.FAILED);

        job.execute(context);

        verify(metrics).scheduleFailed();
        verify(metrics, never()).redelivered();
    }

    @Test
    void skipsExecutionOfARetryWhenTheCanonicalJobWasRescheduledToAnotherDueAt() {
        jobData.put("attempts", 1);
        stubContextJobData();
        when(schedulingService.isSuperseded(TASK_ID, DUE_AT)).thenReturn(true);

        job.execute(context);

        verify(taskService, never()).executeTimeTrigger(any(), any());
        verify(metrics, never()).executed(any());
        verify(metrics, never()).failed();
        verify(metrics, never()).redelivered();
        verify(schedulingService, never()).scheduleRedelivery(any(), any(), any(), anyLong(), anyInt());
    }

    @Test
    void executesARetryWhenTheCanonicalJobIsNotSuperseded() {
        jobData.put("attempts", 1);
        stubContextJobData();
        when(schedulingService.isSuperseded(TASK_ID, DUE_AT)).thenReturn(false);
        when(taskService.executeTimeTrigger(TASK_ID, DUE_AT)).thenReturn(TimeTriggerOutcome.FIRED);
        when(context.getFireTime()).thenReturn(new Date(DUE_AT));

        job.execute(context);

        verify(taskService).executeTimeTrigger(TASK_ID, DUE_AT);
        verify(metrics).executed(TimeTriggerOutcome.FIRED);
    }

    @Test
    void doesNotCheckForSupersessionOnTheFirstExecution() {
        stubContextJobData();
        when(taskService.executeTimeTrigger(TASK_ID, DUE_AT)).thenReturn(TimeTriggerOutcome.SKIPPED_ASSIGNED);

        job.execute(context);

        verify(schedulingService, never()).isSuperseded(any(), anyLong());
        verify(taskService).executeTimeTrigger(TASK_ID, DUE_AT);
    }

    @Test
    void doesNotRecordRedeliveredNorScheduleFailedWhenRedeliveryIsSuperseded() {
        givenRetryableExecutionFailure();
        when(schedulingService.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, DUE_AT, 1)).thenReturn(TimeTriggerRedeliveryResult.SUPERSEDED);

        job.execute(context);

        verify(metrics, never()).redelivered();
        verify(metrics, never()).scheduleFailed();
        verify(metrics, never()).failed();
    }

    @Test
    void retryableFailureDelegatesRedeliveryToSchedulingServiceWithIncrementedAttempt() {
        properties.getRedeliver().setMaxAttempts(5);
        jobData.put("attempts", 2);
        stubContextJobData();
        doThrow(new TimeTriggerRetryableException("execution failed", new IllegalStateException("execution failed")))
                .when(taskService)
                .executeTimeTrigger(TASK_ID, DUE_AT);
        when(schedulingService.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, DUE_AT, 3)).thenReturn(TimeTriggerRedeliveryResult.SCHEDULED);

        assertDoesNotThrow(() -> job.execute(context));

        verify(schedulingService).scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, DUE_AT, 3);
        verify(metrics).redelivered();
        verify(metrics, never()).scheduleFailed();
    }

    @Test
    void resumeFailureRecordsFailureWithoutSchedulingRedelivery() {
        stubContextJobData();
        when(taskService.executeTimeTrigger(TASK_ID, DUE_AT)).thenReturn(TimeTriggerOutcome.RESUME_FAILED);

        job.execute(context);

        verify(metrics).executed(TimeTriggerOutcome.RESUME_FAILED);
        verify(metrics).failed();
        verify(schedulingService, never()).scheduleRedelivery(any(), any(), any(), anyLong(), anyInt());
        verify(metrics, never()).redelivered();
    }

    @Test
    void stopsRedeliveryWhenTheConfiguredAttemptLimitIsReached() {
        properties.getRedeliver().setMaxAttempts(2);
        jobData.put("attempts", 1);
        stubContextJobData();
        doThrow(new TimeTriggerRetryableException(
                        "execution failed",
                        new IllegalStateException("execution failed")
                ))
                .when(taskService)
                .executeTimeTrigger(TASK_ID, DUE_AT);

        job.execute(context);

        verify(schedulingService, never()).scheduleRedelivery(any(), any(), any(), anyLong(), anyInt());
        verify(metrics).failed();
    }

    @Test
    void doesNotRetryUnexpectedRuntimeFailureAndRecordsFailure() {
        stubContextJobData();
        doThrow(new IllegalStateException("unexpected execution failure"))
                .when(taskService)
                .executeTimeTrigger(TASK_ID, DUE_AT);

        job.execute(context);

        verify(schedulingService, never()).scheduleRedelivery(any(), any(), any(), anyLong(), anyInt());
        verify(metrics).failed();
        verify(metrics, never()).redelivered();
    }

    @Test
    void preventsConcurrentExecutionForTheSameQuartzJobKey() {
        assertNotNull(TimeTriggerJob.class.getAnnotation(DisallowConcurrentExecution.class));
    }

    private SimpleMeterRegistry useRealMetrics() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(job, "metrics", new TimeTriggerMetrics(registry));
        return registry;
    }

    private void stubContextJobData() {
        when(context.getMergedJobDataMap()).thenReturn(jobData);
    }

    private void givenRetryableExecutionFailure() {
        properties.getRedeliver().setMaxAttempts(5);
        stubContextJobData();
        doThrow(new TimeTriggerRetryableException(
                        "execution failed",
                        new IllegalStateException("execution failed")
                ))
                .when(taskService)
                .executeTimeTrigger(TASK_ID, DUE_AT);
    }
}
