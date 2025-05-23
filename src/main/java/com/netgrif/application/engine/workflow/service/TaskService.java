package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.history.domain.taskevents.AssignTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.CancelTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.FinishTaskEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.arcs.*;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalMarkingException;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.rules.domain.facts.TransitionEventFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.transaction.NaeTransaction;
import com.netgrif.application.engine.transaction.configuration.NaeTransactionProperties;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.domain.outcomes.CreateTasksOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.DoEventTaskOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.UpdateTaskStateOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.taskoutcomes.*;
import com.netgrif.application.engine.workflow.domain.params.GetDataParams;
import com.netgrif.application.engine.workflow.domain.params.TaskParams;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.domain.triggers.AutoTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.TimeTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService implements ITaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSearchService searchService;

    @Autowired
    @Qualifier("taskScheduler")
    private TaskScheduler scheduler;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private IEventService eventService;

    @Autowired
    private IHistoryService historyService;

    @Autowired
    private ISessionManagerService sessionManagerService;

    @Autowired
    private IUserService userService;

    @Autowired
    private MongoTransactionManager transactionManager;

    @Autowired
    private NaeTransactionProperties transactionProperties;

    @Autowired
    private IRuleEngine ruleEngine;

    /**
     * Executes provided {@link Task} in provided {@link Case}
     *
     * @param task Task to be executed
     * @param useCase Case where the task exists
     *
     * @return list of outcomes from the triggered events during the task execution
     * */
    @Override
    public List<EventOutcome> executeTask(Task task, Case useCase) {
        log.info("[{}]: executeTask [{}] in case [{}]", useCase.getStringId(), task.getTransitionId(), useCase.getTitle());
        List<EventOutcome> outcomes = new ArrayList<>();
        try {
            log.info("assignTask [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            AssignTaskEventOutcome assignOutcome = assignTask(new TaskParams(task));
            outcomes.add(assignOutcome);
            log.info("getData [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            GetDataEventOutcome getDataOutcome = dataService.getData(new GetDataParams(assignOutcome.getTask(),
                    assignOutcome.getCase(), userService.getSystemUser().getStringId()));
            outcomes.add(getDataOutcome);
            log.info("finishTask [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            outcomes.add(finishTask(new TaskParams(getDataOutcome.getTask())));
        } catch (TransitionNotExecutableException e) {
            log.error("execution of task [{}] in case [{}] failed: ", task.getTitle(), useCase.getTitle(), e);
        }
        return outcomes;
    }

    /**
     * Assigns all provided tasks by provided user.
     *
     * @param tasks list of tasks to be assigned
     * @param actorId id of the assignee of the tasks
     * @param params additional parameters for the assign task event
     *
     * @return list of outcomes of the assign events. The order is the same as the order of the provided list of tasks
     * */
    @Override
    public List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, String actorId, Map<String, String> params) throws TransitionNotExecutableException {
        List<AssignTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(
                    assignTask(TaskParams.with()
                            .task(task)
                            .assigneeId(actorId)
                            .params(params)
                            .build())
            );
        }
        return outcomes;
    }

    /**
     * Assigns the {@link Task} by provided parameters
     *
     * @param taskParams parameters to determine the Task to be assigned
     * <br>
     * <b>Required parameters</b>
     * <ul>
     *      <li>taskId or task</li>
     *      <li>user</li>
     * </ul>
     *
     * @return outcome of the assign event
     * */
    @Override
    public AssignTaskEventOutcome assignTask(TaskParams taskParams) throws TransitionNotExecutableException {
        fillMissingAttributes(taskParams);

        if (taskParams.getIsTransactional() && !TransactionSynchronizationManager.isSynchronizationActive()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<AssignTaskEventOutcome>(null) {
                        @Override
                        public AssignTaskEventOutcome call() {
                            try {
                                return doAssignTask(taskParams);
                            } catch (TransitionNotExecutableException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .build();
            transaction.begin();
            return (AssignTaskEventOutcome) transaction.getResultOfEvent();
        } else {
            return doAssignTask(taskParams);
        }
    }

    private AssignTaskEventOutcome doAssignTask(TaskParams taskParams) throws TransitionNotExecutableException {
        Task task = taskParams.getTask();
        Case useCase = taskParams.getUseCase();
        String actorId = taskParams.getAssigneeId();

        Transition transition = useCase.getProcess().getTransition(task.getTransitionId());

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreAssignActions(), useCase,
                task, transition, taskParams.getParams()));

        if (!outcomes.isEmpty()) {
            useCase = workflowService.findOne(task.getCaseId());
            task = findOne(task.getStringId());
        }

        useCase = evaluateRules(useCase, task, EventType.ASSIGN, EventPhase.PRE);
        DoEventTaskOutcome doOutcome = doAssignTaskToUser(actorId, task, transition, useCase);
        useCase = doOutcome.getUseCase();
        task = doOutcome.getTask();

        historyService.save(new AssignTaskEventLog(task, useCase, EventPhase.PRE, actorId));

        List<EventOutcome> postEventOutcomes = eventService.runActions(transition.getPostAssignActions(), useCase, task,
                transition, taskParams.getParams());
        if (!postEventOutcomes.isEmpty()) {
            outcomes.addAll(postEventOutcomes);
            useCase = workflowService.findOne(useCase.getStringId());
        }

        useCase = evaluateRules(useCase, task, EventType.ASSIGN, EventPhase.POST);

        historyService.save(new AssignTaskEventLog(task, useCase, EventPhase.POST, actorId));

        AssignTaskEventOutcome outcome = new AssignTaskEventOutcome(useCase, task, outcomes);
        addMessageToOutcome(transition, EventType.ASSIGN, outcome);

        log.info("[{}]: Task [{}] in case [{}] assigned to [{}]", useCase.getStringId(), task.getTitle(), useCase.getTitle(),
                actorId);
        return outcome;
    }

    private DoEventTaskOutcome doAssignTaskToUser(String actorId, Task task, Transition transition, Case useCase) throws TransitionNotExecutableException {
        useCase.getProcess().initializeArcs();

        log.info("[{}]: Assigning task [{}] to actor [{}]", useCase.getStringId(), task.getTitle(), actorId);

        startExecution(transition, useCase);
        task.setAssigneeId(actorId);
        task.setLastAssigned(LocalDateTime.now());

        useCase = workflowService.save(useCase);
        save(task);

        boolean anyTaskExecuted = reloadTasks(useCase);
        if (anyTaskExecuted) {
            useCase = workflowService.findOne(useCase.getStringId());
        }
        return new DoEventTaskOutcome(task, useCase);
    }

    /**
     * Finishes all provided tasks by provided user.
     *
     * @param tasks list of tasks to be finished
     * @param actorId id of assignee of the tasks
     * @param params additional parameters for the finish task event
     *
     * @return list of outcomes of the finish events. The order is the same as the order of the provided list of tasks
     * */
    @Override
    public List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, String actorId, Map<String, String> params) throws TransitionNotExecutableException {
        List<FinishTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(
                    finishTask(TaskParams.with()
                            .task(task)
                            .assigneeId(actorId)
                            .params(params)
                            .build())
            );
        }
        return outcomes;
    }

    /**
     * Finishes the {@link Task} by provided parameters
     *
     * @param taskParams parameters to determine the Task to be finished
     * <br>
     * <b>Required parameters</b>
     * <ul>
     *      <li>taskId or task</li>
     *      <li>user</li>
     * </ul>
     *
     * @return outcome of the finish event
     * */
    @Override
    public FinishTaskEventOutcome finishTask(TaskParams taskParams) throws TransitionNotExecutableException {
        fillMissingAttributes(taskParams);

        if (taskParams.getIsTransactional() && !TransactionSynchronizationManager.isSynchronizationActive()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<FinishTaskEventOutcome>(null) {
                        @Override
                        public FinishTaskEventOutcome call() {
                            try {
                                return doFinishTask(taskParams);
                            } catch (TransitionNotExecutableException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .build();
            transaction.begin();
            return (FinishTaskEventOutcome) transaction.getResultOfEvent();
        } else {
            return doFinishTask(taskParams);
        }
    }

    private FinishTaskEventOutcome doFinishTask(TaskParams taskParams) throws TransitionNotExecutableException {
        Task task = taskParams.getTask();
        Case useCase = taskParams.getUseCase();
        String actorId = taskParams.getAssigneeId();

        if (task.getAssigneeId() == null) {
            throw new IllegalArgumentException("Task with id=" + task.getStringId() + " is not assigned to any actor.");
        }
        // TODO: 14. 4. 2017 replace with @PreAuthorize
        if (!task.getAssigneeId().equals(actorId)) {
            throw new IllegalArgumentException("Actor that is not assigned tried to finish task");
        }

        Transition transition = useCase.getProcess().getTransition(task.getTransitionId());

        log.info("[{}]: Finishing task [{}] to actor [{}]", useCase.getStringId(), task.getTitle(), actorId);

        // todo: release/8.0.0 backend validation on finish
        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreFinishActions(), useCase,
                task, transition, taskParams.getParams()));
        if (!outcomes.isEmpty()) {
            useCase = workflowService.findOne(task.getCaseId());
            task = findOne(task.getStringId());
        }

        useCase = evaluateRules(useCase, task, EventType.FINISH, EventPhase.PRE);
        DoEventTaskOutcome doOutcome = doFinishTaskByAssignedUser(task, transition, useCase);
        useCase = doOutcome.getUseCase();
        task = doOutcome.getTask();

        historyService.save(new FinishTaskEventLog(task, useCase, EventPhase.PRE, actorId));

        List<EventOutcome> postFinishOutcomes = eventService.runActions(transition.getPostFinishActions(), useCase, task,
                transition, taskParams.getParams());
        if (!postFinishOutcomes.isEmpty()) {
            outcomes.addAll(postFinishOutcomes);
            useCase = workflowService.findOne(task.getCaseId());
        }

        useCase = evaluateRules(useCase, task, EventType.FINISH, EventPhase.POST);

        historyService.save(new FinishTaskEventLog(task, useCase, EventPhase.POST, actorId));

        FinishTaskEventOutcome outcome = new FinishTaskEventOutcome(useCase, task, outcomes);
        addMessageToOutcome(transition, EventType.FINISH, outcome);

        log.info("[{}]: Task [{}] in case [{}] assigned to [{}] was finished", useCase.getStringId(), task.getTitle(),
                useCase.getTitle(), actorId);
        return outcome;
    }

    private DoEventTaskOutcome doFinishTaskByAssignedUser(Task task, Transition transition, Case useCase) {
        useCase = finishExecution(transition, useCase);

        task.setLastFinished(LocalDateTime.now());
        task.setFinishedBy(task.getAssigneeId());
        task.setAssigneeId(null);
        save(task);

        boolean anyTaskExecuted = reloadTasks(useCase);
        if (anyTaskExecuted) {
            useCase = workflowService.findOne(useCase.getStringId());
        }
        return new DoEventTaskOutcome(task, useCase);
    }

    /**
     * Cancels all provided tasks by provided user.
     *
     * @param tasks list of tasks to be canceled
     * @param actorId id of actor by which the task is canceled
     * @param params additional parameters for the cancel task event
     *
     * @return list of outcomes of the cancel events. The order is the same as the order of the provided list of tasks
     * */
    @Override
    public List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, String actorId, Map<String, String> params) {
        List<CancelTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(
                    cancelTask(TaskParams.with()
                            .task(task)
                            .assigneeId(actorId)
                            .params(params)
                            .build())
            );
        }
        return outcomes;
    }

    /**
     * Cancels the {@link Task} by provided parameters
     *
     * @param taskParams parameters to determine the Task to be canceled
     * <br>
     * <b>Required parameters</b>
     * <ul>
     *      <li>taskId or task</li>
     *      <li>user</li>
     * </ul>
     *
     * @return outcome of the cancel event
     * */
    @Override
    public CancelTaskEventOutcome cancelTask(TaskParams taskParams) {
        fillMissingAttributes(taskParams);

        if (taskParams.getIsTransactional() && !TransactionSynchronizationManager.isSynchronizationActive()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<CancelTaskEventOutcome>(null) {
                        @Override
                        public CancelTaskEventOutcome call() {
                            return doCancelTask(taskParams);
                        }
                    })
                    .build();
            transaction.begin();
            return (CancelTaskEventOutcome) transaction.getResultOfEvent();
        } else {
            return doCancelTask(taskParams);
        }
    }

    private CancelTaskEventOutcome doCancelTask(TaskParams taskParams) {
        Task task = taskParams.getTask();
        String actorId = taskParams.getAssigneeId();
        Case useCase = taskParams.getUseCase();

        Transition transition = useCase.getProcess().getTransition(task.getTransitionId());

        log.info("[{}]: Canceling task [{}] to actor [{}]", useCase.getStringId(), task.getTitle(), actorId);

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreCancelActions(), useCase,
                task, transition, taskParams.getParams()));
        if (!outcomes.isEmpty()) {
            useCase = workflowService.findOne(task.getCaseId());
            task = findOne(task.getStringId());
        }

        useCase = evaluateRules(useCase, task, EventType.CANCEL, EventPhase.PRE);
        DoEventTaskOutcome doOutcome = doCancelTaskToUser(task, useCase);
        useCase = doOutcome.getUseCase();
        task = doOutcome.getTask();

        historyService.save(new CancelTaskEventLog(task, useCase, EventPhase.PRE, actorId));

        List<EventOutcome> postEventOutcomes = eventService.runActions(transition.getPostCancelActions(), useCase, task,
                transition, taskParams.getParams());
        if (!postEventOutcomes.isEmpty()) {
            outcomes.addAll(postEventOutcomes);
            useCase = workflowService.findOne(task.getCaseId());
        }

        useCase = evaluateRules(useCase, task, EventType.CANCEL, EventPhase.POST);

        CancelTaskEventOutcome outcome = new CancelTaskEventOutcome(useCase, task);
        outcome.setOutcomes(outcomes);
        addMessageToOutcome(transition, EventType.CANCEL, outcome);

        historyService.save(new CancelTaskEventLog(task, useCase, EventPhase.POST, actorId));

        log.info("[{}]: Task [{}] in case [{}] assigned to [{}] was cancelled", useCase.getStringId(), task.getTitle(),
                useCase.getTitle(), actorId);
        return outcome;
    }

    private DoEventTaskOutcome doCancelTaskToUser(Task task, Case useCase) {
        Process process = useCase.getProcess();
        Case finalUseCase = useCase;
        process.getInputArcsOf(task.getTransitionId()).stream()
                .filter(arc -> arc.getSource() != null)
                .forEach(arc -> {
                    arc.rollbackExecution(finalUseCase.getConsumedTokens().get(arc.getStringId()));
                    finalUseCase.getConsumedTokens().remove(arc.getStringId());
                });
        workflowService.updateMarking(useCase);

        task.setAssigneeId(null);
        // TODO: release/8.0.0 should this be null?
        task.setLastAssigned(null);

        task = save(task);
        workflowService.save(useCase);

        boolean anyTaskExecuted = reloadTasks(useCase);
        if (anyTaskExecuted) {
            useCase = workflowService.findOne(useCase.getStringId());
        }

        return new DoEventTaskOutcome(task, useCase);
    }

    @Override
    public DelegateTaskEventOutcome delegateTask(String actorId, String delegatedId, String taskId) throws TransitionNotExecutableException {
        return delegateTask(actorId, delegatedId, taskId, new HashMap<>());
    }

    @Override
    public DelegateTaskEventOutcome delegateTask(String actorId, String delegatedId, String taskId, Map<String, String> params) throws TransitionNotExecutableException {
        Optional<User> delegatedActorOpt = userService.findById(delegatedId);
        if (delegatedActorOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("Delegated actor with id [%s] does not exist.", delegatedId));
        }

        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        Task task = taskOptional.get();

        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getProcess().getTransition(task.getTransitionId());

        log.info("[{}]: Delegating task [{}] to actor [{}]", useCase.getStringId(), task.getTitle(),
                delegatedActorOpt.get().getEmail());

//        TODO: release/8.0.0 fix
//        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreDelegateActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
//        task = findOne(task.getStringId());
//        evaluateRules(useCase.getStringId(), task, EventType.DELEGATE, EventPhase.PRE);
//        delegate(delegatedUser, task, useCase);
//        historyService.save(new DelegateTaskEventLog(task, useCase, EventPhase.PRE, delegateUser, delegatedUser.getStringId()));
//        outcomes.addAll(eventService.runActions(transition.getPostDelegateActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
//        evaluateRules(useCase.getStringId(), task, EventType.DELEGATE, EventPhase.POST);

        reloadTasks(workflowService.findOne(task.getCaseId()));

//        DelegateTaskEventOutcome outcome = new DelegateTaskEventOutcome(workflowService.findOne(task.getCaseId()), task, outcomes);
//        addMessageToOutcome(transition, EventType.DELEGATE, outcome);
//        historyService.save(new DelegateTaskEventLog(task, useCase, EventPhase.POST, delegateUser, delegatedUser.getStringId()));
//        log.info("Task [{}] in case [{}] assigned to [{}] was delegated to [{}]", task.getTitle(), useCase.getTitle(), delegateUser.getSelfOrImpersonated().getEmail(), delegatedUser.getEmail());
//
//        return outcome;
        return null;
    }

    private void delegate(User delegatedActor, Task task, Case useCase) throws TransitionNotExecutableException {
//        TODO: release/8.0.0
//        if (task.getUserId() != null) {
//            task.setAssigneeId(delegated.getStringId());
//            save(task);
//        } else {
//            assignTaskToUser(delegated, task, useCase.getStringId());
//        }
    }

    private void fillMissingAttributes(TaskParams taskParams) {
        if (taskParams.getTask() == null) {
            Task task = findOne(taskParams.getTaskId());
            taskParams.setTask(task);
        }
        if (taskParams.getUseCase() == null) {
            Case useCase = workflowService.findOne(taskParams.getTask().getCaseId());
            taskParams.setUseCase(useCase);
        }
        if (taskParams.getAssigneeId() == null) {
            taskParams.setAssigneeId(sessionManagerService.getActiveActorId());
        }
        if (taskParams.getIsTransactional() == null) {
            taskParams.setIsTransactional(transactionProperties.isTaskEventTransactional());
        }
    }

    private Case evaluateRules(Case useCase, Task task, EventType eventType, EventPhase eventPhase) {
        log.info("[{}]: Task [{}] in case [{}] evaluating rules of event {} of phase {}", useCase.getStringId(), task.getTitle(), useCase.getTitle(), eventType.name(), eventPhase.name());
        int rulesExecuted = ruleEngine.evaluateRules(useCase, task, TransitionEventFact.of(task, eventType, eventPhase));
        if (rulesExecuted == 0) {
            return useCase;
        }
        return workflowService.save(useCase);
    }

    /**
     * Updates the {@link State} of all the {@link Task} objects of the provided {@link Case}
     *
     * @param useCase Case where the tasks exist, which are updated
     *
     * @return true if at least one auto-trigger Task was executed.
     */
    @Override
    public boolean reloadTasks(Case useCase) {
        log.info("[{}]: Reloading tasks in [{}]", useCase.getStringId(), useCase.getTitle());
        List<String> taskIds = useCase.getTasks().values().stream()
                .map(taskPair -> taskPair.getTaskId().toString())
                .collect(Collectors.toList());

        Optional<Task> autoTriggerTaskOpt = reloadAndSaveTasks((List<Task>) taskRepository.findAllById(taskIds), useCase);

        if (autoTriggerTaskOpt.isPresent()) {
            executeTask(autoTriggerTaskOpt.get(), workflowService.findOne(useCase.getStringId()));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates {@link State} of the provided tasks, that exist in provided {@link Case}. Only tasks with the changed
     * state are updated in database.
     *
     * @param tasks list of tasks to be updated
     * @param useCase Case object where the tasks exist
     *
     * @return optional auto-trigger task, that is not yet executed.
     * */
    private Optional<Task> reloadAndSaveTasks(List<Task> tasks, Case useCase) {
        Task autoTriggered = null;
        Process process = useCase.getProcess();
        List<Task> changedTasks = new ArrayList<>();
        for (Task task : tasks) {
            Transition transition = process.getTransition(task.getTransitionId());
            UpdateTaskStateOutcome updateTaskStateOutcome = updateStateOfTask(task, transition, useCase);
            if (updateTaskStateOutcome.isMustBeExecuted()) {
                autoTriggered = task;
            }
            if (updateTaskStateOutcome.isWasChanged()) {
                changedTasks.add(task);
            }
        }
        save(changedTasks);

        return autoTriggered == null ? Optional.empty() : Optional.of(autoTriggered);
    }

    /**
     * For every {@link Transition} in {@link Process} is created {@link Task} and saved into provided {@link Case}.
     * Tasks are saved into database by {@link #reloadAndSaveTasks(List, Case)}. UseCase is not saved into database by
     * this method.
     *
     * @param useCase Case object, where the new tasks are saved. It must contain {@link Case#getProcess()} ()} initialized.
     *
     * @return created tasks and auto-trigger task as optional. Auto-trigger task is within the tasks collection
     * */
    @Override
    public CreateTasksOutcome createAndSetTasksInCase(Case useCase) {
        List<Task> tasks = useCase.getProcess().getTransitions().values().stream()
                .map(transition -> createTaskFromTransition(transition, useCase))
                .collect(Collectors.toList());

        useCase.addTasks(tasks);

        Optional<Task> autoTriggerTaskOpt = reloadAndSaveTasks(tasks, useCase);

        return new CreateTasksOutcome(useCase, tasks, autoTriggerTaskOpt.orElse(null));
    }

    /**
     * Creates the {@link Task} object by the provided {@link Transition} and {@link Case}. Task is not saved in database
     * and Case object.
     *
     * @param transition transition, from which the Task is created
     * @param useCase Case, where the created Task should be later saved
     *
     * @return created Task
     * */
    private Task createTaskFromTransition(Transition transition, Case useCase) {
        final Task task = Task.with()
                .title(transition.getTitle())
                .processId(useCase.getPetriNetId())
                .caseId(useCase.getId().toString())
                .transitionId(transition.getImportId())
                .properties(transition.getProperties().getMap())
                .processRolePermissions(new AccessPermissions<>(transition.getProcessRolePermissions()))
                .icon(transition.getIcon() == null ? useCase.getIcon() : transition.getIcon())
                .immediateDataFields(transition.getImmediateData())
                .assignPolicy(transition.getAssignPolicy())
                .finishPolicy(transition.getFinishPolicy())
                .build();
        for (Trigger trigger : transition.getTriggers()) {
            Trigger taskTrigger = trigger.clone();
            task.addTrigger(taskTrigger);

            if (taskTrigger instanceof TimeTrigger) {
                TimeTrigger timeTrigger = (TimeTrigger) taskTrigger;
                scheduleTaskExecution(task, timeTrigger.getStartDate(), useCase);
            } else if (taskTrigger instanceof AutoTrigger) {
                task.setAssigneeId(userService.getSystemUser().getStringId());
            }
        }

        return task;
    }


    /**
     * Updates the {@link State} of provided {@link Task}. The state depends on {@link #isExecutable(Transition, Case)}
     *
     * @param task Task, where the state might be updated
     * @param transition transition, by which the execution is determined
     * @param useCase case, by which the execution is determined
     *
     * @return boolean value if the state was updated and if the task must be executed
     * */
    private UpdateTaskStateOutcome updateStateOfTask(Task task, Transition transition, Case useCase) {
        if (isExecutable(transition, useCase)) {
            boolean willBeChanged = task.getState() != State.ENABLED;
            task.setState(State.ENABLED);
            return new UpdateTaskStateOutcome(willBeChanged, task.isAutoTriggered());
        } else {
            boolean willBeChanged = task.getState() != State.DISABLED;
            task.setState(State.DISABLED);
            return new UpdateTaskStateOutcome(willBeChanged, false);
        }
    }

//    @Override
//    public CreateTasksOutcome createTasks(Case useCase) {
//        Process net = useCase.getProcess();
//        List<Task> tasks = new ArrayList<>();
//
//        net.getTransitions().values()
//                .forEach(transition -> tasks.add(createFromTransition(transition, useCase)));
//
//        return new CreateTasksOutcome(workflowService.save(useCase), tasks);
//    }

    private boolean isExecutable(Transition transition, Case useCase) {
        List<PTArc> arcsOfTransition = useCase.getProcess().getInputArcsOf(transition.getImportId());
        if (arcsOfTransition == null) {
            return true;
        }
        Map<String, Integer> markingBefore = useCase.getActivePlaces();
        // TODO: NAE-1858 is this valid check? what about multiple input arcs from same place?
        // todo: from same source error
        //        TODO: release/8.0.0
        try {
            arcsOfTransition.forEach(Arc::execute);
        } catch (IllegalMarkingException e) {
            useCase.getProcess().setActivePlaces(markingBefore);
            return false;
        }
        return true;
    }

    private Case finishExecution(Transition transition, Case useCase) {
        log.info("[{}]: Finish execution of task [{}] in case [{}]", useCase.getStringId(), transition.getTitle(), useCase.getTitle());
        // TODO: release/8.0.0 set multiplicity
        useCase.getProcess().getOutputArcsOf(transition.getImportId()).forEach(Arc::execute);
        workflowService.updateMarking(useCase);
        return workflowService.save(useCase);
    }

    private void startExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
        log.info("[{}]: Start execution of {} in case {}", useCase.getStringId(), transition.getTitle(), useCase.getTitle());

        try {
            useCase.getProcess().getInputArcsOf(transition.getImportId()).stream()
                    .sorted((a1, a2) -> ArcOrderComparator.getInstance().compare(a1, a2))
                    .forEach(arc -> {
                        int consumed = arc.getMultiplicity();
                        if (arc instanceof ResetArc) {
                            consumed = arc.getSource().getTokens();
                        }
                        useCase.getConsumedTokens().put(arc.getStringId(), consumed);
                        arc.execute();
                    });
        } catch (IllegalMarkingException e) {
            throw new TransitionNotExecutableException("Not all arcs can be executed task [" + transition.getStringId() + "] in case [" + useCase.getTitle() + "]");
        }
        workflowService.updateMarking(useCase);
    }

    private void scheduleTaskExecution(Task task, LocalDateTime time, Case useCase) {
        log.info("[{}]: Task {} scheduled to run at {}", useCase.getStringId(), task.getTitle(), time.toString());
        scheduler.schedule(() -> {
            try {
                executeTask(task, useCase);
            } catch (Exception e) {
                log.info("[{}]: Scheduled task [{}] of case [{}] could not be executed: {}", useCase.getStringId(), task.getTitle(), useCase.getTitle(), e.toString());
            }
        }, DateUtils.localDateTimeToDate(time));
    }

    @Override
    public Task findOne(String taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        return optionalTask.get();
    }

    // TODO: release/8.0.0 check usage and delete/replace with current implementation
    @Override
    public Page<Task> getAll(String actorId, Pageable pageable, Locale locale) {
        List<Task> tasks;

//        if (loggedOrImpersonated.getRoles().isEmpty()) {
//            tasks = new ArrayList<>();
//            return new PageImpl<>(tasks, pageable, 0L);
//        } else {
//            StringBuilder queryBuilder = new StringBuilder();
//            queryBuilder.append("{$or:[");
//            loggedOrImpersonated.getRoles().forEach(role -> {
//                queryBuilder.append("{\"roles.");
//                queryBuilder.append(role);
//                queryBuilder.append("\":{$exists:true}},");
//            });
//            if (!loggedOrImpersonated.getRoles().isEmpty())
//                queryBuilder.deleteCharAt(queryBuilder.length() - 1);
//            else
//                queryBuilder.append("{}");
//            queryBuilder.append("]}");
//            BasicQuery query = new BasicQuery(queryBuilder.toString());
//            query = (BasicQuery) query.with(pageable);
//            tasks = mongoTemplate.find(query, Task.class);
//            return loadUsers(new PageImpl<>(tasks, pageable,
//                    mongoTemplate.count(new BasicQuery(queryBuilder.toString(), "{id:1}"), Task.class)));
//        }
        return Page.empty();
    }

    @Override
    public Page<Task> search(List<TaskSearchRequest> requests, Pageable pageable, String actorId, Locale locale, Boolean isIntersection) {
        com.querydsl.core.types.Predicate searchPredicate = searchService.buildQuery(requests, actorId, locale, isIntersection);
        if (searchPredicate != null) {
            Page<Task> page = taskRepository.findAll(searchPredicate, pageable);
            page = loadUsers(page);
            page = dataService.setImmediateFields(page);
            return page;
        } else {
            return Page.empty();
        }
    }

    @Override
    public long count(List<TaskSearchRequest> requests, String actorId, Locale locale, Boolean isIntersection) {
        com.querydsl.core.types.Predicate searchPredicate = searchService.buildQuery(requests, actorId, locale, isIntersection);
        if (searchPredicate == null) {
            return 0;
        }
        return taskRepository.count(searchPredicate);
    }

    @Override
    public Page<Task> findByCases(Pageable pageable, List<String> cases) {
        return loadUsers(taskRepository.findByCaseIdIn(pageable, cases));
    }

    @Override
    public Task findById(String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + id + "]");
        }
        Task task = taskOptional.get();
        this.setUser(task);
        return task;
    }

    @Override
    public List<Task> findAllById(List<String> ids) {
        return taskRepository.findAllByIdIn(ids).stream()
                .filter(Objects::nonNull)
                .sorted(Ordering.explicit(ids).onResultOf(Task::getStringId))
                .peek(this::setUser)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Task> findByAssignee(Pageable pageable, String actorId) {
        return loadUsers(taskRepository.findByAssigneeId(pageable, actorId));
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean existsByTaskAndAssignee(String taskId, String assigneeId) {
        return taskRepository.existsByIdAndAssigneeId(taskId, assigneeId);
    }

    @Override
    public Page<Task> findByTransitions(Pageable pageable, List<String> transitions) {
        return loadUsers(taskRepository.findByTransitionIdIn(pageable, transitions));
    }

    @Override
    public Page<Task> searchAll(com.querydsl.core.types.Predicate predicate) {
        Page<Task> tasks = taskRepository.findAll(predicate, new FullPageRequest());
        return loadUsers(tasks);
    }

    @Override
    public Page<Task> search(com.querydsl.core.types.Predicate predicate, Pageable pageable) {
        Page<Task> tasks = taskRepository.findAll(predicate, pageable);
        return loadUsers(tasks);
    }

    @Override
    public Task searchOne(com.querydsl.core.types.Predicate predicate) {
        Page<Task> tasks = taskRepository.findAll(predicate, PageRequest.of(0, 1));
        if (tasks.getTotalElements() > 0) {
            return tasks.getContent().get(0);
        }
        return null;
    }

    @Override
    public List<TaskReference> findAllByCase(String caseId, Locale locale) {
        return taskRepository.findAllByCaseId(caseId).stream()
                .map(task -> new TaskReference(task.getStringId(), task.getTitle().getTranslation(locale), task.getTransitionId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findAllByCase(String caseId) {
        return taskRepository.findAllByCaseId(caseId);
    }

    @Override
    public Task save(Task task) {
        task = taskRepository.save(task);
        return task;
    }

    @Override
    public List<Task> save(List<Task> tasks) {
        tasks = taskRepository.saveAll(tasks);
        return tasks;
    }

    private List<String> getExistingUsers(UserListFieldValue userListValue) {
        if (userListValue == null) {
            return null;
        }
        // TODO: release/8.0.0 fix null set as user value, remove duplicate code, move this to userservice, optimize to one request to mongo
        return userListValue.getUserValues().stream()
                .filter(Objects::nonNull)
                .map(UserFieldValue::getId)
                .filter(id -> id != null && userService.existsById(id))
                .collect(Collectors.toList());
    }

    private Page<Task> loadUsers(Page<Task> tasks) {
        // TODO: NAE-1969
//        Map<String, IUser> users = new HashMap<>();
//        tasks.forEach(task -> {
//            if (task.getUserId() != null) {
//                if (users.containsKey(task.getUserId()))
//                    task.setUser(users.get(task.getUserId()));
//                else {
//                    task.setUser(userService.resolveById(task.getUserId()));
//                    users.put(task.getUserId(), task.getUser());
//                }
//            }
//        });

        return tasks;
    }

    @Override
    public void delete(List<Task> tasks, Case useCase) {
//        TODO: release/8.0.0
//        workflowService.removeTasksFromCase(tasks, useCase);
        log.info("[{}]: Tasks of case {} are being deleted", useCase.getStringId(), useCase.getTitle());
        taskRepository.deleteAll(tasks);
    }

    @Override
    public void delete(List<Task> tasks, String caseId) {
        log.info("[{}]: Tasks of case are being deleted", caseId);
        taskRepository.deleteAll(tasks);
    }

    @Override
    public void deleteTasksByCase(String caseId) {
        delete(taskRepository.findAllByCaseId(caseId), caseId);
    }

    @Override
    public void deleteTasksByPetriNetId(String petriNetId) {
        taskRepository.deleteAllByProcessId(petriNetId);
    }

    private void setUser(Task task) {
//        TODO: release/8.0.0
//        if (task.getUserId() == null) {
//            return;
//        }
//        task.setUser(userService.resolveById(task.getUserId()));
    }

    private EventOutcome addMessageToOutcome(Transition transition, EventType type, TaskEventOutcome outcome) {
        if (transition.getEvents().containsKey(type)) {
            outcome.setMessage(transition.getEvents().get(type).getMessage());
        }
        return outcome;
    }

    // TODO: release/8.0.0 refactor, not needed here, should be possible to create main outcome when setting data
    @Override
    public SetDataEventOutcome getMainOutcome(Map<String, SetDataEventOutcome> outcomes, String taskId) {
        SetDataEventOutcome mainOutcome;
        String key = taskId;
        if (!outcomes.containsKey(taskId)) {
            Optional<String> optional = outcomes.keySet().stream().findFirst();
            if (optional.isPresent()) {
                key = optional.get();
            }
        }
        mainOutcome = outcomes.remove(key);
        mainOutcome.addOutcomes(new ArrayList<>(outcomes.values()));
        return mainOutcome;
    }
}
