package com.netgrif.application.engine.workflow.scheduler;

import com.netgrif.application.engine.configuration.properties.TimeTriggerProperties;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeTriggerSchedulingServiceTest {

    private static final String TASK_ID = "task-123";
    private static final String CASE_ID = "case-456";
    private static final String TRANSITION_ID = "transition-789";
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    @Mock
    private Scheduler scheduler;

    @Mock
    private TimeTriggerMetrics metrics;

    private TimeTriggerProperties properties;

    private TimeTriggerSchedulingService service;

    @BeforeEach
    void setUp() {
        properties = new TimeTriggerProperties();
        service = new TimeTriggerSchedulingService();
        ReflectionTestUtils.setField(service, "scheduler", scheduler);
        ReflectionTestUtils.setField(service, "metrics", metrics);
        ReflectionTestUtils.setField(service, "timeTriggerProperties", properties);
    }

    @Test
    void schedulesSingleDurableRecoveryJobWithOnlyScalarTaskIdentifiers() throws Exception {
        Task task = task();
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34);
        long expectedEpochMillis = dueAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        service.schedule(task, dueAt);

        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture());

        JobDetail job = jobCaptor.getValue();
        SimpleTrigger trigger = (SimpleTrigger) triggerCaptor.getValue();
        JobKey expectedJobKey = new JobKey(TASK_ID, "timeTriggers");
        TriggerKey expectedTriggerKey = new TriggerKey(TASK_ID, "timeTriggers");

        assertEquals(expectedJobKey, job.getKey());
        assertEquals(expectedTriggerKey, trigger.getKey());
        assertEquals(expectedJobKey, trigger.getJobKey());
        assertFalse(job.isDurable());
        assertTrue(job.requestsRecovery());
        assertEquals(
                Map.of(
                        "caseId", CASE_ID,
                        "taskId", TASK_ID,
                        "transitionId", TRANSITION_ID,
                        "dueAt", expectedEpochMillis
                ),
                job.getJobDataMap().getWrappedMap()
        );
        assertEquals(
                Set.of("caseId", "taskId", "transitionId", "dueAt"),
                job.getJobDataMap().keySet()
        );
        assertEquals(0, trigger.getRepeatCount());
        assertEquals(new Date(expectedEpochMillis), trigger.getStartTime());
        assertEquals(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW, trigger.getMisfireInstruction());
    }

    @Test
    void resolvesLocalTimeUsingTheSystemDefaultZone() throws Exception {
        Task task = task();
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.MARCH, 30, 2, 30);
        long expectedEpochMillis = dueAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        service.schedule(task, dueAt);

        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture());

        assertEquals(expectedEpochMillis, jobCaptor.getValue().getJobDataMap().getLongValue("dueAt"));
        assertEquals(new Date(expectedEpochMillis), triggerCaptor.getValue().getStartTime());
    }

    @Test
    void treatsAlreadyScheduledTaskAsIdempotent() throws Exception {
        doThrow(new ObjectAlreadyExistsException("already exists"))
                .when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        assertTrue(service.schedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));

        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(metrics, never()).scheduled();
    }

    @Test
    void retriesSchedulingOnceAndRecordsOneSuccessfulSchedule() throws Exception {
        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class)))
                .thenThrow(new SchedulerException("scheduler unavailable"))
                .thenReturn(new Date());

        assertTrue(service.schedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));

        verify(scheduler, times(2)).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(metrics, times(1)).scheduled();
    }

    @Test
    void retriesSchedulingTwiceBeforeRecordingTheThirdAttemptAsSuccessful() throws Exception {
        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class)))
                .thenThrow(new SchedulerException("first scheduler failure"))
                .thenThrow(new SchedulerException("second scheduler failure"))
                .thenReturn(new Date());

        assertTrue(service.schedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));

        verify(scheduler, times(3)).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(metrics, times(1)).scheduled();
    }

    @Test
    void stopsAfterThreeSchedulingFailuresWithoutRecordingASchedule() throws Exception {
        doThrow(new SchedulerException("scheduler unavailable"))
                .when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        assertFalse(service.schedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));

        verify(scheduler, times(3)).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(metrics, never()).scheduled();
        verify(metrics, times(1)).scheduleFailed();
    }

    @Test
    void doesNotRecordScheduleFailedWhenSchedulingSucceeds() throws Exception {
        assertTrue(service.schedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));

        verify(metrics).scheduled();
        verify(metrics, never()).scheduleFailed();
    }

    @Test
    void incrementsScheduleFailedCounterWhenAttemptsAreExhausted() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(service, "metrics", new TimeTriggerMetrics(registry));
        doThrow(new SchedulerException("scheduler unavailable"))
                .when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        service.schedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34));

        assertEquals(1, registry.get("netgrif.engine.time_trigger.schedule_failed").counter().count());
        assertNull(registry.find("netgrif.engine.time_trigger.scheduled").counter());
    }

    @Test
    void unscheduleIgnoresSchedulerFailure() throws Exception {
        doThrow(new SchedulerException("scheduler unavailable"))
                .when(scheduler)
                .deleteJobs(anyList());

        assertDoesNotThrow(() -> service.unschedule(TASK_ID));

        verify(scheduler).deleteJobs(anyList());
    }

    @Test
    void unscheduleDelegatesToBatchDeletionOfTaskAndRetryJobs() throws Exception {
        service.unschedule(TASK_ID);

        assertEquals(keysFor(DEFAULT_MAX_ATTEMPTS, TASK_ID), deletedKeys());
    }

    @Test
    void unscheduleAllDeletesTaskAndRetryJobsOfEveryTaskInOneBatch() throws Exception {
        service.unscheduleAll(List.of("task-1", "task-2"));

        verify(scheduler, times(1)).deleteJobs(anyList());
        assertEquals(keysFor(DEFAULT_MAX_ATTEMPTS, "task-1", "task-2"), deletedKeys());
    }

    @Test
    void unscheduleAllDoesNothingForEmptyCollection() throws Exception {
        service.unscheduleAll(List.of());

        verifyNoInteractions(scheduler);
    }

    @Test
    void unscheduleAllHonoursCustomMaxAttempts() throws Exception {
        properties.getRedeliver().setMaxAttempts(5);

        service.unscheduleAll(List.of(TASK_ID));

        List<JobKey> keys = deletedKeys();
        assertEquals(6, keys.size());
        assertTrue(keys.contains(new JobKey(TASK_ID + "#r5", "timeTriggers")));
        assertFalse(keys.contains(new JobKey(TASK_ID + "#r6", "timeTriggers")));
    }

    @Test
    void scheduleRedeliverySchedulesJobWithIncrementedAttemptAndDistinctKey() throws Exception {
        properties.getRedeliver().setDelay(Duration.ofMinutes(1));
        long dueAt = 1_735_000_000_000L;
        long before = System.currentTimeMillis();

        assertEquals(TimeTriggerRedeliveryResult.SCHEDULED, service.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, dueAt, 3));

        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture());
        JobDetail job = jobCaptor.getValue();
        SimpleTrigger trigger = (SimpleTrigger) triggerCaptor.getValue();
        JobKey retryJobKey = new JobKey(TASK_ID + "#r3", "timeTriggers");

        assertEquals(retryJobKey, job.getKey());
        assertTrue(job.requestsRecovery());
        assertEquals(CASE_ID, job.getJobDataMap().getString("caseId"));
        assertEquals(TASK_ID, job.getJobDataMap().getString("taskId"));
        assertEquals(TRANSITION_ID, job.getJobDataMap().getString("transitionId"));
        assertEquals(dueAt, job.getJobDataMap().getLongValue("dueAt"));
        assertEquals(3, job.getJobDataMap().getIntValue("attempts"));
        assertEquals(new TriggerKey(TASK_ID + "#r3", "timeTriggers"), trigger.getKey());
        assertEquals(retryJobKey, trigger.getJobKey());
        assertEquals(0, trigger.getRepeatCount());
        assertEquals(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW, trigger.getMisfireInstruction());
        assertTrue(trigger.getStartTime().getTime() >= before + Duration.ofMinutes(1).toMillis());
        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    void scheduleRedeliveryRetriesSchedulingBeforeSucceeding() throws Exception {
        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class)))
                .thenThrow(new SchedulerException("first scheduler failure"))
                .thenThrow(new SchedulerException("second scheduler failure"))
                .thenReturn(new Date());

        assertEquals(TimeTriggerRedeliveryResult.SCHEDULED, service.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, 1L, 1));

        verify(scheduler, times(3)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void scheduleRedeliveryReturnsFalseAfterThreeSchedulingFailures() throws Exception {
        doThrow(new SchedulerException("scheduler unavailable"))
                .when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        assertEquals(TimeTriggerRedeliveryResult.FAILED, service.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, 1L, 1));

        verify(scheduler, times(3)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void scheduleRedeliveryTreatsAlreadyScheduledAsIdempotentWithoutRetrying() throws Exception {
        doThrow(new ObjectAlreadyExistsException("already exists"))
                .when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        assertEquals(TimeTriggerRedeliveryResult.SCHEDULED, service.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, 1L, 1));

        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void scheduleRedeliveryIsSupersededWhenCanonicalJobWasRescheduledToAnotherDueAt() throws Exception {
        givenCanonicalJobWithDueAt(2L);

        assertEquals(TimeTriggerRedeliveryResult.SUPERSEDED, service.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, 1L, 2));

        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void scheduleRedeliverySchedulesWhenCanonicalJobHasTheSameDueAt() throws Exception {
        givenCanonicalJobWithDueAt(1L);

        assertEquals(TimeTriggerRedeliveryResult.SCHEDULED, service.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, 1L, 2));

        verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void scheduleRedeliverySchedulesWhenCanonicalJobAlreadyCompleted() throws Exception {
        when(scheduler.getJobDetail(new JobKey(TASK_ID, "timeTriggers"))).thenReturn(null);

        assertEquals(TimeTriggerRedeliveryResult.SCHEDULED, service.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, 1L, 2));

        verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void scheduleRedeliveryFailsWhenCanonicalJobCannotBeRead() throws Exception {
        when(scheduler.getJobDetail(new JobKey(TASK_ID, "timeTriggers"))).thenThrow(new SchedulerException("scheduler unavailable"));

        assertEquals(TimeTriggerRedeliveryResult.FAILED, service.scheduleRedelivery(CASE_ID, TASK_ID, TRANSITION_ID, 1L, 2));

        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    private void givenCanonicalJobWithDueAt(long dueAt) throws SchedulerException {
        JobDetail canonicalJob = mock(JobDetail.class);
        JobDataMap canonicalData = new JobDataMap();
        canonicalData.put("dueAt", dueAt);
        when(canonicalJob.getJobDataMap()).thenReturn(canonicalData);
        when(scheduler.getJobDetail(new JobKey(TASK_ID, "timeTriggers"))).thenReturn(canonicalJob);
    }

    @Test
    void isSupersededWhenCanonicalJobHasAnotherDueAt() throws Exception {
        givenCanonicalJobWithDueAt(2L);

        assertTrue(service.isSuperseded(TASK_ID, 1L));
    }

    @Test
    void isNotSupersededWhenCanonicalJobHasTheSameDueAt() throws Exception {
        givenCanonicalJobWithDueAt(1L);

        assertFalse(service.isSuperseded(TASK_ID, 1L));
    }

    @Test
    void isNotSupersededWhenCanonicalJobIsAbsent() throws Exception {
        when(scheduler.getJobDetail(new JobKey(TASK_ID, "timeTriggers"))).thenReturn(null);

        assertFalse(service.isSuperseded(TASK_ID, 1L));
    }

    @Test
    void isNotSupersededWhenCanonicalJobCannotBeRead() throws Exception {
        when(scheduler.getJobDetail(new JobKey(TASK_ID, "timeTriggers"))).thenThrow(new SchedulerException("scheduler unavailable"));

        assertFalse(service.isSuperseded(TASK_ID, 1L));
    }

    @Test
    void rescheduleReplacesExistingTriggerWithoutFallbackAndClearsRetryJobs() throws Exception {
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34);
        long expectedEpochMillis = dueAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        TriggerKey triggerKey = new TriggerKey(TASK_ID, "timeTriggers");
        when(scheduler.rescheduleJob(eq(triggerKey), any(Trigger.class))).thenReturn(new Date());

        boolean result = service.reschedule(task(), dueAt);

        assertTrue(result);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).rescheduleJob(eq(triggerKey), triggerCaptor.capture());
        assertEquals(new Date(expectedEpochMillis), triggerCaptor.getValue().getStartTime());
        assertEquals(new JobKey(TASK_ID, "timeTriggers"), triggerCaptor.getValue().getJobKey());
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
        for (int attempt = 1; attempt <= DEFAULT_MAX_ATTEMPTS; attempt++) {
            verify(scheduler).deleteJob(new JobKey(TASK_ID + "#r" + attempt, "timeTriggers"));
        }
        verify(scheduler, never()).deleteJob(new JobKey(TASK_ID, "timeTriggers"));
        verify(metrics).scheduled();
        verify(metrics, never()).scheduleFailed();
    }

    @Test
    void rescheduleFallsBackToScheduleJobWhenNoExistingTrigger() throws Exception {
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34);
        long expectedEpochMillis = dueAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(null);

        boolean result = service.reschedule(task(), dueAt);

        assertTrue(result);
        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        InOrder inOrder = inOrder(scheduler);
        inOrder.verify(scheduler).rescheduleJob(eq(new TriggerKey(TASK_ID, "timeTriggers")), any(Trigger.class));
        inOrder.verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture());
        assertEquals(new JobKey(TASK_ID, "timeTriggers"), jobCaptor.getValue().getKey());
        assertEquals(expectedEpochMillis, jobCaptor.getValue().getJobDataMap().getLongValue("dueAt"));
        assertEquals(new Date(expectedEpochMillis), triggerCaptor.getValue().getStartTime());
        verify(metrics).scheduled();
        verify(metrics, never()).scheduleFailed();
    }

    @Test
    void rescheduleReplacesJobDataWithNewDueAtBeforeReschedulingTrigger() throws Exception {
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.JUNE, 1, 8, 15);
        long expectedEpochMillis = dueAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(new Date());

        assertTrue(service.reschedule(task(), dueAt));

        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        InOrder inOrder = inOrder(scheduler);
        inOrder.verify(scheduler).addJob(jobCaptor.capture(), eq(true), eq(true));
        inOrder.verify(scheduler).rescheduleJob(eq(new TriggerKey(TASK_ID, "timeTriggers")), any(Trigger.class));
        JobDetail job = jobCaptor.getValue();
        assertEquals(new JobKey(TASK_ID, "timeTriggers"), job.getKey());
        assertEquals(expectedEpochMillis, job.getJobDataMap().getLongValue("dueAt"));
        assertEquals(CASE_ID, job.getJobDataMap().getString("caseId"));
        assertEquals(TASK_ID, job.getJobDataMap().getString("taskId"));
        assertEquals(TRANSITION_ID, job.getJobDataMap().getString("transitionId"));
    }

    @Test
    void rescheduleReturnsFalseRecordsFailureAndSkipsRescheduleWhenAddJobFails() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(service, "metrics", new TimeTriggerMetrics(registry));
        doThrow(new SchedulerException("scheduler unavailable"))
                .when(scheduler)
                .addJob(any(JobDetail.class), eq(true), eq(true));

        boolean result = service.reschedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34));

        assertFalse(result);
        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(scheduler, never()).deleteJob(new JobKey(TASK_ID, "timeTriggers"));
        assertEquals(1, registry.get("netgrif.engine.time_trigger.schedule_failed").counter().count());
        assertNull(registry.find("netgrif.engine.time_trigger.scheduled").counter());
    }

    @Test
    void rescheduleFallbackDeletesJobBeforeSchedulingItAgain() throws Exception {
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(null);

        assertTrue(service.reschedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));

        InOrder inOrder = inOrder(scheduler);
        inOrder.verify(scheduler).addJob(any(JobDetail.class), eq(true), eq(true));
        inOrder.verify(scheduler).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
        inOrder.verify(scheduler).deleteJob(new JobKey(TASK_ID, "timeTriggers"));
        inOrder.verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void rescheduleReturnsFalseAndRecordsFailureWhenRescheduleJobThrows() throws Exception {
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class)))
                .thenThrow(new SchedulerException("scheduler unavailable"));

        boolean result = service.reschedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34));

        assertFalse(result);
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(scheduler, never()).deleteJob(new JobKey(TASK_ID, "timeTriggers"));
        verify(metrics).scheduleFailed();
        verify(metrics, never()).scheduled();
    }

    @Test
    void rescheduleReturnsFalseWhenFallbackScheduleJobThrows() throws Exception {
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(null);
        doThrow(new SchedulerException("scheduler unavailable"))
                .when(scheduler)
                .scheduleJob(any(JobDetail.class), any(Trigger.class));

        boolean result = service.reschedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34));

        assertFalse(result);
        verify(metrics).scheduleFailed();
        verify(metrics, never()).scheduled();
    }

    @Test
    void rescheduleReturnsFalseAndLeavesTheCanonicalJobUntouchedWhenClearingRetryJobsFails() throws Exception {
        doThrow(new SchedulerException("scheduler unavailable"))
                .when(scheduler)
                .deleteJob(any(JobKey.class));

        boolean result = service.reschedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34));

        assertFalse(result);
        verify(scheduler, never()).addJob(any(JobDetail.class), eq(true), eq(true));
        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(metrics).scheduleFailed();
        verify(metrics, never()).scheduled();
    }

    @Test
    void rescheduleDeletesPendingRetryJobsBeforeReplacingTheCanonicalJob() throws Exception {
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(new Date());

        assertTrue(service.reschedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));

        InOrder inOrder = inOrder(scheduler);
        for (int attempt = 1; attempt <= DEFAULT_MAX_ATTEMPTS; attempt++) {
            inOrder.verify(scheduler).deleteJob(new JobKey(TASK_ID + "#r" + attempt, "timeTriggers"));
        }
        inOrder.verify(scheduler).addJob(any(JobDetail.class), eq(true), eq(true));
        inOrder.verify(scheduler).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    void rescheduleIncrementsMetricCountersOnRealRegistry() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(service, "metrics", new TimeTriggerMetrics(registry));
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class)))
                .thenReturn(new Date())
                .thenThrow(new SchedulerException("scheduler unavailable"));

        assertTrue(service.reschedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));
        assertFalse(service.reschedule(task(), LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34)));

        assertEquals(1, registry.get("netgrif.engine.time_trigger.scheduled").counter().count());
        assertEquals(1, registry.get("netgrif.engine.time_trigger.schedule_failed").counter().count());
    }

    @SuppressWarnings("unchecked")
    private List<JobKey> deletedKeys() throws SchedulerException {
        ArgumentCaptor<List<JobKey>> captor = ArgumentCaptor.forClass(List.class);
        verify(scheduler).deleteJobs(captor.capture());
        return captor.getValue();
    }

    private List<JobKey> keysFor(int maxAttempts, String... taskIds) {
        List<JobKey> keys = new ArrayList<>();
        for (String taskId : taskIds) {
            keys.add(new JobKey(taskId, "timeTriggers"));
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                keys.add(new JobKey(taskId + "#r" + attempt, "timeTriggers"));
            }
        }
        return keys;
    }

    private Task task() {
        Task task = mock(Task.class);
        when(task.getCaseId()).thenReturn(CASE_ID);
        when(task.getStringId()).thenReturn(TASK_ID);
        when(task.getTransitionId()).thenReturn(TRANSITION_ID);
        return task;
    }
}
