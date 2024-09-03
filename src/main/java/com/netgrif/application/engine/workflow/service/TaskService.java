package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.event.events.task.*;
import com.netgrif.application.engine.history.domain.taskevents.AssignTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.CancelTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.DelegateTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.FinishTaskEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.petrinet.domain.*;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.arcs.ArcOrderComparator;
import com.netgrif.application.engine.petrinet.domain.arcs.ResetArc;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;

import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.validation.service.interfaces.IValidationService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.TaskNotFoundException;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.*;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.domain.triggers.TimeTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
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
    protected IUserService userService;

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
    protected IProcessRoleService processRoleService;

    @Autowired
    protected IElasticTaskMappingService taskMappingService;

    @Autowired
    protected IEventService eventService;

    protected IElasticTaskService elasticTaskService;

    @Autowired
    protected IHistoryService historyService;

    @Autowired
    protected IValidationService validation;

    @Autowired
    public void setElasticTaskService(IElasticTaskService elasticTaskService) {
        this.elasticTaskService = elasticTaskService;
    }

//    @Autowired
//    private IRuleEngine ruleEngine;

    @Override
    public List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, IUser user) throws TransitionNotExecutableException {
        return assignTasks(tasks, user, new HashMap<>());
    }

    @Override
    public List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, IUser user, Map<String, String> params) throws TransitionNotExecutableException {
        List<AssignTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(assignTask(task, user, params));
        }
        return outcomes;
    }

    @Override
    public AssignTaskEventOutcome assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException {
        return assignTask(loggedUser, taskId, new HashMap<>());
    }

    @Override
    public AssignTaskEventOutcome assignTask(LoggedUser loggedUser, String taskId, Map<String, String> params) throws TransitionNotExecutableException {
        Task task = this.findById(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Could not find task with id [" + taskId + "]");
        }

        IUser user = getUserFromLoggedUser(loggedUser);
        return assignTask(task, user, params);
    }

    @Override
    public AssignTaskEventOutcome assignTask(String taskId) throws TransitionNotExecutableException {
        return assignTask(taskId, new HashMap<>());
    }

    @Override
    public AssignTaskEventOutcome assignTask(String taskId, Map<String, String> params) throws TransitionNotExecutableException {
        LoggedUser user = userService.getLoggedOrSystem().transformToLoggedUser();
        return assignTask(user, taskId, params);
    }

    @Override
    public AssignTaskEventOutcome assignTask(Task task, IUser user) throws TransitionNotExecutableException {
        return assignTask(task, user, new HashMap<>());
    }

    @Override
    public AssignTaskEventOutcome assignTask(Task task, IUser user, Map<String, String> params) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreAssignActions(), useCase, task, transition, params));
        task = findOne(task.getStringId());
        useCase = evaluateRules(new AssignTaskEvent(new AssignTaskEventOutcome(useCase, task, outcomes), EventPhase.PRE));
        useCase = assignTaskToUser(user, task, useCase.getStringId());
        historyService.save(new AssignTaskEventLog(task, useCase, EventPhase.PRE, user));
        outcomes.addAll((eventService.runActions(transition.getPostAssignActions(), useCase, task, transition, params)));
        useCase = evaluateRules(new AssignTaskEvent(new AssignTaskEventOutcome(useCase, task, outcomes), EventPhase.POST));
        historyService.save(new AssignTaskEventLog(task, useCase, EventPhase.POST, user));

        AssignTaskEventOutcome outcome = new AssignTaskEventOutcome(useCase, task, outcomes);
        addMessageToOutcome(transition, EventType.ASSIGN, outcome);
        //publisher.publishEvent(new AssignTaskEvent(outcome));
        log.info("[" + useCase.getStringId() + "]: Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] assigned to [" + user.getSelfOrImpersonated().getEmail() + "]");
        return outcome;
    }

    protected Case assignTaskToUser(IUser user, Task task, String useCaseId) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(useCaseId);
        useCase.getPetriNet().initializeArcs();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[" + useCaseId + "]: Assigning task [" + task.getTitle() + "] to user [" + user.getSelfOrImpersonated().getEmail() + "]");

        startExecution(transition, useCase);
        task.setUserId(user.getSelfOrImpersonated().getStringId());
        task.setStartDate(LocalDateTime.now());
        task.setUser(user.getSelfOrImpersonated());

        useCase = workflowService.save(useCase);
        save(task);
        reloadTasks(useCase);
        useCase = workflowService.findOne(useCase.getStringId());
        return useCase;
    }

    @Override
    public List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, IUser user) throws TransitionNotExecutableException {
        return finishTasks(tasks, user, new HashMap<>());
    }

    @Override
    public List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, IUser user, Map<String, String> params) throws TransitionNotExecutableException {
        List<FinishTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(finishTask(task, user, params));
        }
        return outcomes;
    }

    @Override
    public FinishTaskEventOutcome finishTask(String taskId) throws IllegalArgumentException, TransitionNotExecutableException {
        return finishTask(taskId, new HashMap<>());
    }

    @Override
    public FinishTaskEventOutcome finishTask(String taskId, Map<String, String> params) throws IllegalArgumentException, TransitionNotExecutableException {
        LoggedUser user = userService.getLoggedOrSystem().transformToLoggedUser();
        return finishTask(user, taskId, params);
    }

    @Override
    public FinishTaskEventOutcome finishTask(LoggedUser loggedUser, String taskId) throws IllegalArgumentException, TransitionNotExecutableException {
        return finishTask(loggedUser, taskId, new HashMap<>());
    }

    @Override
    public FinishTaskEventOutcome finishTask(LoggedUser loggedUser, String taskId, Map<String, String> params) throws IllegalArgumentException, TransitionNotExecutableException {
        Task task = this.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        IUser user = getUserFromLoggedUser(loggedUser);

        if (task.getUserId() == null) {
            throw new IllegalArgumentException("Task with id=" + taskId + " is not assigned to any user.");
        }
        // TODO: 14. 4. 2017 replace with @PreAuthorize
        if (!task.getUserId().equals(user.getSelfOrImpersonated().getStringId()) && !loggedUser.isAnonymous()) {
            throw new IllegalArgumentException("User that is not assigned tried to finish task");
        }

        return finishTask(task, user, params);
    }

    @Override
    public FinishTaskEventOutcome finishTask(Task task, IUser user) throws TransitionNotExecutableException {
        return finishTask(task, user, null);
    }

    @Override
    public FinishTaskEventOutcome finishTask(Task task, IUser user, Map<String, String> params) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[" + useCase.getStringId() + "]: Finishing task [" + task.getTitle() + "] to user [" + user.getSelfOrImpersonated().getEmail() + "]");

        validateData(transition, useCase);
        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreFinishActions(), useCase, task, transition, params));
        task = findOne(task.getStringId());
        useCase = evaluateRules(new FinishTaskEvent(new FinishTaskEventOutcome(useCase, task, outcomes), EventPhase.PRE));

        finishExecution(transition, useCase.getStringId());
        task.setFinishDate(LocalDateTime.now());
        task.setFinishedBy(task.getUserId());
        task.setUserId(null);

        useCase = workflowService.findOne(useCase.getStringId());
        save(task);
        reloadTasks(useCase);
        useCase = workflowService.findOne(useCase.getStringId());
        historyService.save(new FinishTaskEventLog(task, useCase, EventPhase.PRE, user));
        outcomes.addAll(eventService.runActions(transition.getPostFinishActions(), useCase, task, transition, params));
        useCase = evaluateRules(new FinishTaskEvent(new FinishTaskEventOutcome(useCase, task, outcomes), EventPhase.POST));

        FinishTaskEventOutcome outcome = new FinishTaskEventOutcome(useCase, task, outcomes);
        addMessageToOutcome(transition, EventType.FINISH, outcome);
        //publisher.publishEvent(new FinishTaskEvent(outcome));
        historyService.save(new FinishTaskEventLog(task, useCase, EventPhase.POST, user));
        log.info("[" + useCase.getStringId() + "]: Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] assigned to [" + user.getSelfOrImpersonated().getEmail() + "] was finished");

        return outcome;
    }

    @Override
    public List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, IUser user) {
        return cancelTasks(tasks, user, new HashMap<>());
    }

    @Override
    public List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, IUser user, Map<String, String> params) {
        List<CancelTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(cancelTask(task, user, params));
        }
        return outcomes;
    }

    @Override
    public CancelTaskEventOutcome cancelTask(LoggedUser loggedUser, String taskId) {
        return cancelTask(loggedUser, taskId, new HashMap<>());
    }

    @Override
    public CancelTaskEventOutcome cancelTask(LoggedUser loggedUser, String taskId, Map<String, String> params) {
        Task task = this.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        IUser user = getUserFromLoggedUser(loggedUser);
        return cancelTask(task, user, params);
    }

    @Override
    public CancelTaskEventOutcome cancelTask(Task task, IUser user) {
        return cancelTask(task, user, null);
    }

    @Override
    public CancelTaskEventOutcome cancelTask(Task task, IUser user, Map<String, String> params) {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[" + useCase.getStringId() + "]: Canceling task [" + task.getTitle() + "] to user [" + user.getSelfOrImpersonated().getEmail() + "]");

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreCancelActions(), useCase, task, transition, params));
        task = findOne(task.getStringId());
        useCase = evaluateRules(new CancelTaskEvent(new CancelTaskEventOutcome(useCase, task, outcomes), EventPhase.PRE));
        task = returnTokens(task, useCase.getStringId());
        useCase = workflowService.findOne(useCase.getStringId());
        reloadTasks(useCase);
        useCase = workflowService.findOne(useCase.getStringId());
        historyService.save(new CancelTaskEventLog(task, useCase, EventPhase.PRE, user));
        outcomes.addAll(eventService.runActions(transition.getPostCancelActions(), useCase, task, transition, params));
        useCase = evaluateRules(new CancelTaskEvent(new CancelTaskEventOutcome(useCase, task, outcomes), EventPhase.POST));

        CancelTaskEventOutcome outcome = new CancelTaskEventOutcome(useCase, task);
        outcome.setOutcomes(outcomes);
        addMessageToOutcome(transition, EventType.CANCEL, outcome);
        //publisher.publishEvent(new CancelTaskEvent(outcome));
        historyService.save(new CancelTaskEventLog(task, useCase, EventPhase.POST, user));
        log.info("[" + useCase.getStringId() + "]: Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] assigned to [" + user.getSelfOrImpersonated().getEmail() + "] was cancelled");
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
            if (task.getUserId() != null) {
                if (useCase == null)
                    useCase = workflowService.findOne(task.getCaseId());
                Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
                eventService.runActions(transition.getPreCancelActions(), useCase, task, transition, params);
                returnTokens(task, useCase.getStringId());
                eventService.runActions(transition.getPostCancelActions(), useCase, task, transition, params);
            }
        }
    }

    private Task returnTokens(Task task, String useCaseId) {
        Case useCase = workflowService.findOne(useCaseId);
        PetriNet net = useCase.getPetriNet();
        net.getArcsOfTransition(task.getTransitionId()).stream()
                .filter(arc -> arc.getSource() instanceof Place)
                .forEach(arc -> {
                    arc.rollbackExecution(useCase.getConsumedTokens().get(arc.getStringId()));
                    useCase.getConsumedTokens().remove(arc.getStringId());
                });
        workflowService.updateMarking(useCase);

        task.setUserId(null);
        task.setStartDate(null);
        task = save(task);
        workflowService.save(useCase);

        return task;
    }

    @Override
    public DelegateTaskEventOutcome delegateTask(LoggedUser loggedUser, String delegatedId, String taskId) throws TransitionNotExecutableException {
        return delegateTask(loggedUser, delegatedId, taskId, new HashMap<>());
    }

    @Override
    public DelegateTaskEventOutcome delegateTask(LoggedUser loggedUser, String delegatedId, String taskId, Map<String, String> params) throws TransitionNotExecutableException {
        IUser delegatedUser = userService.resolveById(delegatedId, true);
        IUser delegateUser = getUserFromLoggedUser(loggedUser);

        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        Task task = taskOptional.get();

        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[" + useCase.getStringId() + "]: Delegating task [" + task.getTitle() + "] to user [" + delegatedUser.getEmail() + "]");

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreDelegateActions(), useCase, task, transition, params));
        task = findOne(task.getStringId());
        useCase = evaluateRules(new DelegateTaskEvent(new DelegateTaskEventOutcome(useCase, task, outcomes), EventPhase.PRE));
        delegate(delegatedUser, task, useCase);
        historyService.save(new DelegateTaskEventLog(task, useCase, EventPhase.PRE, delegateUser, delegatedUser.getStringId()));
        outcomes.addAll(eventService.runActions(transition.getPostDelegateActions(), useCase, task, transition, params));
        useCase = evaluateRules(new DelegateTaskEvent(new DelegateTaskEventOutcome(useCase, task, outcomes), EventPhase.POST));

        useCase = workflowService.findOne(useCase.getStringId());
        reloadTasks(useCase);

        DelegateTaskEventOutcome outcome = new DelegateTaskEventOutcome(useCase, task, outcomes);
        addMessageToOutcome(transition, EventType.DELEGATE, outcome);
        //publisher.publishEvent(new DelegateTaskEvent(outcome));
        historyService.save(new DelegateTaskEventLog(task, useCase, EventPhase.POST, delegateUser, delegatedUser.getStringId()));
        log.info("Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] assigned to [" + delegateUser.getSelfOrImpersonated().getEmail() + "] was delegated to [" + delegatedUser.getEmail() + "]");

        return outcome;
    }

    protected void delegate(IUser delegated, Task task, Case useCase) throws TransitionNotExecutableException {
        if (task.getUserId() != null) {
            task.setUserId(delegated.getStringId());
            task.setUser(delegated);
            save(task);
        } else {
            assignTaskToUser(delegated, task, useCase.getStringId());
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
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void reloadTasks(Case useCase) {
        log.info("[" + useCase.getStringId() + "]: Reloading tasks in [" + useCase.getTitle() + "]");
        PetriNet net = useCase.getPetriNet();

        List<Task> newTasks = new ArrayList<>();
        List<Task> disabledTasks = new ArrayList<>();
        Map<String, String> tasks = useCase.getTasks().stream().collect(Collectors.toMap(TaskPair::getTransition, TaskPair::getTask));
        for (Transition transition : net.getTransitions().values()) {
            String taskId = tasks.get(transition.getImportId());
            if (isExecutable(transition, net)) {
                if (taskId != null) {
                    // task exists - do nothing
                } else {
                    // task does not exist - create a new task
                    newTasks.add(createFromTransition(transition, useCase));
                }
            } else {
                if (taskId != null) {
                    // task exists - delete task if not assigned
                    Optional<Task> optionalTask = taskRepository.findById(taskId);
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
        useCase = workflowService.resolveUserRef(useCase);

        for (Task task : newTasks) {
            executeIfAutoTrigger(useCase, net, task);
        }
    }

    private void executeIfAutoTrigger(Case useCase, PetriNet net, Task task) {
        try {
            Transition transition = net.getTransition(task.getTransitionId());
            if (transition.hasAutoTrigger()) {
                executeTransition(task, useCase);
            }
        } catch (TaskNotFoundException e) {
            log.info("Could not execute auto trigger on task [" + task.getStringId() + "],[" + task.getTransitionId() + "], reason: " + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    boolean isExecutable(Transition transition, PetriNet net) {
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);

        if (arcsOfTransition == null) {
            return true;
        }

        return arcsOfTransition.stream()
                .filter(arc -> arc.getDestination().equals(transition)) // todo: from same source error
                .allMatch(Arc::isExecutable);
    }

    void finishExecution(Transition transition, String useCaseId) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(useCaseId);
        log.info("[" + useCaseId + "]: Finish execution of task [" + transition.getTitle() + "] in case [" + useCase.getTitle() + "]");
        execute(transition, useCase, arc -> arc.getSource().equals(transition));
        Supplier<Stream<Arc>> arcStreamSupplier = () -> useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream();
        arcStreamSupplier.get().filter(arc -> useCase.getConsumedTokens().containsKey(arc.getStringId())).forEach(arc -> useCase.getConsumedTokens().remove(arc.getStringId()));
        workflowService.save(useCase);
    }

    public void startExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
        log.info("[" + useCase.getStringId() + "]: Start execution of " + transition.getTitle() + " in case " + useCase.getTitle());
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

    protected List<EventOutcome> executeTransition(Task task, Case useCase) {
        log.info("[" + useCase.getStringId() + "]: executeTransition [" + task.getTransitionId() + "] in case [" + useCase.getTitle() + "]");
        List<EventOutcome> outcomes = new ArrayList<>();
        try {
            log.info("assignTask [" + task.getTitle() + "] in case [" + useCase.getTitle() + "]");
            outcomes.add(assignTask(task.getStringId()));
            log.info("getData [" + task.getTitle() + "] in case [" + useCase.getTitle() + "]");
            outcomes.add(dataService.getData(task.getStringId()));
            log.info("finishTask [" + task.getTitle() + "] in case [" + useCase.getTitle() + "]");
            outcomes.add(finishTask(task.getStringId()));
        } catch (TransitionNotExecutableException e) {
            log.error("execution of task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] failed: ", e);
        }
        return outcomes;
    }

    void validateData(Transition transition, Case useCase) {
        for (Map.Entry<String, DataFieldLogic> entry : transition.getDataSet().entrySet()) {
            if (useCase.getPetriNet().getDataSet().get(entry.getKey()) != null
                    && useCase.getPetriNet().getDataSet().get(entry.getKey()).getValidations() != null) {
                validation.valid(useCase.getPetriNet().getDataSet().get(entry.getKey()), useCase.getDataField(entry.getKey()));
            }
            if (!useCase.getDataField(entry.getKey()).isRequired(transition.getImportId()))
                continue;
            if (useCase.getDataField(entry.getKey()).isUndefined(transition.getImportId()) && !entry.getValue().isRequired())
                continue;

            Object value = useCase.getDataSet().get(entry.getKey()).getValue();
            if (value == null) {
                Field field = useCase.getField(entry.getKey());
                throw new IllegalArgumentException("Field \"" + field.getName() + "\" has null value");
            }
            if (value instanceof String && ((String) value).isEmpty()) {
                Field field = useCase.getField(entry.getKey());
                throw new IllegalArgumentException("Field \"" + field.getName() + "\" has empty value");
            }
        }
    }

    protected void scheduleTaskExecution(Task task, LocalDateTime time, Case useCase) {
        log.info("[" + useCase.getStringId() + "]: Task " + task.getTitle() + " scheduled to run at " + time.toString());
        scheduler.schedule(() -> {
            try {
                executeTransition(task, useCase);
            } catch (Exception e) {
                log.info("[" + useCase.getStringId() + "]: Scheduled task [" + task.getTitle() + "] of case [" + useCase.getTitle() + "] could not be executed: " + e);
            }
        }, DateUtils.localDateTimeToDate(time));
    }

    @Override
    public Task findOne(String taskId) {
        String[] parts = taskId.split("-");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid NetgrifId format: " + taskId);
        }
        String objectIdPart = parts[1];
        ObjectId objectId = new ObjectId(objectIdPart);
        Optional<Task> taskOptional = taskRepository.findByIdObjectId(objectId);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        return taskOptional.get();
    }

    @Override
    public Page<Task> getAll(LoggedUser loggedUser, Pageable pageable, Locale locale) {
        List<Task> tasks;
        LoggedUser loggedOrImpersonated = loggedUser.getSelfOrImpersonated();

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
        String[] parts = id.split("-");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid NetgrifId format: " + id);
        }
        String objectIdPart = parts[1];
        ObjectId objectId = new ObjectId(objectIdPart);
        Optional<Task> taskOptional = taskRepository.findByIdObjectId(objectId);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + id + "]");
        }
        Task task = taskOptional.get();
        this.setUser(task);
        return task;
    }

    @Override
    public List<Task> findAllById(List<String> ids) {
        return taskRepository.findAllBy_idIn(ids).stream()
                .filter(Objects::nonNull)
                .sorted(Ordering.explicit(ids).onResultOf(Task::getStringId))
                .peek(this::setUser)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Task> findByUser(Pageable pageable, IUser user) {
        return loadUsers(taskRepository.findByUserId(pageable, user.getSelfOrImpersonated().getStringId()));
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
            return tasks.getContent().get(0);
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
        tasks = taskRepository.saveAll(tasks);
        tasks.forEach(task -> elasticTaskService.index(this.taskMappingService.transform(task)));
        return tasks;
    }

    @Override
    public void resolveUserRef(Case useCase) {
        useCase.getTasks().forEach(taskPair -> {
            Optional<Task> taskOptional = taskRepository.findById(taskPair.getTask());
            taskOptional.ifPresent(task -> resolveUserRef(task, useCase));
        });

    }

    @Override
    public Task resolveUserRef(Task task, Case useCase) {
        task.getUsers().clear();
        task.getNegativeViewUsers().clear();
        task.getUserRefs().forEach((id, permission) -> {
            List<String> userIds = getExistingUsers((UserListFieldValue) useCase.getDataSet().get(id).getValue());
            if (userIds != null && userIds.size() != 0 && permission.containsKey("view") && !permission.get("view")) {
                task.getNegativeViewUsers().addAll(userIds);
            } else if (userIds != null && userIds.size() != 0) {
                task.addUsers(new HashSet<>(userIds), permission);
            }
        });
        task.resolveViewUsers();
        return taskRepository.save(task);
    }

    private List<String> getExistingUsers(UserListFieldValue userListValue) {
        if (userListValue == null)
            return null;
        return userListValue.getUserValues().stream().map(UserFieldValue::getId)
                .filter(id -> userService.resolveById(id, false) != null)
                .collect(Collectors.toList());
    }

    private Task createFromTransition(Transition transition, Case useCase) {
        final Task task = Task.with()
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
                .create();
        transition.getEvents().forEach((type, event) -> task.addEventTitle(type, event.getTitle()));
        task.addAssignedUserPolicy(transition.getAssignedUserPolicy());
        for (Trigger trigger : transition.getTriggers()) {
            Trigger taskTrigger = trigger.clone();
            task.addTrigger(taskTrigger);

            if (taskTrigger instanceof TimeTrigger) {
                TimeTrigger timeTrigger = (TimeTrigger) taskTrigger;
                scheduleTaskExecution(task, timeTrigger.getStartDate(), useCase);
            }
        }
        ProcessRole defaultRole = processRoleService.defaultRole();
        ProcessRole anonymousRole = processRoleService.anonymousRole();
        for (Map.Entry<String, Map<String, Boolean>> entry : transition.getRoles().entrySet()) {
            if (useCase.getEnabledRoles().contains(entry.getKey())
                    || defaultRole.getStringId().equals(entry.getKey())
                    || anonymousRole.getStringId().equals(entry.getKey())) {
                task.addRole(entry.getKey(), entry.getValue());
            }
        }
        transition.getNegativeViewRoles().forEach(task::addNegativeViewRole);

        for (Map.Entry<String, Map<String, Boolean>> entry : transition.getUserRefs().entrySet()) {
            task.addUserRef(entry.getKey(), entry.getValue());
        }
        task.resolveViewRoles();
        task.resolveViewUserRefs();

        Transaction transaction = useCase.getPetriNet().getTransactionByTransition(transition);
        if (transaction != null) {
            task.setTransactionId(transaction.getStringId());
        }

        useCase.addTask(task);
        CreateTaskEventOutcome outcome = new CreateTaskEventOutcome(useCase, task);
        //publisher.publishEvent(new CreateTaskEvent(outcome));

        return task;
    }

    private Page<Task> loadUsers(Page<Task> tasks) {
        Map<String, IUser> users = new HashMap<>();
        tasks.forEach(task -> {
            if (task.getUserId() != null) {
                if (users.containsKey(task.getUserId()))
                    task.setUser(users.get(task.getUserId()));
                else {
                    task.setUser(userService.resolveById(task.getUserId(), true));
                    users.put(task.getUserId(), task.getUser());
                }
            }
        });

        return tasks;
    }

    @Override
    public void delete(List<Task> tasks, Case useCase) {
        workflowService.removeTasksFromCase(tasks, useCase);
        log.info("[" + useCase.getStringId() + "]: Tasks of case " + useCase.getTitle() + " are being deleted");
        taskRepository.deleteAll(tasks);
        tasks.forEach(t -> elasticTaskService.remove(t.getStringId()));
    }

    @Override
    public void delete(List<Task> tasks, String caseId) {
        workflowService.removeTasksFromCase(tasks, caseId);
        log.info("[" + caseId + "]: Tasks of case are being deleted");
        taskRepository.deleteAll(tasks);
        tasks.forEach(t -> elasticTaskService.remove(t.getStringId()));
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
        if (task.getUserId() != null) {
            task.setUser(userService.resolveById(task.getUserId(), true));
        }
    }

    private EventOutcome addMessageToOutcome(Transition transition, EventType type, TaskEventOutcome outcome) {
        if (transition.getEvents().containsKey(type)) {
            outcome.setMessage(transition.getEvents().get(type).getMessage());
        }
        return outcome;
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
        mainOutcome.addOutcomes(new ArrayList<>(outcomes.values()));
        return mainOutcome;
    }

    protected IUser getUserFromLoggedUser(LoggedUser loggedUser) {
        IUser user = userService.resolveById(loggedUser.getId(), true);
        IUser fromLogged = loggedUser.transformToUser();
        user.setImpersonated(fromLogged.getImpersonated());
        return user;
    }
}
