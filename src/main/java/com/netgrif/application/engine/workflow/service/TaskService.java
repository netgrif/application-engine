package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.history.domain.taskevents.AssignTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.CancelTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.DelegateTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.FinishTaskEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.Place;
import com.netgrif.application.engine.petrinet.domain.Transaction;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.arcs.ArcOrderComparator;
import com.netgrif.application.engine.petrinet.domain.arcs.ResetArc;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.rules.domain.facts.TransitionEventFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.CreateTasksOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.taskoutcomes.*;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private TaskRepository taskRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

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
    private IProcessRoleService processRoleService;

    @Autowired
    private IEventService eventService;

    @Autowired
    private IHistoryService historyService;

    @Autowired
    private IRuleEngine ruleEngine;

    /**
     * todo javadoc
     * */
    @Override
    public List<EventOutcome> executeTask(Task task, Case useCase) {
        log.info("[{}]: executeTask [{}] in case [{}]", useCase.getStringId(), task.getTransitionId(), useCase.getTitle());
        List<EventOutcome> outcomes = new ArrayList<>();
        try {
            log.info("assignTask [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            outcomes.add(assignTask(task.getStringId()));
            log.info("getData [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            outcomes.add(dataService.getData(task.getStringId(), userService.getSystem()));
            log.info("finishTask [{}] in case [{}]", task.getTitle(), useCase.getTitle());
            outcomes.add(finishTask(task.getStringId()));
        } catch (TransitionNotExecutableException e) {
            log.error("execution of task [{}] in case [{}] failed: ", task.getTitle(), useCase.getTitle(), e);
        }
        return outcomes;
    }

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
    public AssignTaskEventOutcome assignTask(String taskId) throws TransitionNotExecutableException {
        return assignTask(taskId, new HashMap<>());
    }

    @Override
    public AssignTaskEventOutcome assignTask(String taskId, Map<String, String> params) throws TransitionNotExecutableException {
        LoggedUser user = userService.getLoggedOrSystem().transformToLoggedUser();
        return assignTask(user, taskId, params);
    }

    @Override
    public AssignTaskEventOutcome assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException {
        return assignTask(loggedUser, taskId, new HashMap<>());
    }

    @Override
    public AssignTaskEventOutcome assignTask(LoggedUser loggedUser, String taskId, Map<String, String> params) throws TransitionNotExecutableException, TaskNotFoundException {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundException("Could not find task with id [" + taskId + "]");
        }
        IUser user = userService.getUserFromLoggedUser(loggedUser);
        return assignTask(taskOptional.get(), user, params);
    }

    @Override
    public AssignTaskEventOutcome assignTask(Task task, IUser user) throws TransitionNotExecutableException {
        return assignTask(task, user, new HashMap<>());
    }

    @Override
    public AssignTaskEventOutcome assignTask(Task task, IUser user, Map<String, String> params) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreAssignActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
        useCase = workflowService.findOne(task.getCaseId());
        task = findOne(task.getStringId());
        evaluateRules(useCase.getStringId(), task, EventType.ASSIGN, EventPhase.PRE);
        useCase = workflowService.findOne(task.getCaseId());
        assignTaskToUser(user, task, useCase.getStringId());
        useCase = workflowService.findOne(task.getCaseId());
        historyService.save(new AssignTaskEventLog(task, useCase, EventPhase.PRE, user));
        outcomes.addAll((eventService.runActions(transition.getPostAssignActions(), workflowService.findOne(task.getCaseId()), task, transition, params)));
        useCase = workflowService.findOne(task.getCaseId());
        evaluateRules(useCase.getStringId(), task, EventType.ASSIGN, EventPhase.POST);
        useCase = workflowService.findOne(task.getCaseId());
        historyService.save(new AssignTaskEventLog(task, useCase, EventPhase.POST, user));

        AssignTaskEventOutcome outcome = new AssignTaskEventOutcome(workflowService.findOne(task.getCaseId()), task, outcomes);
        addMessageToOutcome(transition, EventType.ASSIGN, outcome);

        log.info("[{}]: Task [{}] in case [{}] assigned to [{}]", useCase.getStringId(), task.getTitle(), useCase.getTitle(), user.getSelfOrImpersonated().getEmail());
        return outcome;
    }

    protected Case assignTaskToUser(IUser user, Task task, String useCaseId) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(useCaseId);
        useCase.getPetriNet().initializeArcs();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[{}]: Assigning task [{}] to user [{}]", useCaseId, task.getTitle(), user.getSelfOrImpersonated().getEmail());

        startExecution(transition, useCase);
        task.setUserId(user.getSelfOrImpersonated().getStringId());
        task.setLastAssigned(LocalDateTime.now());
        task.setUser(user.getSelfOrImpersonated());

        workflowService.save(useCase);
        save(task);
        reloadTasks(workflowService.findOne(useCase.getStringId()));
        return workflowService.findOne(useCase.getStringId());
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
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        Task task = taskOptional.get();
        IUser user = userService.getUserFromLoggedUser(loggedUser);

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
        return finishTask(task, user, new HashMap<>());
    }

    @Override
    public FinishTaskEventOutcome finishTask(Task task, IUser user, Map<String, String> params) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[{}]: Finishing task [{}] to user [{}]", useCase.getStringId(), task.getTitle(), user.getSelfOrImpersonated().getEmail());

        validateData(transition, useCase);
        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreFinishActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
        useCase = workflowService.findOne(task.getCaseId());
        task = findOne(task.getStringId());
        evaluateRules(useCase.getStringId(), task, EventType.FINISH, EventPhase.PRE);
        useCase = workflowService.findOne(task.getCaseId());

        finishExecution(transition, useCase.getStringId());
        task.setLastFinished(LocalDateTime.now());
        task.setFinishedBy(task.getUserId());
        task.setUserId(null);
        save(task);
        reloadTasks(workflowService.findOne(task.getCaseId()));
        useCase = workflowService.findOne(task.getCaseId());
        historyService.save(new FinishTaskEventLog(task, useCase, EventPhase.PRE, user));
        outcomes.addAll(eventService.runActions(transition.getPostFinishActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
        useCase = workflowService.findOne(task.getCaseId());
        evaluateRules(useCase.getStringId(), task, EventType.FINISH, EventPhase.POST);
        useCase = workflowService.findOne(task.getCaseId());
        FinishTaskEventOutcome outcome = new FinishTaskEventOutcome(workflowService.findOne(task.getCaseId()), task, outcomes);
        addMessageToOutcome(transition, EventType.FINISH, outcome);
        historyService.save(new FinishTaskEventLog(task, useCase, EventPhase.POST, user));
        log.info("[{}]: Task [{}] in case [{}] assigned to [{}] was finished", useCase.getStringId(), task.getTitle(), useCase.getTitle(), user.getSelfOrImpersonated().getEmail());

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
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        IUser user = userService.getUserFromLoggedUser(loggedUser);
        return cancelTask(taskOptional.get(), user, params);
    }

    @Override
    public CancelTaskEventOutcome cancelTask(Task task, IUser user) {
        return cancelTask(task, user, new HashMap<>());
    }

    @Override
    public CancelTaskEventOutcome cancelTask(Task task, IUser user, Map<String, String> params) {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[{}]: Canceling task [{}] to user [{}]", useCase.getStringId(), task.getTitle(), user.getSelfOrImpersonated().getEmail());

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreCancelActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
        useCase = workflowService.findOne(task.getCaseId());
        task = findOne(task.getStringId());
        evaluateRules(useCase.getStringId(), task, EventType.CANCEL, EventPhase.PRE);
        useCase = workflowService.findOne(task.getCaseId());
        task = returnTokens(task, useCase.getStringId());
        reloadTasks(workflowService.findOne(task.getCaseId()));
        useCase = workflowService.findOne(task.getCaseId());
        historyService.save(new CancelTaskEventLog(task, useCase, EventPhase.PRE, user));
        outcomes.addAll(eventService.runActions(transition.getPostCancelActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
        useCase = workflowService.findOne(task.getCaseId());
        evaluateRules(useCase.getStringId(), task, EventType.CANCEL, EventPhase.POST);
        useCase = workflowService.findOne(task.getCaseId());
        CancelTaskEventOutcome outcome = new CancelTaskEventOutcome(workflowService.findOne(task.getCaseId()), task);
        outcome.setOutcomes(outcomes);
        addMessageToOutcome(transition, EventType.CANCEL, outcome);

        historyService.save(new CancelTaskEventLog(task, useCase, EventPhase.POST, user));
        log.info("[{}]: Task [{}] in case [{}] assigned to [{}] was cancelled", useCase.getStringId(), task.getTitle(), useCase.getTitle(), user.getSelfOrImpersonated().getEmail());
        return outcome;
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
        // TODO: NAE-1848 should this be null?
        task.setLastAssigned(null);
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
        IUser delegateUser = userService.getUserFromLoggedUser(loggedUser);

        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        }
        Task task = taskOptional.get();

        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[{}]: Delegating task [{}] to user [{}]", useCase.getStringId(), task.getTitle(), delegatedUser.getEmail());

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreDelegateActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
        task = findOne(task.getStringId());
        evaluateRules(useCase.getStringId(), task, EventType.DELEGATE, EventPhase.PRE);
        delegate(delegatedUser, task, useCase);
        historyService.save(new DelegateTaskEventLog(task, useCase, EventPhase.PRE, delegateUser, delegatedUser.getStringId()));
        outcomes.addAll(eventService.runActions(transition.getPostDelegateActions(), workflowService.findOne(task.getCaseId()), task, transition, params));
        evaluateRules(useCase.getStringId(), task, EventType.DELEGATE, EventPhase.POST);

        reloadTasks(workflowService.findOne(task.getCaseId()));

        DelegateTaskEventOutcome outcome = new DelegateTaskEventOutcome(workflowService.findOne(task.getCaseId()), task, outcomes);
        addMessageToOutcome(transition, EventType.DELEGATE, outcome);
        historyService.save(new DelegateTaskEventLog(task, useCase, EventPhase.POST, delegateUser, delegatedUser.getStringId()));
        log.info("Task [{}] in case [{}] assigned to [{}] was delegated to [{}]", task.getTitle(), useCase.getTitle(), delegateUser.getSelfOrImpersonated().getEmail(), delegatedUser.getEmail());

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

    protected Case evaluateRules(String caseId, Task task, EventType eventType, EventPhase eventPhase) {
        Case useCase = workflowService.findOne(caseId);
        log.info("[{}]: Task [{}] in case [{}] evaluating rules of event {} of phase {}", useCase.getStringId(), task.getTitle(), useCase.getTitle(), eventType.name(), eventPhase.name());
        int rulesExecuted = ruleEngine.evaluateRules(useCase, task, TransitionEventFact.of(task, eventType, eventPhase));
        if (rulesExecuted == 0) {
            return useCase;
        }
        return workflowService.save(useCase);
    }

    /**
     * todo javadoc
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
    @Override
    public void reloadTasks(Case useCase) {
        log.info("[{}]: Reloading tasks in [{}]", useCase.getStringId(), useCase.getTitle());
        List<String> taskIds = useCase.getTasks().values().stream()
                .map(taskPair -> taskPair.getTaskId().toString())
                .collect(Collectors.toList());

        Optional<Task> autoTriggerTaskOpt = reloadAndSaveTasks((List<Task>) taskRepository.findAllById(taskIds), useCase);

        autoTriggerTaskOpt.ifPresent(task -> executeTask(task, workflowService.findOne(useCase.getStringId())));
    }

    /**
     * todo javadoc
     * */
    private Optional<Task> reloadAndSaveTasks(List<Task> tasks, Case useCase) {
        Task autoTriggered = null;
        PetriNet net = useCase.getPetriNet();
        for (Task task : tasks) {
            Transition transition = net.getTransition(task.getTransitionId());
            if (updateStateOfTask(task, transition, net)) {
                autoTriggered = task;
            }
        }
        save(tasks);

        return autoTriggered == null ? Optional.empty() : Optional.of(autoTriggered);
    }

    /**
     * todo javadoc
     * */
    @Override
    public CreateTasksOutcome createAndSetTasksInCase(Case useCase) {
        List<Task> tasks = useCase.getPetriNet().getTransitions().values().stream()
                .map(transition -> createTaskFromTransition(transition, useCase))
                .collect(Collectors.toList());

        useCase.addTasks(tasks);

        Optional<Task> autoTriggerTaskOpt = reloadAndSaveTasks(tasks, useCase);

        return new CreateTasksOutcome(tasks, autoTriggerTaskOpt.orElse(null));
    }

    /**
     * todo javadoc
     * */
    private Task createTaskFromTransition(Transition transition, Case useCase) {
        final Task task = Task.with()
                .title(transition.getTitle())
                .processId(useCase.getPetriNetId())
                .caseId(useCase.getId().toString())
                .transitionId(transition.getImportId())
                .layout(transition.getLayout())
                .tags(transition.getTags())
                .caseColor(useCase.getColor())
                .caseTitle(useCase.getTitle())
                .priority(transition.getPriority())
                .icon(transition.getIcon() == null ? useCase.getIcon() : transition.getIcon())
                .immediateDataFields(transition.getImmediateData())
                .assignPolicy(transition.getAssignPolicy())
                .dataFocusPolicy(transition.getDataFocusPolicy())
                .finishPolicy(transition.getFinishPolicy())
                .build();
        transition.getEvents().forEach((type, event) -> task.addEventTitle(type, event.getTitle()));
        task.addAssignedUserPolicy(transition.getAssignedUserPolicy());
        for (Trigger trigger : transition.getTriggers()) {
            Trigger taskTrigger = trigger.clone();
            task.addTrigger(taskTrigger);

            if (taskTrigger instanceof TimeTrigger) {
                TimeTrigger timeTrigger = (TimeTrigger) taskTrigger;
                scheduleTaskExecution(task, timeTrigger.getStartDate(), useCase);
            } else if (taskTrigger instanceof AutoTrigger) {
                task.setUserId(userService.getSystem().getStringId());
            }
        }
        ProcessRole defaultRole = processRoleService.defaultRole();
        ProcessRole anonymousRole = processRoleService.anonymousRole();
        for (Map.Entry<String, Map<RolePermission, Boolean>> entry : transition.getRoles().entrySet()) {
            if (useCase.getEnabledRoles().contains(entry.getKey())
                    || defaultRole.getStringId().equals(entry.getKey())
                    || anonymousRole.getStringId().equals(entry.getKey())) {
                task.addRole(entry.getKey(), entry.getValue());
            }
        }
        transition.getNegativeViewRoles().forEach(task::addNegativeViewRole);

        for (Map.Entry<String, Map<RolePermission, Boolean>> entry : transition.getUserRefs().entrySet()) {
            task.addUserRef(entry.getKey(), entry.getValue());
        }
        task.resolveViewRoles();
        task.resolveViewUserRefs();

        Transaction transaction = useCase.getPetriNet().getTransactionByTransition(transition);
        if (transaction != null) {
            task.setTransactionId(transaction.getStringId());
        }

        return task;
    }


    /**
     * todo javadoc
     * returns true if it is autotrigger
     * */
    private boolean updateStateOfTask(Task task, Transition transition, PetriNet net) {
        if (isExecutable(transition, net)) {
            task.setState(State.ENABLED);
            return task.isAutoTriggered();
        } else {
            task.setState(State.DISABLED);
            return false;
        }
    }

    private boolean isExecutable(Transition transition, PetriNet net) {
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);

        if (arcsOfTransition == null) {
            return true;
        }
        // TODO: NAE-1858 is this valid check? what about multiple input arcs from same place?
        return arcsOfTransition.stream()
                .filter(arc -> arc.getDestination().equals(transition)) // todo: from same source error
                .allMatch(Arc::isExecutable);
    }

    private void finishExecution(Transition transition, String useCaseId) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(useCaseId);
        log.info("[{}]: Finish execution of task [{}] in case [{}]", useCaseId, transition.getTitle(), useCase.getTitle());
        execute(transition, useCase, arc -> arc.getSource().equals(transition));
        Supplier<Stream<Arc>> arcStreamSupplier = () -> useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream();
        arcStreamSupplier.get().filter(arc -> useCase.getConsumedTokens().containsKey(arc.getStringId())).forEach(arc -> useCase.getConsumedTokens().remove(arc.getStringId()));
        workflowService.save(useCase);
    }

    private void startExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
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

    private void validateData(Transition transition, Case useCase) {
//        TODO: release/8.0.0 fix validation
//        for (Map.Entry<String, DataFieldLogic> entry : transition.getDataSet().entrySet()) {
//            if (useCase.getPetriNet().getDataSet().get(entry.getKey()) != null
//                    && useCase.getPetriNet().getDataSet().get(entry.getKey()).getValidations() != null) {
//                validation.valid(useCase.getPetriNet().getDataSet().get(entry.getKey()), useCase.getDataField(entry.getKey()));
//            }
//            if (!useCase.getDataField(entry.getKey()).isRequired(transition.getImportId()))
//                continue;
//            if (useCase.getDataField(entry.getKey()).isUndefined(transition.getImportId()) && !entry.getValue().isRequired())
//                continue;
//
//            Object value = useCase.getDataSet().get(entry.getKey()).getValue();
//            if (value == null) {
//                Field field = useCase.getField(entry.getKey());
//                throw new IllegalArgumentException("Field \"" + field.getName() + "\" has null value");
//            }
//            if (value instanceof String && ((String) value).isEmpty()) {
//                Field field = useCase.getField(entry.getKey());
//                throw new IllegalArgumentException("Field \"" + field.getName() + "\" has empty value");
//            }
//        }
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
                    mongoTemplate.count(new BasicQuery(queryBuilder.toString(), "{id:1}"), Task.class)));
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

    @Override
    public void resolveUserRef(Case useCase) {
        useCase.getTasks().values().forEach(taskPair -> {
            Optional<Task> taskOptional = taskRepository.findById(taskPair.getTaskStringId());
            taskOptional.ifPresent(task -> resolveUserRef(task, useCase));
        });
    }

    @Override
    public Task resolveUserRef(Task task, Case useCase) {
        task.getUsers().clear();
        task.getNegativeViewUsers().clear();
        task.getUserRefs().forEach((id, permission) -> {
            UserListField userListField = (UserListField) useCase.getDataSet().get(id);
            if (userListField.getValue() == null) {
                return;
            }
            List<String> userIds = getExistingUsers(userListField.getValue().getValue());
            if (userIds != null && userIds.size() != 0 && permission.containsKey(RolePermission.VIEW) && !permission.get(RolePermission.VIEW)) {
                task.getNegativeViewUsers().addAll(userIds);
            } else if (userIds != null && userIds.size() != 0) {
                task.addUsers(new HashSet<>(userIds), permission);
            }
        });
        task.resolveViewUsers();
        return taskRepository.save(task);
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
        log.info("[{}]: Tasks of case {} are being deleted", useCase.getStringId(), useCase.getTitle());
        taskRepository.deleteAll(tasks);
    }

    @Override
    public void delete(List<Task> tasks, String caseId) {
        workflowService.removeTasksFromCase(tasks, caseId);
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
