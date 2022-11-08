package com.netgrif.application.engine.workflow.service;

import com.google.common.collect.Ordering;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
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
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.rules.domain.facts.TransitionEventFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskService implements ITaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

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
    public void setElasticTaskService(IElasticTaskService elasticTaskService) {
        this.elasticTaskService = elasticTaskService;
    }

    @Autowired
    private IRuleEngine ruleEngine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, IUser user) throws TransitionNotExecutableException {
        List<AssignTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(assignTask(task, user));
        }
        return outcomes;
    }

    @Override
    @Transactional
    public AssignTaskEventOutcome assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (!taskOptional.isPresent())
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");

        IUser user = userService.resolveById(loggedUser.getId(), true);
        return assignTask(taskOptional.get(), user);
    }

    @Override
    public AssignTaskEventOutcome assignTask(String taskId) throws TransitionNotExecutableException {
        LoggedUser user = userService.getLoggedOrSystem().transformToLoggedUser();
        return assignTask(user, taskId);
    }

    @Override
    @Transactional
    public AssignTaskEventOutcome assignTask(Task task, IUser user) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreAssignActions(), workflowService.findOne(task.getCaseId()), task, transition));
        task = findOne(task.getStringId());
        evaluateRules(useCase.getStringId(), task, EventType.ASSIGN, EventPhase.PRE);
        assignTaskToUser(user, task, useCase.getStringId());
        historyService.save(new AssignTaskEventLog(task, useCase, EventPhase.PRE, user.getStringId()));
        outcomes.addAll((eventService.runActions(transition.getPostAssignActions(), workflowService.findOne(task.getCaseId()), task, transition)));
        evaluateRules(useCase.getStringId(), task, EventType.ASSIGN, EventPhase.POST);
        historyService.save(new AssignTaskEventLog(task, useCase, EventPhase.POST, user.getStringId()));

        AssignTaskEventOutcome outcome = new AssignTaskEventOutcome(workflowService.findOne(task.getCaseId()), task, outcomes);
        addMessageToOutcome(transition, EventType.ASSIGN, outcome);

        log.info("[" + useCase.getStringId() + "]: Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] assigned to [" + user.getEmail() + "]");
        return outcome;
    }

    @Transactional
    protected Case assignTaskToUser(IUser user, Task task, String useCaseId) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(useCaseId);
        useCase.getPetriNet().initializeArcs();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[" + useCaseId + "]: Assigning task [" + task.getTitle() + "] to user [" + user.getEmail() + "]");

        startExecution(transition, useCase);
        task.setUserId(user.getStringId());
        task.setStartDate(LocalDateTime.now());
        task.setUser(user);

        workflowService.save(useCase);
        save(task);
        reloadTasks(workflowService.findOne(useCase.getStringId()));
        return workflowService.findOne(useCase.getStringId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, IUser user) throws TransitionNotExecutableException {
        List<FinishTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(finishTask(task, user));
        }
        return outcomes;
    }

    @Override
    public FinishTaskEventOutcome finishTask(String taskId) throws IllegalArgumentException, TransitionNotExecutableException {
        LoggedUser user = userService.getLoggedOrSystem().transformToLoggedUser();
        return finishTask(user, taskId);
    }

    @Override
    @Transactional
    public FinishTaskEventOutcome finishTask(LoggedUser loggedUser, String taskId) throws IllegalArgumentException, TransitionNotExecutableException {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (!taskOptional.isPresent())
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        Task task = taskOptional.get();
        IUser user = userService.resolveById(loggedUser.getId(), true);

        if (task.getUserId() == null) {
            throw new IllegalArgumentException("Task with id=" + taskId + " is not assigned to any user.");
        }
        // TODO: 14. 4. 2017 replace with @PreAuthorize
        if (!task.getUserId().equals(loggedUser.getId()) && !loggedUser.isAnonymous()) {
            throw new IllegalArgumentException("User that is not assigned tried to finish task");
        }

        return finishTask(task, user);
    }

    @Override
    @Transactional
    public FinishTaskEventOutcome finishTask(Task task, IUser user) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[" + useCase.getStringId() + "]: Finishing task [" + task.getTitle() + "] to user [" + user.getEmail() + "]");

        validateData(transition, useCase);
        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreFinishActions(), workflowService.findOne(task.getCaseId()), task, transition));
        task = findOne(task.getStringId());
        evaluateRules(useCase.getStringId(), task, EventType.FINISH, EventPhase.PRE);

        finishExecution(transition, useCase.getStringId());
        task.setFinishDate(LocalDateTime.now());
        task.setFinishedBy(task.getUserId());
        task.setUserId(null);

        save(task);
        reloadTasks(workflowService.findOne(task.getCaseId()));
        historyService.save(new FinishTaskEventLog(task, useCase, EventPhase.PRE, user.getStringId()));
        outcomes.addAll(eventService.runActions(transition.getPostFinishActions(), workflowService.findOne(task.getCaseId()), task, transition));
        evaluateRules(useCase.getStringId(), task, EventType.FINISH, EventPhase.POST);

        FinishTaskEventOutcome outcome = new FinishTaskEventOutcome(workflowService.findOne(task.getCaseId()), task, outcomes);
        addMessageToOutcome(transition, EventType.FINISH, outcome);
        historyService.save(new FinishTaskEventLog(task, useCase, EventPhase.POST, user.getStringId()));
        log.info("[" + useCase.getStringId() + "]: Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] assigned to [" + user.getEmail() + "] was finished");

        return outcome;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, IUser user) {
        List<CancelTaskEventOutcome> outcomes = new ArrayList<>();
        for (Task task : tasks) {
            outcomes.add(cancelTask(task, user));
        }
        return outcomes;
    }

    @Override
    @Transactional
    public CancelTaskEventOutcome cancelTask(LoggedUser loggedUser, String taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (!taskOptional.isPresent())
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");

        IUser user = userService.resolveById(loggedUser.getId(), true);
        return cancelTask(taskOptional.get(), user);
    }

    @Override
    @Transactional
    public CancelTaskEventOutcome cancelTask(Task task, IUser user) {
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        log.info("[" + useCase.getStringId() + "]: Canceling task [" + task.getTitle() + "] to user [" + user.getEmail() + "]");

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreCancelActions(), workflowService.findOne(task.getCaseId()), task, transition));
        task = findOne(task.getStringId());
        evaluateRules(useCase.getStringId(), task, EventType.CANCEL, EventPhase.PRE);
        task = returnTokens(task, useCase.getStringId());
        workflowService.findOne(useCase.getStringId());
        reloadTasks(workflowService.findOne(task.getCaseId()));
        workflowService.findOne(useCase.getStringId());
        historyService.save(new CancelTaskEventLog(task, useCase, EventPhase.PRE, user.getStringId()));
        outcomes.addAll(eventService.runActions(transition.getPostCancelActions(), workflowService.findOne(task.getCaseId()), task, transition));
        evaluateRules(useCase.getStringId(), task, EventType.CANCEL, EventPhase.POST);

        CancelTaskEventOutcome outcome = new CancelTaskEventOutcome(workflowService.findOne(task.getCaseId()), task);
        outcome.setOutcomes(outcomes);
        addMessageToOutcome(transition, EventType.CANCEL, outcome);

        historyService.save(new CancelTaskEventLog(task, useCase, EventPhase.POST, user.getStringId()));
        log.info("[" + useCase.getStringId() + "]: Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] assigned to [" + user.getEmail() + "] was cancelled");
        return outcome;
    }

    /**
     * Used in cancel task action
     */
    @Override
    public void cancelTasksWithoutReload(Set<String> transitions, String caseId) {
        List<Task> tasks = taskRepository.findAllByTransitionIdInAndCaseId(transitions, caseId);
        Case useCase = null;
        for (Task task : tasks) {
            if (task.getUserId() != null) {
                if (useCase == null)
                    useCase = workflowService.findOne(task.getCaseId());
                Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
                eventService.runActions(transition.getPreCancelActions(), useCase, task, transition);
                returnTokens(task, useCase.getStringId());
                eventService.runActions(transition.getPostCancelActions(), useCase, task, transition);
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
    @Transactional
    public DelegateTaskEventOutcome delegateTask(LoggedUser loggedUser, String delegatedId, String taskId) throws TransitionNotExecutableException {
        IUser delegatedUser = userService.resolveById(delegatedId, true);
        IUser delegateUser = userService.resolveById(loggedUser.getId(), true);
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (!taskOptional.isPresent())
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        Task task = taskOptional.get();

        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());


        log.info("[" + useCase.getStringId() + "]: Delegating task [" + task.getTitle() + "] to user [" + delegatedUser.getEmail() + "]");

        List<EventOutcome> outcomes = new ArrayList<>(eventService.runActions(transition.getPreDelegateActions(), workflowService.findOne(task.getCaseId()), task, transition));
        task = findOne(task.getStringId());
        evaluateRules(useCase.getStringId(), task, EventType.DELEGATE, EventPhase.PRE);
        delegate(delegatedUser, task, useCase);
        historyService.save(new DelegateTaskEventLog(task, useCase, EventPhase.PRE, delegateUser.getStringId(), delegatedUser.getStringId()));
        outcomes.addAll(eventService.runActions(transition.getPostDelegateActions(), workflowService.findOne(task.getCaseId()), task, transition));
        evaluateRules(useCase.getStringId(), task, EventType.DELEGATE, EventPhase.POST);

        reloadTasks(workflowService.findOne(task.getCaseId()));

        DelegateTaskEventOutcome outcome = new DelegateTaskEventOutcome(workflowService.findOne(task.getCaseId()), task, outcomes);
        addMessageToOutcome(transition, EventType.DELEGATE, outcome);
        historyService.save(new DelegateTaskEventLog(task, useCase, EventPhase.POST, delegateUser.getStringId(), delegatedUser.getStringId()));
        log.info("Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] assigned to [" + delegateUser.getEmail() + "] was delegated to [" + delegatedUser.getEmail() + "]");

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
        log.info("[" + useCase.getStringId() + "]: Task [" + task.getTitle() + "] in case [" + useCase.getTitle() + "] evaluating rules of event " + eventType.name() + " of phase " + eventPhase.name());
        ruleEngine.evaluateRules(useCase, task, TransitionEventFact.of(task, eventType, eventPhase));
        return workflowService.save(useCase);
    }

    /**
     * Reloads all unassigned tasks of given case:
     * <table border="1">
     * <tr>
     * <td></td><td>LocalisedTask is present</td><td>LocalisedTask is not present</td>
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
    @Transactional
    public void reloadTasks(Case useCase) {
        log.info("[" + useCase.getStringId() + "]: Reloading tasks in [" + useCase.getTitle() + "]");
        PetriNet net = useCase.getPetriNet();

        net.getTransitions().values().forEach(transition -> {
            List<Task> tasks = taskRepository.findAllByCaseId(useCase.getStringId());
            if (isExecutable(transition, net)) {
                if (taskIsNotPresent(tasks, transition)) {
                    createFromTransition(transition, useCase);
                }
            } else {
                deleteUnassignedNotExecutableTasks(tasks, transition, useCase);
            }
        });
        String sysemId = userService.getSystem().getStringId();
        List<Task> tasks = taskRepository.findAllByCaseId(useCase.getStringId());
        if (tasks.stream().anyMatch(task -> Objects.equals(task.getUserId(), sysemId) && task.getStartDate() != null)) {
            return;
        }
        for (Task task : tasks) {
            if (Objects.equals(task.getUserId(), sysemId) && task.getStartDate() == null) {
                executeTransition(task, workflowService.findOne(useCase.getStringId()));
                return;
            }
            resolveUserRef(task, useCase);
        }
    }

    @Transactional
    void deleteUnassignedNotExecutableTasks(List<Task> tasks, Transition transition, Case useCase) {
        delete(tasks.stream()
                .filter(task -> task.getTransitionId().equals(transition.getStringId()) && task.getUserId() == null)
                .collect(Collectors.toList()), useCase);
    }

    @Transactional
    boolean taskIsNotPresent(List<Task> tasks, Transition transition) {
        return tasks.stream().noneMatch(task -> task.getTransitionId().equals(transition.getStringId()));
    }

    @Transactional
    boolean isExecutable(Transition transition, PetriNet net) {
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);

        if (arcsOfTransition == null)
            return true;

        return arcsOfTransition.stream()
                .filter(arc -> arc.getDestination().equals(transition)) // todo: from same source error
                .allMatch(Arc::isExecutable);
    }

    @Transactional
    void finishExecution(Transition transition, String useCaseId) throws TransitionNotExecutableException {
        Case useCase = workflowService.findOne(useCaseId);
        log.info("[" + useCaseId + "]: Finish execution of task [" + transition.getTitle() + "] in case [" + useCase.getTitle() + "]");
        execute(transition, useCase, arc -> arc.getSource().equals(transition));
        Supplier<Stream<Arc>> arcStreamSupplier = () -> useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream();
        arcStreamSupplier.get().filter(arc -> useCase.getConsumedTokens().containsKey(arc.getStringId())).forEach(arc -> useCase.getConsumedTokens().remove(arc.getStringId()));
        workflowService.save(useCase);
    }

    @Transactional
    public void startExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
        log.info("[" + useCase.getStringId() + "]: Start execution of " + transition.getTitle() + " in case " + useCase.getTitle());
        execute(transition, useCase, arc -> arc.getDestination().equals(transition));
    }

    @Transactional
    protected void execute(Transition transition, Case useCase, Predicate<Arc> predicate) throws TransitionNotExecutableException {
        Supplier<Stream<Arc>> filteredSupplier = () -> useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream().filter(predicate);

        if (!filteredSupplier.get().allMatch(Arc::isExecutable))
            throw new TransitionNotExecutableException("Not all arcs can be executed task [" + transition.getStringId() + "] in case [" + useCase.getTitle() + "]");

        filteredSupplier.get().sorted((o1, o2) -> ArcOrderComparator.getInstance().compare(o1, o2)).forEach(arc -> {
            if (arc instanceof ResetArc) {
                useCase.getConsumedTokens().put(arc.getStringId(), ((Place) arc.getSource()).getTokens());
            }
            if(arc.getReference() != null && arc.getSource() instanceof Place){
                useCase.getConsumedTokens().put(arc.getStringId(), arc.getReference().getMultiplicity());
            }
            arc.execute();
        });

        workflowService.updateMarking(useCase);
    }

    @Transactional
    protected List<EventOutcome> executeTransition(Task task, Case useCase) {
        log.info("[" + useCase.getStringId() + "]: executeTransition [" + task.getTransitionId() + "] in case [" + useCase.getTitle() + "]");
        useCase = workflowService.decrypt(useCase);
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

    @Transactional
    void validateData(Transition transition, Case useCase) {
        for (Map.Entry<String, DataRef> entry : transition.getDataSet().entrySet()) {
            // TODO: NAE-1645 fix validation
//            if (!useCase.getDataField(entry.getKey()).req(transition.getImportId()))
//                continue;
//            if (useCase.getDataField(entry.getKey()).isUndefined(transition.getImportId()) && !entry.getValue().isRequired())
//                continue;
//            Object value = useCase.getDataSet().get(entry.getKey()).getValue();
//            if (value == null) {
//                Field field = useCase.getField(entry.getKey());
//                throw new IllegalArgumentException("Field \"" + field.getName() + "\" has null value");
//            }
//            if (value instanceof String && ((String) value).isEmpty()) {
//                Field field = useCase.getField(entry.getKey());
//                throw new IllegalArgumentException("Field \"" + field.getName() + "\" has empty value");
//            }
        }
    }

    @Transactional
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
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent())
            throw new IllegalArgumentException("Could not find task with id [" + taskId + "]");
        return optionalTask.get();
    }

    @Override
    public Page<Task> getAll(LoggedUser loggedUser, Pageable pageable, Locale locale) {
        List<Task> tasks;
        if (loggedUser.getProcessRoles().isEmpty()) {
            tasks = new ArrayList<>();
            return new PageImpl<>(tasks, pageable, 0L);
        } else {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("{$or:[");
            loggedUser.getProcessRoles().forEach(role -> {
                queryBuilder.append("{\"roles.");
                queryBuilder.append(role);
                queryBuilder.append("\":{$exists:true}},");
            });
            if (!loggedUser.getProcessRoles().isEmpty())
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
        if (searchPredicate != null)
            return taskRepository.count(searchPredicate);
        else
            return 0;
    }

    @Override
    public Page<Task> findByCases(Pageable pageable, List<String> cases) {
        return loadUsers(taskRepository.findByCaseIdIn(pageable, cases));
    }

    @Override
    public Task findById(String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent())
            throw new IllegalArgumentException("Could not find task with id [" + id + "]");
        Task task = taskOptional.get();
        this.setUser(task);
        return task;
    }

    @Override
    public List<Task> findAllById(List<String> ids) {
        return taskRepository.findAllBy_idIn(ids).stream()
                .filter(Objects::nonNull)
                .sorted(Ordering.explicit(ids).onResultOf(Task::getStringId))
                .map(task -> {
                    this.setUser(task);
                    return task;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createTasks(Case useCase) {
        PetriNet net = useCase.getPetriNet();
        Collection<Transition> transitions = net.getTransitions().values();

        for (Transition transition : transitions) {
            if (isExecutable(transition, net)) {
                Task task = createFromTransition(transition, useCase);
                // TODO: 16. 3. 2017 there should be some fancy logic
//                task.setAssignRole(net.getRoles().get(transition.getRoles().keySet().stream().findFirst().orElseGet(null)).getStringId());
                //figureOutProcessRoles(task, transition);
                if (task == null)
                    break;
                save(task);
            }
        }
    }

    @Override
    public Page<Task> findByUser(Pageable pageable, IUser user) {
        return loadUsers(taskRepository.findByUserId(pageable, user.getStringId()));
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
    public List<Task> save(List<Task>  tasks) {
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
            List<String> userIds = getExistingUsers((List<String>) useCase.getDataSet().get(id).getValue());
            if (userIds != null && userIds.size() != 0 && permission.containsKey("view") && !permission.get("view")) {
                task.getNegativeViewUsers().addAll(userIds);
            } else if (userIds != null && userIds.size() != 0) {
                task.addUsers(new HashSet<>(userIds), permission);
            }
        });
        task.resolveViewUsers();
        return taskRepository.save(task);
    }

    private List<String> getExistingUsers(List<String> userIds) {
        if (userIds == null)
            return null;
        return userIds.stream().filter(userId -> userService.resolveById(userId, false) != null).collect(Collectors.toList());
    }

    private Task createFromTransition(Transition transition, Case useCase) {
        final Task task = Task.with()
                .title(transition.getTitle())
                .processId(useCase.getPetriNetId())
                .caseId(useCase.get_id().toString())
                .transitionId(transition.getImportId())
                .layout(transition.getLayout())
                .caseColor(useCase.getColor())
                .caseTitle(useCase.getTitle())
                .priority(transition.getPriority())
                .icon(transition.getIcon() == null ? useCase.getIcon() : transition.getIcon())
//                .immediateDataFields(new LinkedHashSet<>(transition.getImmediateData())) TODO: NAE-1645 fix
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

        resolveUserRef(task, useCase);
        Task savedTask = save(task);

        useCase.addTask(savedTask);
        useCase = workflowService.save(useCase);

        return savedTask;
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
    public void delete(Iterable<? extends Task> tasks, Case useCase) {
        workflowService.removeTasksFromCase(tasks, useCase);
        log.info("[" + useCase.getStringId() + "]: Tasks of case " + useCase.getTitle() + " are being deleted by user [" + userService.getLoggedOrSystem().getStringId() + "]");
        taskRepository.deleteAll(tasks);
        tasks.forEach(t -> elasticTaskService.remove(t.getStringId()));
    }

    @Override
    public void delete(Iterable<? extends Task> tasks, String caseId) {
        workflowService.removeTasksFromCase(tasks, caseId);
        log.info("[" + caseId + "]: Tasks of case are being deleted by user [" + userService.getLoggedOrSystem().getStringId() + "]");
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
        if (task.getUserId() != null)
            task.setUser(userService.resolveById(task.getUserId(), true));
    }

    private EventOutcome addMessageToOutcome(Transition transition, EventType type, TaskEventOutcome outcome) {
        if(transition.getEvents().containsKey(type)){
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
}
