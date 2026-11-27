package com.netgrif.application.engine.workflow.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.Transition;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.triggers.AutoTrigger;
import com.netgrif.application.engine.objects.workflow.domain.triggers.DateTimeTrigger;
import com.netgrif.application.engine.objects.workflow.domain.triggers.Trigger;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.scheduler.TimeTriggerSchedulingService;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskServiceTimeTriggerTest {

    private static final String TASK_ID = "task-123";
    private static final String CASE_ID = "case-456";
    private static final String TRANSITION_ID = "transition-789";
    private static final String PROCESS_ID = "process-321";
    private static final LocalDateTime DUE_AT = LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34);
    private static final long DUE_AT_MILLIS = DUE_AT.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    private TaskService service;
    private IWorkflowService workflowService;
    private TimeTriggerSchedulingService timeTriggerSchedulingService;
    private TaskRepository taskRepository;
    private IElasticTaskService elasticTaskService;
    private IElasticTaskMappingService taskMappingService;
    private IDataService dataService;
    private UserService userService;
    private ProcessRoleService processRoleService;

    @BeforeEach
    void setUp() {
        service = spy(new TaskService());
        workflowService = mock(IWorkflowService.class);
        timeTriggerSchedulingService = mock(TimeTriggerSchedulingService.class);
        taskRepository = mock(TaskRepository.class);
        elasticTaskService = mock(IElasticTaskService.class);
        taskMappingService = mock(IElasticTaskMappingService.class);
        dataService = mock(IDataService.class);
        userService = mock(UserService.class);
        processRoleService = mock(ProcessRoleService.class);
        AbstractUser systemUser = mock(AbstractUser.class);
        when(userService.getSystem()).thenReturn(systemUser);
        when(systemUser.getStringId()).thenReturn("system-user");

        ReflectionTestUtils.setField(service, "workflowService", workflowService);
        ReflectionTestUtils.setField(service, "timeTriggerSchedulingService", timeTriggerSchedulingService);
        ReflectionTestUtils.setField(service, "taskRepository", taskRepository);
        ReflectionTestUtils.setField(service, "elasticTaskService", elasticTaskService);
        ReflectionTestUtils.setField(service, "taskMappingService", taskMappingService);
        ReflectionTestUtils.setField(service, "dataService", dataService);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "processRoleService", processRoleService);
        ReflectionTestUtils.setField(service, "publisher", mock(ApplicationEventPublisher.class));
    }

    @Test
    void returnsSkippedTaskMissingWhenTaskDoesNotExist() {
        doReturn(Optional.empty()).when(service).findOptionalById(TASK_ID);

        assertEquals(TimeTriggerOutcome.SKIPPED_TASK_MISSING, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(workflowService, never()).findOne(any());
        verify(service, never()).executeTransition(any(), any());
    }

    @Test
    void returnsSkippedAlreadyFinishedForFinishedTaskWithoutExecutingIt() throws Exception {
        Task task = task();
        when(task.getFinishDate()).thenReturn(DUE_AT);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);

        assertEquals(TimeTriggerOutcome.SKIPPED_ALREADY_FINISHED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(workflowService, never()).findOne(any());
        verify(service, never()).isExecutable(any(), any());
        verify(service, never()).assignTask(any(TaskParams.class));
        verify(service, never()).finishTask(any(TaskParams.class));
        verify(service, never()).executeTransition(any(), any());
        verify(dataService, never()).getData(any(String.class));
    }

    @Test
    void returnsSkippedAlreadyFinishedWhenTaskFinishedAfterDueAt() {
        Task task = task();
        when(task.getFinishDate()).thenReturn(DUE_AT.plusSeconds(1));
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);

        assertEquals(TimeTriggerOutcome.SKIPPED_ALREADY_FINISHED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(workflowService, never()).findOne(any());
        verify(service, never()).executeTransition(any(), any());
    }

    @Test
    void firesTaskFinishedBeforeDueAt() throws Exception {
        Task task = task();
        when(task.getFinishDate()).thenReturn(DUE_AT.minusSeconds(1));
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        PetriNet petriNet = useCase.getPetriNet();
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(true).when(service).isExecutable(transition, petriNet);
        doReturn(new AssignTaskEventOutcome(useCase, task)).when(service).assignTask(any(TaskParams.class));
        doReturn(null).when(service).finishTask(any(TaskParams.class));

        assertEquals(TimeTriggerOutcome.FIRED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(service).assignTask(any(TaskParams.class));
        verify(service).finishTask(any(TaskParams.class));
    }

    @Test
    void doesNotResumeTaskFinishedBeforeDueAtWhenAssignedToSystemUser() throws Exception {
        Task task = task();
        when(task.getFinishDate()).thenReturn(DUE_AT.minusSeconds(1));
        lenient().when(task.getUserId()).thenReturn("system-user");
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(true).when(service).isExecutable(any(), any());

        assertEquals(TimeTriggerOutcome.SKIPPED_ASSIGNED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(dataService, never()).getData(any(String.class));
        verify(service, never()).finishTask(any(TaskParams.class));
    }

    @Test
    void returnsSkippedAlreadyFinishedWhenFinishedAndDueAtIsUnknown() {
        Task task = task();
        when(task.getFinishDate()).thenReturn(DUE_AT.minusDays(1));
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);

        assertEquals(TimeTriggerOutcome.SKIPPED_ALREADY_FINISHED, service.executeTimeTrigger(TASK_ID, null));
    }

    @Test
    void doesNotResumeFinishedTaskAssignedToSystemUser() throws Exception {
        Task task = task();
        when(task.getFinishDate()).thenReturn(DUE_AT);
        lenient().when(task.getUserId()).thenReturn("system-user");
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);

        TimeTriggerOutcome outcome = service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS);

        assertEquals(TimeTriggerOutcome.SKIPPED_ALREADY_FINISHED, outcome);
        verify(dataService, never()).getData(any(String.class));
        verify(service, never()).finishTask(any(TaskParams.class));
        verify(service, never()).executeTransition(any(), any());
    }

    @Test
    void skipsFinishedTaskEvenWhenItsTransitionIsMissing() {
        Task task = task();
        when(task.getFinishDate()).thenReturn(DUE_AT);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);

        assertEquals(TimeTriggerOutcome.SKIPPED_ALREADY_FINISHED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(workflowService, never()).findOne(any());
    }

    @Test
    void resumesUnfinishedTaskAssignedToSystemUser() throws Exception {
        Task task = task();
        when(task.getFinishDate()).thenReturn(null);
        when(task.getUserId()).thenReturn("system-user");
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(null).when(service).finishTask(any(TaskParams.class));

        assertEquals(TimeTriggerOutcome.RESUMED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(service).finishTask(any(TaskParams.class));
    }

    @Test
    void retriesWhenTaskLookupFailsWithTransientDataAccessException() {
        TransientDataAccessException taskLookupFailure = transientFailure("task lookup failed");
        doThrow(taskLookupFailure).when(service).findOptionalById(TASK_ID);

        TimeTriggerRetryableException failure = assertThrows(
                TimeTriggerRetryableException.class,
                () -> service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS)
        );

        assertSame(taskLookupFailure, failure.getCause());
        verify(workflowService, never()).findOne(any());
    }

    @Test
    void retriesWhenCaseDoesNotExist() {
        Task task = task();
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        IllegalArgumentException missingCase = new IllegalArgumentException("case missing");
        when(workflowService.findOne(CASE_ID)).thenThrow(missingCase);

        TimeTriggerRetryableException failure = assertThrows(
                TimeTriggerRetryableException.class,
                () -> service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS)
        );

        assertSame(missingCase, failure.getCause());
        verify(service, never()).executeTransition(any(), any());
    }

    @Test
    void retriesWhenCaseLookupFailsWithTransientDataAccessException() {
        Task task = task();
        TransientDataAccessException caseLookupFailure = transientFailure("case lookup failed");
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenThrow(caseLookupFailure);

        TimeTriggerRetryableException failure = assertThrows(
                TimeTriggerRetryableException.class,
                () -> service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS)
        );

        assertSame(caseLookupFailure, failure.getCause());
    }

    @Test
    void returnsSkippedTransitionMissingWhenTransitionDoesNotExist() {
        Task task = task();
        Case useCase = useCase(null);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);

        assertEquals(TimeTriggerOutcome.SKIPPED_TRANSITION_MISSING, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(service, never()).isExecutable(any(), any());
        verify(service, never()).executeTransition(any(), any());
    }

    @Test
    void returnsSkippedNotExecutableWhenTransitionCannotExecute() {
        Task task = task();
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        PetriNet petriNet = useCase.getPetriNet();
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(false).when(service).isExecutable(transition, petriNet);

        assertEquals(TimeTriggerOutcome.SKIPPED_NOT_EXECUTABLE, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(service, never()).executeTransition(any(), any());
    }

    @Test
    void returnsSkippedAssignedWhenTaskBelongsToANonSystemUser() throws Exception {
        Task task = task();
        when(task.getUserId()).thenReturn("user-1");
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        PetriNet petriNet = useCase.getPetriNet();
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(true).when(service).isExecutable(transition, petriNet);

        assertEquals(TimeTriggerOutcome.SKIPPED_ASSIGNED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(service, never()).executeTransition(any(), any());
        verify(service, never()).finishTask(any(TaskParams.class));
    }

    @Test
    void finishesSystemAssignedTaskAndReturnsResumed() throws Exception {
        Task task = task();
        when(task.getUserId()).thenReturn("system-user");
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(null).when(service).finishTask(any(TaskParams.class));

        assertEquals(TimeTriggerOutcome.RESUMED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        var order = inOrder(dataService, service);
        order.verify(dataService).getData(TASK_ID);
        order.verify(service).finishTask(any(TaskParams.class));
        verify(service, never()).executeTransition(any(), any());
    }

    @Test
    void returnsResumeFailedWhenLoadingDataForSystemAssignedTaskFailsTransiently() throws Exception {
        Task task = task();
        when(task.getUserId()).thenReturn("system-user");
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        TransientDataAccessException dataFailure = transientFailure("data loading failed");
        when(dataService.getData(TASK_ID)).thenThrow(dataFailure);

        assertEquals(TimeTriggerOutcome.RESUME_FAILED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));
        verify(dataService).getData(TASK_ID);
        verify(service, never()).finishTask(any(TaskParams.class));
    }

    @Test
    void returnsResumeFailedWhenLoadingDataForSystemAssignedTaskFailsNonTransiently() throws Exception {
        Task task = task();
        when(task.getUserId()).thenReturn("system-user");
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        IllegalStateException dataFailure = new IllegalStateException("data loading failed");
        when(dataService.getData(TASK_ID)).thenThrow(dataFailure);

        assertEquals(TimeTriggerOutcome.RESUME_FAILED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        verify(dataService).getData(TASK_ID);
        verify(service, never()).finishTask(any(TaskParams.class));
    }

    @Test
    void returnsResumeFailedWhenFinishingSystemAssignedTaskFailsTransiently() throws Exception {
        Task task = task();
        when(task.getUserId()).thenReturn("system-user");
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        TransientDataAccessException finishFailure = transientFailure("finish failed");
        doThrow(finishFailure).when(service).finishTask(any(TaskParams.class));

        assertEquals(TimeTriggerOutcome.RESUME_FAILED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));
        verify(service).finishTask(any(TaskParams.class));
    }

    @Test
    void returnsResumeFailedWhenFinishingSystemAssignedTaskFailsNonTransiently() throws Exception {
        Task task = task();
        when(task.getUserId()).thenReturn("system-user");
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        IllegalStateException finishFailure = new IllegalStateException("finish failed");
        doThrow(finishFailure).when(service).finishTask(any(TaskParams.class));

        assertEquals(TimeTriggerOutcome.RESUME_FAILED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));
        verify(service).finishTask(any(TaskParams.class));
    }

    @Test
    void retriesWhenPerformTransitionFailsWithTransientDataAccessException() throws Exception {
        Task task = task();
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        PetriNet petriNet = useCase.getPetriNet();
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(true).when(service).isExecutable(transition, petriNet);
        TransientDataAccessException performTransitionFailure = transientFailure("assign failed");
        doThrow(performTransitionFailure).when(service).assignTask(any(TaskParams.class));

        TimeTriggerRetryableException failure = assertThrows(
                TimeTriggerRetryableException.class,
                () -> service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS)
        );

        assertSame(performTransitionFailure, failure.getCause());
    }

    @Test
    void propagatesPerformTransitionFailureWithoutReturningFired() throws Exception {
        Task task = task();
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        PetriNet petriNet = useCase.getPetriNet();
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(true).when(service).isExecutable(transition, petriNet);
        IllegalStateException performTransitionFailure = new IllegalStateException("assign failed");
        doThrow(performTransitionFailure).when(service).assignTask(any(TaskParams.class));

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS)
        );

        assertSame(performTransitionFailure, failure);
        verify(dataService, never()).getData(any(String.class));
        verify(service, never()).finishTask(any(TaskParams.class));
    }

    @Test
    void executesAnExistingUnassignedExecutableTaskAndReturnsFiredOnlyAfterTransitionCompletes() throws Exception {
        Task task = task();
        Transition transition = mock(Transition.class);
        Case useCase = useCase(transition);
        PetriNet petriNet = useCase.getPetriNet();
        doReturn(Optional.of(task)).when(service).findOptionalById(TASK_ID);
        when(workflowService.findOne(CASE_ID)).thenReturn(useCase);
        doReturn(true).when(service).isExecutable(transition, petriNet);
        doReturn(new AssignTaskEventOutcome(useCase, task)).when(service).assignTask(any(TaskParams.class));
        doReturn(null).when(service).finishTask(any(TaskParams.class));

        assertEquals(TimeTriggerOutcome.FIRED, service.executeTimeTrigger(TASK_ID, DUE_AT_MILLIS));

        var order = inOrder(service, dataService);
        order.verify(service).assignTask(any(TaskParams.class));
        order.verify(dataService).getData(TASK_ID);
        order.verify(service).finishTask(any(TaskParams.class));
    }

    @Test
    void schedulesEveryTimeTriggerOfTheTaskWithoutDroppingAnyAndWarnsAboutTheCount() {
        Task task = task();
        when(task.getTriggers()).thenReturn(List.of(
                new DateTimeTrigger("2025-01-15T12:34:00"),
                new DateTimeTrigger("2025-01-16T12:34:00")
        ));
        Case useCase = caseForScheduling();

        Logger logger = (Logger) LoggerFactory.getLogger(TaskService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            ReflectionTestUtils.invokeMethod(service, "scheduleTimeTriggers", task, useCase);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        long warnCount = appender.list.stream()
                .filter(event -> event.getLevel().equals(Level.WARN))
                .filter(event -> event.getFormattedMessage().contains("[" + TASK_ID + "]"))
                .filter(event -> event.getFormattedMessage().contains("[" + CASE_ID + "]"))
                .filter(event -> event.getFormattedMessage().contains("has [2] time triggers"))
                .count();
        assertEquals(1, warnCount);
        verify(timeTriggerSchedulingService).schedule(task, LocalDateTime.of(2025, Month.JANUARY, 15, 12, 34));
        verify(timeTriggerSchedulingService).schedule(task, LocalDateTime.of(2025, Month.JANUARY, 16, 12, 34));
    }

    @Test
    void doesNotWarnWhenTaskHasASingleTimeTrigger() {
        Task task = task();
        when(task.getTriggers()).thenReturn(List.of(new DateTimeTrigger("2025-01-15T12:34:00")));

        Logger logger = (Logger) LoggerFactory.getLogger(TaskService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            ReflectionTestUtils.invokeMethod(service, "scheduleTimeTriggers", task, caseForScheduling());
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        assertTrue(appender.list.stream().noneMatch(event -> event.getLevel().equals(Level.WARN)));
        verify(timeTriggerSchedulingService).schedule(eq(task), any(LocalDateTime.class));
    }

    @Test
    void logsErrorWithTaskAndCaseIdWhenSchedulingTheTimeTriggerFails() {
        Task task = task();
        Case useCase = caseForScheduling();
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.FEBRUARY, 3, 4, 5);
        when(timeTriggerSchedulingService.schedule(task, dueAt)).thenReturn(false);

        Logger logger = (Logger) LoggerFactory.getLogger(TaskService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            service.scheduleTaskExecution(task, dueAt, useCase);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        long errorCount = appender.list.stream()
                .filter(event -> event.getLevel().equals(Level.ERROR))
                .filter(event -> event.getFormattedMessage().contains("[" + TASK_ID + "]"))
                .filter(event -> event.getFormattedMessage().contains("[" + CASE_ID + "]"))
                .count();
        assertEquals(1, errorCount);
    }

    @Test
    void alwaysSchedulesThroughQuartzAndHasNoInMemoryScheduler() {
        Task task = task();
        Case useCase = caseForScheduling();
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.FEBRUARY, 3, 4, 5);

        service.scheduleTaskExecution(task, dueAt, useCase);

        verify(timeTriggerSchedulingService).schedule(task, dueAt);
        boolean holdsTaskScheduler = Arrays.stream(TaskService.class.getDeclaredFields())
                .anyMatch(field -> TaskScheduler.class.isAssignableFrom(field.getType()));
        assertFalse(holdsTaskScheduler);
    }

    @Test
    void reloadsTasksOnlyAfterSavingThemAndTheirCase() {
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.FEBRUARY, 3, 4, 5);
        Transition transition = schedulableTransition(dueAt);
        PetriNet petriNet = mock(PetriNet.class);
        when(petriNet.getTransitions()).thenReturn(new LinkedHashMap<>(Map.of(TRANSITION_ID, transition)));
        Case useCase = caseForReload(petriNet);
        when(workflowService.resolveActorRef(useCase, false)).thenReturn(useCase);
        when(workflowService.save(useCase)).thenReturn(useCase);
        ProcessRole defaultRole = mock(ProcessRole.class);
        ProcessRole anonymousRole = mock(ProcessRole.class);
        AbstractUser loggedOrSystemUser = mock(AbstractUser.class);
        when(processRoleService.getDefaultRole()).thenReturn(defaultRole);
        when(processRoleService.getAnonymousRole()).thenReturn(anonymousRole);
        when(userService.getLoggedOrSystem()).thenReturn(loggedOrSystemUser);
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(true).when(service).isExecutable(transition, petriNet);

        service.reloadTasks(useCase, false);

        var order = inOrder(taskRepository, workflowService, timeTriggerSchedulingService);
        order.verify(taskRepository).saveAll(anyList());
        order.verify(workflowService).save(useCase);
        order.verify(timeTriggerSchedulingService).schedule(any(Task.class), eq(dueAt));
    }

    @Test
    void doesNotScheduleTimeTriggerForTaskAutoExecutedDuringReload() {
        LocalDateTime dueAt = LocalDateTime.of(2025, Month.FEBRUARY, 3, 4, 5);
        Transition transition = schedulableTransition(dueAt);
        when(transition.hasAutoTrigger()).thenReturn(true);
        when(transition.getTriggers()).thenReturn(List.of(new AutoTrigger(), new DateTimeTrigger(dueAt.toString())));
        PetriNet petriNet = mock(PetriNet.class);
        when(petriNet.getTransitions()).thenReturn(new LinkedHashMap<>(Map.of(TRANSITION_ID, transition)));
        Case useCase = caseForReload(petriNet);
        when(workflowService.resolveActorRef(useCase, false)).thenReturn(useCase);
        when(workflowService.save(useCase)).thenReturn(useCase);
        ProcessRole defaultRole = mock(ProcessRole.class);
        ProcessRole anonymousRole = mock(ProcessRole.class);
        AbstractUser loggedOrSystemUser = mock(AbstractUser.class);
        when(processRoleService.getDefaultRole()).thenReturn(defaultRole);
        when(processRoleService.getAnonymousRole()).thenReturn(anonymousRole);
        when(userService.getLoggedOrSystem()).thenReturn(loggedOrSystemUser);
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(true).when(service).isExecutable(transition, petriNet);
        doNothing().when(service).executeTransition(any(Task.class), eq(useCase));

        service.reloadTasks(useCase, false);

        verify(service).executeTransition(any(Task.class), eq(useCase));
        verify(timeTriggerSchedulingService, never()).schedule(any(), any());
    }

    @Test
    void deletingTasksWithCaseObjectUnschedulesAllTimeTriggeredTasksInOneCall() {
        List<Task> tasks = List.of(timeTriggeredTask("task-1"), timeTriggeredTask("task-2"));
        Case useCase = mock(Case.class);
        when(useCase.getStringId()).thenReturn(CASE_ID);

        service.delete(tasks, useCase);

        verify(timeTriggerSchedulingService).unscheduleAll(List.of("task-1", "task-2"));
        verify(timeTriggerSchedulingService, never()).unschedule(any());
    }

    @Test
    void deletingTasksByCaseIdUnschedulesAllTimeTriggeredTasksInOneCall() {
        List<Task> tasks = List.of(timeTriggeredTask("task-1"), timeTriggeredTask("task-2"));

        service.delete(tasks, CASE_ID, true);

        verify(timeTriggerSchedulingService).unscheduleAll(List.of("task-1", "task-2"));
        verify(timeTriggerSchedulingService, never()).unschedule(any());
    }

    @Test
    void unschedulingSendsEveryDeletedTaskIdRegardlessOfTriggers() {
        List<Task> tasks = List.of(
                timeTriggeredTask("task-1"),
                taskWithTriggers("task-auto", List.of(new AutoTrigger())),
                taskWithTriggers("task-none", List.of()),
                timeTriggeredTask("task-2"));

        service.delete(tasks, CASE_ID, true);

        verify(timeTriggerSchedulingService).unscheduleAll(List.of("task-1", "task-auto", "task-none", "task-2"));
    }

    @Test
    void unschedulingTasksWithoutTimeTriggerStillSendsTheirIds() {
        List<Task> tasks = List.of(taskWithTriggers("task-auto", List.of(new AutoTrigger())), taskWithTriggers("task-none", List.of()));

        service.delete(tasks, CASE_ID, true);

        verify(timeTriggerSchedulingService).unscheduleAll(List.of("task-auto", "task-none"));
    }

    @Test
    void deletingTasksByProcessUnschedulesTimeTriggeredTasksBeforeDeletingTheProcessTasks() {
        List<Task> tasks = List.of(timeTriggeredTask("task-1"), taskWithTriggers("task-auto", List.of(new AutoTrigger())), timeTriggeredTask("task-2"));
        when(taskRepository.findAllByProcessId(PROCESS_ID)).thenReturn(tasks);

        service.deleteTasksByPetriNetId(PROCESS_ID);

        var order = inOrder(taskRepository, timeTriggerSchedulingService);
        order.verify(taskRepository).findAllByProcessId(PROCESS_ID);
        order.verify(timeTriggerSchedulingService).unscheduleAll(List.of("task-1", "task-auto", "task-2"));
        order.verify(taskRepository).deleteAllByProcessId(PROCESS_ID);
    }

    private Task task() {
        Task task = mock(Task.class);
        when(task.getCaseId()).thenReturn(CASE_ID);
        when(task.getStringId()).thenReturn(TASK_ID);
        when(task.getTransitionId()).thenReturn(TRANSITION_ID);
        return task;
    }

    private TransientDataAccessException transientFailure(String message) {
        return new TransientDataAccessException(message) {
        };
    }

    private Case useCase(Transition transition) {
        PetriNet petriNet = mock(PetriNet.class);
        when(petriNet.getTransition(TRANSITION_ID)).thenReturn(transition);
        Case useCase = mock(Case.class);
        when(useCase.getPetriNet()).thenReturn(petriNet);
        return useCase;
    }

    private Case caseForScheduling() {
        Case useCase = mock(Case.class);
        when(useCase.getStringId()).thenReturn(CASE_ID);
        return useCase;
    }

    private Case caseForReload(PetriNet petriNet) {
        Case useCase = mock(Case.class);
        when(useCase.getStringId()).thenReturn(CASE_ID);
        when(useCase.getTitle()).thenReturn("case title");
        when(useCase.getPetriNet()).thenReturn(petriNet);
        when(useCase.getPetriNetId()).thenReturn(PROCESS_ID);
        when(useCase.getProcessIdentifier()).thenReturn("process-identifier");
        when(useCase.get_id()).thenReturn(new ProcessResourceId());
        when(useCase.getEnabledRoles()).thenReturn(Set.of());
        when(useCase.getTasks()).thenReturn(Set.of());
        return useCase;
    }

    private Transition schedulableTransition(LocalDateTime dueAt) {
        Transition transition = mock(Transition.class);
        when(transition.getImportId()).thenReturn(TRANSITION_ID);
        when(transition.getEvents()).thenReturn(Map.of());
        when(transition.getTriggers()).thenReturn(List.of(new DateTimeTrigger(dueAt.toString())));
        when(transition.getImmediateData()).thenReturn(List.of());
        when(transition.getAssignedUserPolicy()).thenReturn(Map.of());
        when(transition.getRoles()).thenReturn(Map.of());
        when(transition.getNegativeViewRoles()).thenReturn(List.of());
        when(transition.getActorRefs()).thenReturn(Map.of());
        return transition;
    }

    private Task timeTriggeredTask(String taskId) {
        return taskWithTriggers(taskId, List.of(new DateTimeTrigger("2025-01-15T12:34:00")));
    }

    private Task taskWithTriggers(String taskId, List<Trigger> triggers) {
        Task task = mock(Task.class);
        when(task.getStringId()).thenReturn(taskId);
        when(task.getTriggers()).thenReturn(triggers);
        return task;
    }
}
