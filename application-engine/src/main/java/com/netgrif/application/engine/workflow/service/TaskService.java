package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorFieldValue;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorListFieldValue;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.objects.event.events.task.*;
import com.netgrif.application.engine.objects.petrinet.domain.*;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.ArcOrderComparator;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.ResetArc;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.validation.service.interfaces.IValidationService;
import com.netgrif.application.engine.objects.workflow.domain.*;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.*;
import com.netgrif.application.engine.workflow.domain.outcomes.ReloadTaskOutcome;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.objects.workflow.domain.triggers.TimeTrigger;
import com.netgrif.application.engine.objects.workflow.domain.triggers.Trigger;
import com.netgrif.application.engine.workflow.params.DelegateTaskParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TaskService implements ITaskService {

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected GroupService groupService;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected TaskSearchService searchService;

    @Autowired
    @Qualifier("taskScheduler")
    protected TaskScheduler scheduler;

    @Autowired
    protected IWorkflowService workflowService;

    @Autowired
    protected IDataService dataService;

    @Autowired
    protected ProcessRoleService processRoleService;

    @Autowired
    protected IElasticTaskMappingService taskMappingService;

    @Autowired
    protected IEventService eventService;

    protected IElasticTaskService elasticTaskService;

    @Autowired
    protected IValidationService validationService;

    @Lazy
    @Autowired
    public void setElasticTaskService(IElasticTaskService elasticTaskService) {
        this.elasticTaskService = elasticTaskService;
    }

    @Override
    public List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, AbstractUser user) throws TransitionNotExecutableException {
        return assignTasks(tasks, user, new HashMap<>());
    }

    @Override
    public List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, AbstractUser user, Map<String, String> params) throws TransitionNotExecutableException {
        List<AssignTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(assignTask(TaskParams.with()
                    .task(task)
                    .user(user)
                    .params(params)
                    .build()));
        }
        return outcomes;
    }

    @Override
    public AssignTaskEventOutcome assignTask(TaskParams taskParams) throws TransitionNotExecutableException {
        fillAndValidateAttributes(taskParams);

        Case useCase = taskParams.getUseCase();
        Task task = taskParams.getTask();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreAssignActions(), useCase,
                task, transition, taskParams.getParams()));
        if (!outcomes.isEmpty()) {
            task = findOne(task.getStringId());
        }
        AssignTaskEventOutcome outcome = new AssignTaskEventOutcome(useCase, task, outcomes);
        useCase = evaluateRules(new AssignTaskEvent(outcome, EventPhase.PRE));

        useCase = assignTaskToUser(taskParams.getUser(), task, useCase, transition);
        publisher.publishEvent(new AssignTaskEvent(outcome, EventPhase.PRE, taskParams.getUser()));
        outcomes.addAll((eventService.runActions(transition.getPostAssignActions(), useCase, task, transition, taskParams.getParams())));
        useCase = evaluateRules(new AssignTaskEvent(outcome, EventPhase.POST));

        publisher.publishEvent(new AssignTaskEvent(outcome, EventPhase.POST, taskParams.getUser()));
        addMessageToOutcome(transition, EventType.ASSIGN, outcome);
        // TODO: impersonation user.getSelfOrImpersonated().getEmail()
        log.info("[{}]: Task [{}] in case [{}] assigned to [{}]", useCase.getStringId(), task.getTitle(),
                useCase.getTitle(), taskParams.getUser().getEmail());

        return outcome;
    }

    protected Case assignTaskToUser(AbstractUser user, Task task, Case useCase, Transition transition) throws TransitionNotExecutableException {
        useCase.getPetriNet().initializeArcs();

        // TODO: impersonation user.getSelfOrImpersonated().getEmail()
        log.info("[{}]: Assigning task [{}] to user [{}]", useCase.getStringId(), task.getTitle(), user.getEmail());

        startExecution(transition, useCase);
        // TODO: impersonation
        task.setAssignee(ActorTransformer.toActorRef(user));
        task.setStartDate(LocalDateTime.now());
        // TODO: impersonation
        task.setUser(user);

        useCase = workflowService.save(useCase);
        save(task);
        ReloadTaskOutcome reloadTaskOutcome = reloadTasks(useCase, false);
        if (reloadTaskOutcome.isAnyTaskExecuted()) {
            useCase = workflowService.findOne(useCase.getStringId());
        }
        return useCase;
    }

    @Override
    public List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, AbstractUser user) throws TransitionNotExecutableException {
        return finishTasks(tasks, user, new HashMap<>());
    }

    @Override
    public List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, AbstractUser user, Map<String, String> params) throws TransitionNotExecutableException {
        List<FinishTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(finishTask(TaskParams.with()
                    .task(task)
                    .user(user)
                    .params(params)
                    .build()));
        }
        return outcomes;
    }

    @Override
    public FinishTaskEventOutcome finishTask(TaskParams taskParams) throws TransitionNotExecutableException {
        fillAndValidateAttributes(taskParams);

        Task task = taskParams.getTask();
        Case useCase = taskParams.getUseCase();
        AbstractUser user = taskParams.getUser();

        if (task.getUserId() == null) {
            throw new IllegalArgumentException("Task with id=%s is not assigned to any user.".formatted(task.getStringId()));
        }
        // TODO: impersonation
        if (!task.getUserId().equals(user.getStringId()) && !((Boolean) user.getAttributes().containsKey("anonymous"))) {
            throw new IllegalArgumentException("User that is not assigned tried to finish task");
        }
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        // TODO: impersonation
        log.info("[{}]: Finishing task [{}] to user [{}]", useCase.getStringId(), task.getTitle(), user.getEmail());

        validateData(transition, useCase);
        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreFinishActions(), useCase,
                task, transition, taskParams.getParams()));
        if (!outcomes.isEmpty()) {
            task = findOne(task.getStringId());
        }
        FinishTaskEventOutcome outcome = new FinishTaskEventOutcome(useCase, task, outcomes);
        useCase = evaluateRules(new FinishTaskEvent(outcome, EventPhase.PRE));

        useCase = finishExecution(transition, task, useCase);
        ReloadTaskOutcome reloadTaskOutcome = reloadTasks(useCase, false);
        if (reloadTaskOutcome.isAnyTaskExecuted()) {
            useCase = workflowService.findOne(useCase.getStringId());
        }

        publisher.publishEvent(new FinishTaskEvent(outcome, EventPhase.PRE, user));
        outcomes.addAll(eventService.runActions(transition.getPostFinishActions(), useCase, task, transition, taskParams.getParams()));
        useCase = evaluateRules(new FinishTaskEvent(outcome, EventPhase.POST));

        outcome = new FinishTaskEventOutcome(useCase, task, outcomes);
        addMessageToOutcome(transition, EventType.FINISH, outcome);
        publisher.publishEvent(new FinishTaskEvent(outcome, EventPhase.POST, user));
        // TODO: impersonation
        log.info("[{}]: Task [{}] in case [{}] assigned to [{}] was finished", useCase.getStringId(), task.getTitle(),
                useCase.getTitle(), user.getEmail());

        return outcome;
    }

    @Override
    public List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, AbstractUser user) {
        return cancelTasks(tasks, user, new HashMap<>());
    }

    @Override
    public List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, AbstractUser user, Map<String, String> params) {
        List<CancelTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(cancelTask(TaskParams.with()
                    .task(task)
                    .user(user)
                    .params(params)
                    .build()));
        }
        return outcomes;
    }

    @Override
    public CancelTaskEventOutcome cancelTask(TaskParams taskParams) {
        fillAndValidateAttributes(taskParams);

        Task task = taskParams.getTask();
        Case useCase = taskParams.getUseCase();
        AbstractUser user = taskParams.getUser();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        // TODO: impersonation
        log.info("[{}]: Canceling task [{}] to user [{}]", useCase.getStringId(), task.getTitle(), user.getEmail());

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreCancelActions(), useCase,
                task, transition, taskParams.getParams()));
        if (!outcomes.isEmpty()) {
            task = findOne(task.getStringId());
        }
        CancelTaskEventOutcome outcome = new CancelTaskEventOutcome(useCase, task, outcomes);
        useCase = evaluateRules(new CancelTaskEvent(outcome, EventPhase.PRE));

        useCase = returnTokens(task, useCase);
        ReloadTaskOutcome reloadTaskOutcome = reloadTasks(useCase, false);
        if (reloadTaskOutcome.isAnyTaskExecuted()) {
            useCase = workflowService.findOne(useCase.getStringId());
        }

        publisher.publishEvent(new CancelTaskEvent(outcome, EventPhase.POST, user));
        outcomes.addAll(eventService.runActions(transition.getPostCancelActions(), useCase, task, transition, taskParams.getParams()));
        useCase = evaluateRules(new CancelTaskEvent(outcome, EventPhase.POST));

        outcome = new CancelTaskEventOutcome(useCase, task);
        outcome.setOutcomes(outcomes);
        addMessageToOutcome(transition, EventType.CANCEL, outcome);
        publisher.publishEvent(new CancelTaskEvent(outcome, EventPhase.POST, user));
        // TODO: impersonation
        log.info("[{}]: Task [{}] in case [{}] assigned to [{}] was cancelled", useCase.getStringId(), task.getTitle(),
                useCase.getTitle(), user.getEmail());

        return outcome;
    }

    /**
     * Used in cancel task action
     */
    @Override
    public void cancelTasksWithoutReload(Set<String> transitions, String caseId) {
        cancelTasksWithoutReload(transitions, caseId, new HashMap<>());
    }

    /**
     * Used in cancel task action
     */
    @Override
    public void cancelTasksWithoutReload(Set<String> transitions, String caseId, Map<String, String> params) {
        List<Task> tasks = taskRepository.findAllByTransitionIdInAndCaseId(transitions, caseId);
        Case useCase = null;
        for (Task task : tasks) {
            if (task.getUserId() == null) {
                continue;
            }
            if (useCase == null) {
                useCase = workflowService.findOne(task.getCaseId());
            }

            Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
            boolean anyActionExecuted = !eventService.runActions(transition.getPreCancelActions(), useCase, task,
                    transition, params).isEmpty();
            if (anyActionExecuted) {
                useCase = workflowService.findOne(useCase.getStringId());
            }
            returnTokens(task, useCase);
            eventService.runActions(transition.getPostCancelActions(), useCase, task, transition, params);
        }
    }

    protected void fillAndValidateAttributes(TaskParams taskParams) {
        if (taskParams.getTask() == null) {
            Task task = findOne(taskParams.getTaskId());
            taskParams.setTask(task);
        }
        if (taskParams.getUseCase() == null) {
            Case useCase = workflowService.findOne(taskParams.getTask().getCaseId());
            taskParams.setUseCase(useCase);
        }
        if (taskParams.getUser() == null) {
            AbstractUser user = userService.getLoggedOrSystem();
            taskParams.setUser(user);
        }
    }

    protected void fillAndValidateAttributes(DelegateTaskParams taskParams) {
        if (taskParams.getTask() == null) {
            Task task = findOne(taskParams.getTaskId());
            taskParams.setTask(task);
        }
        if (taskParams.getUseCase() == null) {
            Case useCase = workflowService.findOne(taskParams.getTask().getCaseId());
            taskParams.setUseCase(useCase);
        }
        if (taskParams.getDelegator() == null) {
            AbstractUser delegator = null;
            if (taskParams.getDelegatorId() != null) {
                delegator = userService.findById(taskParams.getDelegatorId(), null);
            }
            if (delegator == null) {
                delegator = userService.getLoggedOrSystem();
            }
            taskParams.setDelegator(delegator);
        }
        if (taskParams.getNewAssignee() == null) {
            if (taskParams.getNewAssigneeId() == null) {
                throw new IllegalArgumentException("New assignee is not specified.");
            }
            AbstractUser newAssignee = userService.findById(taskParams.getNewAssigneeId(), null);
            if (newAssignee == null) {
                throw new IllegalArgumentException("Such user [%s] does not exist.".formatted(taskParams.getNewAssigneeId()));
            }
            taskParams.setNewAssignee(newAssignee);
        }
    }

    private Case returnTokens(Task task, Case useCase) {
        PetriNet net = useCase.getPetriNet();
        Case finalUseCase = useCase;
        net.getArcsOfTransition(task.getTransitionId()).stream()
                .filter(arc -> arc.getSource() instanceof Place)
                .forEach(arc -> {
                    arc.rollbackExecution(finalUseCase.getConsumedTokens().get(arc.getStringId()));
                    finalUseCase.getConsumedTokens().remove(arc.getStringId());
                });
        workflowService.updateMarking(useCase);

        task.setAssignee(null);
        task.setStartDate(null);

        useCase = workflowService.save(useCase);
        save(task);

        return useCase;
    }

    @Override
    public DelegateTaskEventOutcome delegateTask(DelegateTaskParams delegateTaskParams) throws TransitionNotExecutableException {
        fillAndValidateAttributes(delegateTaskParams);

        AbstractUser newAssignee = delegateTaskParams.getNewAssignee();
        AbstractUser delegator = delegateTaskParams.getDelegator();
        Task task = delegateTaskParams.getTask();
        Case useCase = delegateTaskParams.getUseCase();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[{}]: Delegating task [{}] to user [{}]", useCase.getStringId(), task.getTitle(), newAssignee.getEmail());

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreDelegateActions(), useCase,
                task, transition, delegateTaskParams.getParams()));
        if (!outcomes.isEmpty()) {
            task = findOne(task.getStringId());
        }

        DelegateTaskEventOutcome outcome = new DelegateTaskEventOutcome(useCase, task, outcomes);
        useCase = evaluateRules(new DelegateTaskEvent(outcome, EventPhase.PRE));
        publisher.publishEvent(new DelegateTaskEvent(outcome, EventPhase.PRE, delegator, newAssignee.getStringId()));

        useCase = delegate(newAssignee, task, useCase, transition);

        outcomes.addAll(eventService.runActions(transition.getPostDelegateActions(), useCase, task, transition,
                delegateTaskParams.getParams()));
        useCase = evaluateRules(new DelegateTaskEvent(new DelegateTaskEventOutcome(useCase, task, outcomes), EventPhase.POST));

        ReloadTaskOutcome reloadTaskOutcome = reloadTasks(useCase, false);
        if (reloadTaskOutcome.isAnyTaskExecuted()) {
            useCase = workflowService.findOne(useCase.getStringId());
        }

        addMessageToOutcome(transition, EventType.DELEGATE, outcome);
        publisher.publishEvent(new DelegateTaskEvent(outcome, EventPhase.POST, delegator, newAssignee.getStringId()));
        // TODO: impersonation
        log.info("Task [{}] in case [{}] assigned to [{}] was delegated to [{}]", task.getTitle(), useCase.getTitle(),
                newAssignee.getEmail(), newAssignee.getEmail());

        return outcome;
    }

    protected Case delegate(AbstractUser delegated, Task task, Case useCase, Transition transition) throws TransitionNotExecutableException {
        if (task.getUserId() != null) {
            task.setAssignee(ActorTransformer.toActorRef(delegated));
            task.setUser(delegated);
            save(task);
            return useCase;
        } else {
            return assignTaskToUser(delegated, task, useCase, transition);
        }
    }

    protected Case evaluateRules(TaskEvent taskEvent) {
        publisher.publishEvent(taskEvent);
        return workflowService.findOne(taskEvent.getTaskEventOutcome().getCase().getStringId());
    }

    /**
     * Reloads all unassigned tasks of given case:
     * <table border="1">
     * <tr>
     * <td></td><td>Task is present</td><td>Task is not present</td>
     * </tr>
     * <tr>
     * <td>Transition executable</td><td>no action</td><td>create task</td>
     * </tr>
     * <tr>
     * <td>Transition not executable</td><td>destroy task</td><td>no action</td>
     * </tr>
     * </table>
     *
     * @param useCase useCase for which to reload tasks
     * @param lazyCaseSave if set to true, the useCase is saved only if any task is about to be executed. If set to false
     *                     the useCase is saved every time this method is called.
     *
     * @return {@link ReloadTaskOutcome}, which holds the information if any task was executed and if the useCase was saved
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public ReloadTaskOutcome reloadTasks(Case useCase, boolean lazyCaseSave) {
        log.info("[{}]: Reloading tasks in [{}]", useCase.getStringId(), useCase.getTitle());
        PetriNet net = useCase.getPetriNet();
        ReloadTaskOutcome outcome = new ReloadTaskOutcome();

        List<Task> newTasks = new ArrayList<>();
        List<Task> tasksToBeExecuted = new ArrayList<>();
        List<Task> disabledTasks = new ArrayList<>();
        Map<String, String> tasks = useCase.getTasks().stream().collect(Collectors.toMap(TaskPair::getTransition, TaskPair::getTask));
        for (Transition transition : net.getTransitions().values()) {
            String taskId = tasks.get(transition.getImportId());
            if (isExecutable(transition, net)) {
                if (taskId != null) {
                    // task exists - do nothing
                } else {
                    // task does not exist - create a new task and check a trigger
                    Task newTask = createFromTransition(transition, useCase);
                    newTasks.add(newTask);
                    if (transition.hasAutoTrigger()) {
                        tasksToBeExecuted.add(newTask);
                    }
                }
            } else {
                if (taskId != null) {
                    // task exists - delete task if not assigned
                    Optional<Task> optionalTask = findOptionalById(taskId);
                    if (optionalTask.isEmpty()) {
                        continue;
                    }
                    Task task = optionalTask.get();
                    if (task.getUserId() != null && !task.getUserId().isBlank()) {
                        // task is assigned - do not delete
                    } else {
                        // task is not assigned - delete
                        disabledTasks.add(task);
                    }
                } else {
                    // task does not exist - do nothing
                }
            }
        }
        save(newTasks);
        delete(disabledTasks, useCase);
        useCase = workflowService.resolveActorRef(useCase, false);

        if (!lazyCaseSave && (!newTasks.isEmpty() || !disabledTasks.isEmpty())) {
            useCase = workflowService.save(useCase);
            outcome.setUseCaseSaved(true);
        }

        for (Task task : tasksToBeExecuted) {
            if (!outcome.isUseCaseSaved()) {
                useCase = workflowService.save(useCase);
                outcome.setUseCaseSaved(true);
            }
            executeTransition(task, useCase);
            outcome.setAnyTaskExecuted(true);
        }
        return outcome;
    }

    protected boolean isExecutable(Transition transition, PetriNet net) {
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);

        if (arcsOfTransition == null) {
            return true;
        }

        return arcsOfTransition.stream()
                .filter(arc -> arc.getDestination().equals(transition)) // todo: from same source error
                .allMatch(Arc::isExecutable);
    }

    protected Case finishExecution(Transition transition, Task task, Case useCase) throws TransitionNotExecutableException {
        log.info("[{}]: Finish execution of task [{}] in case [{}]", useCase.getStringId(), transition.getTitle(), useCase.getTitle());

        execute(transition, useCase, arc -> arc.getSource().equals(transition));
        useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream()
                .filter(arc -> useCase.getConsumedTokens().containsKey(arc.getStringId()))
                .forEach(arc -> useCase.getConsumedTokens().remove(arc.getStringId()));

        task.setFinishDate(LocalDateTime.now());
        task.setFinishedBy(task.getUserId());
        task.setAssignee(null);

        save(task);
        return workflowService.save(useCase);
    }

    public void startExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
        log.info("[{}]: Start execution of {} in case {}", useCase.getStringId(), transition.getTitle(), useCase.getTitle());
        execute(transition, useCase, arc -> arc.getDestination().equals(transition));
    }

    protected void execute(Transition transition, Case useCase, Predicate<Arc> predicate) throws TransitionNotExecutableException {
        Supplier<Stream<Arc>> filteredSupplier = () -> useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream().filter(predicate);

        if (!filteredSupplier.get().allMatch(Arc::isExecutable)) {
            throw new TransitionNotExecutableException("Not all arcs can be executed task [" + transition.getStringId() + "] in case [" + useCase.getTitle() + "]");
        }

        filteredSupplier.get().sorted((o1, o2) -> ArcOrderComparator.getInstance().compare(o1, o2)).forEach(arc -> {
            if (arc instanceof ResetArc) {
                useCase.getConsumedTokens().put(arc.getStringId(), ((Place) arc.getSource()).getTokens());
            }
            if (arc.getReference() != null && arc.getSource() instanceof Place) {
                useCase.getConsumedTokens().put(arc.getStringId(), arc.getReference().getMultiplicity());
            }
            arc.execute();
        });

        workflowService.updateMarking(useCase);
    }

    protected void executeTransition(Task task, Case useCase) {
        log.info("[{}]: executeTransition [{}] in case [{}]", useCase.getStringId(), task.getTransitionId(), useCase.getTitle());
        try {
            log.info("assignTask [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            assignTask(TaskParams.with()
                    .task(task)
                    .build());

            log.info("getData [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            dataService.getData(task.getStringId());

            log.info("finishTask [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            finishTask(TaskParams.with()
                    .task(task)
                    .build());
        } catch (Exception e) {
            log.error("Execution of task [{}] in case [{}] failed: ", task.getTitle(), useCase.getTitle(), e);
        }
    }

    void validateData(Transition transition, Case useCase) {
        for (Map.Entry<String, DataFieldLogic> entry : transition.getDataSet().entrySet()) {
            Field<?> field = useCase.getField(entry.getKey());
            DataField dataField = useCase.getDataField(entry.getKey());
            if (field != null && field.getValidations() != null) {
                validationService.valid(field, dataField);
            }
            if (!dataField.isRequired(transition.getImportId())
                    || dataField.isUndefined(transition.getImportId()) && !entry.getValue().isRequired()) {
                continue;
            }

            Object value = dataField.getValue();
            if (value == null) {
                if (field == null) {
                    throw new IllegalArgumentException("Some field has null value");
                } else {
                    throw new IllegalArgumentException("Field \"%s\" has null value".formatted(field.getName()));
                }
            }
            if (value instanceof String && ((String) value).isEmpty()) {
                if (field == null) {
                    throw new IllegalArgumentException("Some field has empty value");
                } else {
                    throw new IllegalArgumentException("Field \"%s\" has empty value".formatted(field.getName()));
                }
            }
        }
    }

    protected void scheduleTaskExecution(Task task, LocalDateTime time, Case useCase) {
        log.info("[{}]: Task {} scheduled to run at {}", useCase.getStringId(), task.getTitle(), time);
        scheduler.schedule(() -> executeTransition(task, useCase), DateUtils.localDateTimeToDate(time));
    }

    @Override
    public Task findOne(String taskId) {
        Optional<Task> taskOptional = findOptionalById(taskId);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        return taskOptional.get();
    }

    @Override
    public Page<Task> getAll(LoggedUser loggedUser, Pageable pageable, Locale locale) {
        List<Task> tasks;
        // TODO: impersonation
//        LoggedUser loggedOrImpersonated = loggedUser.getSelfOrImpersonated();
        LoggedUser loggedOrImpersonated = loggedUser;

        if (loggedOrImpersonated.getProcessRoles().isEmpty()) {
            tasks = new ArrayList<>();
            return new PageImpl<>(tasks, pageable, 0L);
        } else {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("{$or:[");
            loggedOrImpersonated.getProcessRoles().forEach(role -> {
                queryBuilder.append("{\"roles.");
                queryBuilder.append(role);
                queryBuilder.append("\":{$exists:true}},");
            });
            if (!loggedOrImpersonated.getProcessRoles().isEmpty())
                queryBuilder.deleteCharAt(queryBuilder.length() - 1);
            else
                queryBuilder.append("{}");
            queryBuilder.append("]}");
            BasicQuery query = new BasicQuery(queryBuilder.toString());
            query = (BasicQuery) query.with(pageable);
            tasks = mongoTemplate.find(query, Task.class);
            return loadUsers(new PageImpl<>(tasks, pageable,
                    mongoTemplate.count(new BasicQuery(queryBuilder.toString(), "{_id:1}"), Task.class)));
        }
    }

    @Override
    public Page<Task> search(List<TaskSearchRequest> requests, Pageable pageable, LoggedUser user, Locale locale, Boolean isIntersection) {
        com.querydsl.core.types.Predicate searchPredicate = searchService.buildQuery(requests, user, locale, isIntersection);
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
    public long count(List<TaskSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection) {
        com.querydsl.core.types.Predicate searchPredicate = searchService.buildQuery(requests, user, locale, isIntersection);
        if (searchPredicate != null) {
            return taskRepository.count(searchPredicate);
        } else {
            return 0;
        }
    }

    @Override
    public Page<Task> findByCases(Pageable pageable, List<String> cases) {
        return loadUsers(taskRepository.findByCaseIdIn(pageable, cases));
    }

    @Override
    public Task findById(String id) {
        Optional<Task> taskOptional = findOptionalById(id);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [%s]".formatted(id));
        }
        Task task = taskOptional.get();
        this.setUser(task);
        return task;
    }

    @Override
    public Optional<Task> findOptionalById(String id) {
        String[] parts = id.split(ProcessResourceId.ID_SEPARATOR);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid NetgrifId format: %s".formatted(id));
        }
        String objectIdPart = parts[1];
        ObjectId objectId = new ObjectId(objectIdPart);
        return taskRepository.findByIdObjectId(objectId);
    }

    @Override
    public List<Task> findAllById(List<String> ids) {
        return taskRepository.findAllBy_idIn(ids.stream().map(ProcessResourceId::new).toList()).stream()
                .filter(Objects::nonNull)
                .sorted(Ordering.explicit(ids).onResultOf(Task::getStringId))
                .peek(this::setUser)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Task> findByUser(Pageable pageable, AbstractUser user) {
        // TODO: impersonation
//        return loadUsers(taskRepository.findByUserId(pageable, user.getSelfOrImpersonated().getStringId()));
        return loadUsers(taskRepository.findByAssignee_Id(pageable, user.getStringId()));
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
        if (tasks.getTotalElements() > 0)
            return tasks.getContent().getFirst();
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
        elasticTaskService.index(this.taskMappingService.transform(task));
        return task;
    }

    @Override
    public List<Task> save(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return tasks;
        }
        tasks = taskRepository.saveAll(tasks);
        tasks.forEach(task -> elasticTaskService.index(this.taskMappingService.transform(task)));
        return tasks;
    }

    @Override
    public void resolveActorRef(Case useCase) {
        useCase.getTasks().forEach(taskPair -> {
            Optional<Task> taskOptional = findOptionalById(taskPair.getTask());
            taskOptional.ifPresent(task -> resolveActorRef(task, useCase));
        });

    }

    @Override
    public Task resolveActorRef(Task task, Case useCase) {
        AtomicBoolean isTaskModified = new AtomicBoolean(!task.getActors().isEmpty() || !task.getNegativeViewActors().isEmpty());
        task.getActors().clear();
        task.getNegativeViewActors().clear();
        task.getActorRefs().forEach((actorFieldId, permission) -> {
            List<String> actorIds = getExistingActors((ActorListFieldValue) useCase.getDataSet().get(actorFieldId).getValue());
            if (actorIds != null && !actorIds.isEmpty()) {
                task.addActors(new HashSet<>(actorIds), permission);
                isTaskModified.set(true);
                if (permission.containsKey(RolePermission.VIEW.getValue()) && !permission.get(RolePermission.VIEW.getValue())) {
                    task.getNegativeViewActors().addAll(actorIds);
                }
            }
        });
        if (task.resolveViewActors()) {
            isTaskModified.set(true);
        }
        if (isTaskModified.get()) {
            return taskRepository.save(task);
        }
        return task;
    }

    private List<String> getExistingActors(ActorListFieldValue actorListFieldValue) {
        if (actorListFieldValue == null) {
            return null;
        }
        return actorListFieldValue.getActorValues().stream()
                .map(ActorFieldValue::getId)
                .filter(actorId -> {
                    AbstractUser user = userService.findById(actorId, null);
                    if (user != null) {
                        return true;
                    }
                    try {
                        groupService.findById(actorId);
                        return true;
                    } catch (IllegalArgumentException ignored) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private Task createFromTransition(Transition transition, Case useCase) {
        final Task task = com.netgrif.application.engine.adapter.spring.workflow.domain.Task.with()
                .title(transition.getTitle())
                .processId(useCase.getPetriNetId())
                .caseId(useCase.get_id().toString())
                .transitionId(transition.getImportId())
                .layout(transition.getLayout())
                .tags(transition.getTags())
                .caseColor(useCase.getColor())
                .caseTitle(useCase.getTitle())
                .priority(transition.getPriority())
                .icon(transition.getIcon() == null ? useCase.getIcon() : transition.getIcon())
                .immediateDataFields(new LinkedHashSet<>(transition.getImmediateData()))
                .assignPolicy(transition.getAssignPolicy())
                .dataFocusPolicy(transition.getDataFocusPolicy())
                .finishPolicy(transition.getFinishPolicy())
                .assignedUserPolicy(new HashMap<>(transition.getAssignedUserPolicy()))
                .roles(new HashMap<>())
                .actorRefs(new HashMap<>())
                .actors(new HashMap<>())
                .viewRoles(new LinkedList<>())
                .viewActorRefs(new LinkedList<>())
                .viewActors(new LinkedList<>())
                .negativeViewRoles(new LinkedList<>())
                .negativeViewActors(new LinkedList<>())
                .triggers(new LinkedList<>())
                .eventTitles(new HashMap<>())
                .build();
        transition.getEvents().forEach((type, event) -> task.addEventTitle(type, event.getTitle()));
        for (Trigger trigger : transition.getTriggers()) {
            Trigger taskTrigger = trigger.clone();
            task.addTrigger(taskTrigger);

            if (taskTrigger instanceof TimeTrigger timeTrigger) {
                scheduleTaskExecution(task, timeTrigger.getStartDate(), useCase);
            }
        }
        ProcessRole defaultRole = processRoleService.getDefaultRole();
        ProcessRole anonymousRole = processRoleService.getAnonymousRole();
        for (Map.Entry<String, Map<String, Boolean>> entry : transition.getRoles().entrySet()) {
            if (useCase.getEnabledRoles().contains(entry.getKey())
                    || defaultRole.getStringId().equals(entry.getKey())
                    || anonymousRole.getStringId().equals(entry.getKey())) {
                task.addRole(entry.getKey(), entry.getValue());
            }
        }
        transition.getNegativeViewRoles().forEach(task::addNegativeViewRole);

        for (Map.Entry<String, Map<String, Boolean>> entry : transition.getActorRefs().entrySet()) {
            task.addActorRef(entry.getKey(), entry.getValue());
        }
        task.resolveViewRoles();
        task.resolveViewActorRefs();

        Transaction transaction = useCase.getPetriNet().getTransactionByTransition(transition);
        if (transaction != null) {
            task.setTransactionId(transaction.getStringId());
        }

        useCase.addTask(task);
        CreateTaskEventOutcome outcome = new CreateTaskEventOutcome(useCase, task);
        publisher.publishEvent(new CreateTaskEvent(outcome, EventPhase.POST, userService.getLoggedOrSystem()));

        return task;
    }

    private Page<Task> loadUsers(Page<Task> tasks) {
        Map<String, AbstractUser> users = new HashMap<>();
        tasks.forEach(task -> {
            if (task.getUserId() != null) {
                if (users.containsKey(task.getUserId()))
                    task.setUser(users.get(task.getUserId()));
                else {
                    task.setUser(userService.findById(task.getUserId(), task.getUserRealmId()));
                    users.put(task.getUserId(), task.getUser());
                }
            } else {
                task.setUser(null);
            }
        });

        return tasks;
    }

    @Override
    public void delete(List<Task> tasks, Case useCase) {
        if (tasks.isEmpty()) {
            return;
        }
        workflowService.removeTasksFromCase(tasks, useCase);
        log.info("[{}]: Tasks of case {} are being deleted", useCase.getStringId(), useCase.getTitle());
        taskRepository.deleteAll(tasks);
        tasks.forEach(t -> elasticTaskService.remove(t.getStringId()));
    }

    @Override
    public void delete(List<Task> tasks, String caseId) {
        delete(tasks, caseId, false);
    }

    @Override
    public void delete(List<Task> tasks, String caseId, boolean force) {
        if (!force) {
            workflowService.removeTasksFromCase(tasks, caseId);
        }
        log.info("[{}]: Tasks of case are being deleted", caseId);
        taskRepository.deleteAll(tasks);
        tasks.forEach(t -> elasticTaskService.remove(t.getStringId()));
    }

    @Override
    public void deleteTasksByCase(String caseId) {
        deleteTasksByCase(caseId, false);
    }

    @Override
    public void deleteTasksByCase(String caseId, boolean force) {
        delete(taskRepository.findAllByCaseId(caseId), caseId, force);
    }

    @Override
    public void deleteTasksByPetriNetId(String petriNetId) {
        taskRepository.deleteAllByProcessId(petriNetId);
    }

    private void setUser(Task task) {
        if (task.getUserId() != null) {
            task.setUser(userService.findById(task.getUserId(), task.getUserRealmId()));
        }
    }

    private void addMessageToOutcome(Transition transition, EventType type, TaskEventOutcome outcome) {
        if (transition.getEvents().containsKey(type)) {
            outcome.setMessage(transition.getEvents().get(type).getMessage());
        }
    }

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
        if (mainOutcome == null) {
            return null;
        }
        mainOutcome.addOutcomes(new ArrayList<>(outcomes.values()));
        return mainOutcome;
    }
}
