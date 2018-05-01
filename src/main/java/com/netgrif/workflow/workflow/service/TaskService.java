package com.netgrif.workflow.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.task.*;
import com.netgrif.workflow.event.events.usecase.SaveCaseDataEvent;
import com.netgrif.workflow.importer.service.FieldFactory;
import com.netgrif.workflow.petrinet.domain.*;
import com.netgrif.workflow.petrinet.domain.arcs.Arc;
import com.netgrif.workflow.petrinet.domain.arcs.ResetArc;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.FileField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository;
import com.netgrif.workflow.workflow.domain.triggers.AutoTrigger;
import com.netgrif.workflow.workflow.domain.triggers.TimeTrigger;
import com.netgrif.workflow.workflow.domain.triggers.Trigger;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Service
public class TaskService implements ITaskService {

    private static final Logger log = Logger.getLogger(TaskService.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TaskSearchService searchService;

    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private FieldActionsRunner actionsRunner;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private FieldFactory fieldFactory;

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
    public Page<Task> search(Map<String, Object> request, Pageable pageable, LoggedUser user) {
        if (request.containsKey("or")) {
            if (((Map<String, Object>) request.get("or")).containsKey("role")) {
                Object roles = ((Map<String, Object>) request.get("or")).get("role");
                Set<String> union = new HashSet<>(user.getProcessRoles());
                if (roles instanceof String)
                    union.add((String) roles);
                else if (roles instanceof List)
                    union.addAll((List) roles);

                ((Map<String, Object>) request.get("or")).put("role", new ArrayList<>(union));

            } else
                ((Map<String, Object>) request.get("or")).put("role", new ArrayList<>(user.getProcessRoles()));

        } else {
            Map<String, Object> orMap = new LinkedHashMap<>();
            orMap.put("role", new ArrayList<>(user.getProcessRoles()));
            request.put("or", orMap);
        }

        return setImmediateFields(loadUsers(searchService.search(request, pageable, Task.class)));
    }

    @Override
    public Page<Task> findByCases(Pageable pageable, List<String> cases) {
        return loadUsers(taskRepository.findByCaseIdIn(pageable, cases));
    }

    @Override
    public Task findById(String id) {
        Task task = taskRepository.findOne(id);
        if (task.getUserId() != null)
            task.setUser(userRepository.findOne(task.getUserId()));
        return task;
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
                taskRepository.save(task);
            }
        }
    }

    @Override
    public Page<Task> findByUser(Pageable pageable, User user) {
        return loadUsers(taskRepository.findByUserId(pageable, user.getId()));
    }

    @Override
    public List<Task> findUserFinishedTasks(User user) {
        return taskRepository.findByUserIdAndFinishDateNotNull(user.getId());
    }

    @Override
    public Page<Task> findByPetriNets(Pageable pageable, List<String> petriNets) {
        StringBuilder caseQueryBuilder = new StringBuilder();
        petriNets.forEach(net -> {
            caseQueryBuilder.append("{$ref:\"petriNet\",$id:{$oid:\"");
            caseQueryBuilder.append(net);
            caseQueryBuilder.append("\"}},");
        });
        caseQueryBuilder.deleteCharAt(caseQueryBuilder.length() - 1);
        BasicQuery caseQuery = new BasicQuery("{petriNet:{$in:[" + caseQueryBuilder.toString() + "]}}", "{_id:1}");
        List<Case> useCases = mongoTemplate.find(caseQuery, Case.class);
        return loadUsers(taskRepository.findByCaseIdIn(pageable, useCases.stream().map(Case::getStringId).collect(Collectors.toList())));
    }

    @Override
    public Page<Task> findByTransitions(Pageable pageable, List<String> transitions) {
        return loadUsers(taskRepository.findByTransitionIdIn(pageable, transitions));
    }

    //TODO: 2/4/2017 findByDataFields

    @Override
    @Transactional
    public void finishTask(LoggedUser loggedUser, String taskId) throws IllegalArgumentException, TransitionNotExecutableException {
        Task task = taskRepository.findOne(taskId);
        User user = userRepository.findOne(loggedUser.getId());
        if (task == null) {
            throw new IllegalArgumentException("Could not find task with id=" + taskId);
        } else if (task.getUserId() == null) {
            throw new IllegalArgumentException("Task with id=" + taskId + " is not assigned to any user.");
        }
        // TODO: 14. 4. 2017 replace with @PreAuthorize
        if (!task.getUserId().equals(loggedUser.getId())) {
            throw new IllegalArgumentException("User that is not assigned tried to finish task");
        }

        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        validateData(transition, useCase);
        finishExecution(transition, useCase);
        task.setFinishDate(LocalDateTime.now());
        task.setFinishedBy(task.getUserId());
        task.setUserId(null);

        workflowService.save(useCase);
        taskRepository.save(task);
        reloadTasks(useCase, loggedUser.getId());

        publisher.publishEvent(new UserFinishTaskEvent(user, task, useCase));
    }

    @Override
    public void finishTask(String taskId) throws IllegalArgumentException, TransitionNotExecutableException {
        LoggedUser user = userService.getLoggedOrSystem();
        finishTask(user, taskId);
    }

    @Override
    @Transactional
    public void assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException {
        Task task = taskRepository.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        User user = userRepository.findOne(loggedUser.getId());

        assignTaskToUser(user, task, useCase);

        publisher.publishEvent(new UserAssignTaskEvent(user, task, useCase));
    }

    @Override
    public void assignTask(String taskId) throws TransitionNotExecutableException {
        LoggedUser user = userService.getLoggedOrSystem();
        assignTask(user, taskId);
    }

    @Override
    public List<Field> getData(String taskId) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());

        return getData(task, useCase);
    }

    @Override
    public List<Field> getData(Task task, Case useCase) {
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        Set<String> fieldsIds = transition.getDataSet().keySet();
        List<Field> dataSetFields = new ArrayList<>();

        fieldsIds.forEach(fieldId -> {
            resolveActions(useCase.getPetriNet().getField(fieldId).get(),
                    Action.ActionTrigger.GET, useCase, transition);

            if (useCase.hasFieldBehavior(fieldId, transition.getStringId())) {
                if (useCase.getDataSet().get(fieldId).isDisplayable(transition.getStringId())) {
                    Field field = fieldFactory.buildFieldWithValidation(useCase, fieldId);
                    field.setBehavior(useCase.getDataSet().get(fieldId).applyBehavior(transition.getStringId()));
                    dataSetFields.add(field);
                }
            } else {
                if (transition.getDataSet().get(fieldId).isDisplayable()) {
                    Field field = fieldFactory.buildFieldWithValidation(useCase, fieldId);
                    field.setBehavior(transition.getDataSet().get(fieldId).applyBehavior());
                    dataSetFields.add(field);
                }
            }
        });
        LongStream.range(0L, dataSetFields.size())
                .forEach(index -> dataSetFields.get((int) index).setOrder(index));

        workflowService.save(useCase);
        return dataSetFields;
    }

    @Override
    public List<DataGroup> getDataGroups(String taskId) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        return new ArrayList<>(transition.getDataGroups().values());
    }

    public Page<Task> setImmediateFields(Page<Task> tasks) {
        tasks.getContent().forEach(task -> task.setImmediateData(getImmediateFields(task)));
        return tasks;
    }

    public List<Field> getImmediateFields(Task task) {
        Case useCase = workflowService.findOne(task.getCaseId());

        List<Field> fields = task.getImmediateDataFields().stream().map(id -> fieldFactory.buildFieldWithoutValidation(useCase, id)).collect(Collectors.toList());
        LongStream.range(0L, fields.size()).forEach(index -> fields.get((int) index).setOrder(index));

        return fields;
    }

    @Override
    public List<TaskReference> findAllByCase(String caseId, Locale locale) {
        return taskRepository.findAllByCaseId(caseId).stream()
                .map(task -> new TaskReference(task.getStringId(), task.getTitle().getTranslation(locale), task.getTransitionId()))
                .collect(Collectors.toList());
    }

    @Override
    public ChangedFieldContainer setData(String taskId, ObjectNode values) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());

        Map<String, ChangedField> changedFields = new HashMap<>();
        values.fields().forEachRemaining(entry -> {
            useCase.getDataSet().get(entry.getKey()).setValue(parseFieldsValues(entry.getValue()));
            //changedFields.put(entry.getKey(), new ChangedField(entry.getKey()));
            resolveActions(useCase.getPetriNet().getField(entry.getKey()).get(),
                    Action.ActionTrigger.SET, useCase, useCase.getPetriNet().getTransition(task.getTransitionId()))
                    .forEach((key, changedField) -> {
                        if (changedFields.containsKey(changedField.getId()))
                            changedFields.get(changedField.getId()).merge(changedField);
                        else
                            changedFields.put(changedField.getId(), changedField);
                    });
        });

        workflowService.save(useCase);
        publisher.publishEvent(new SaveCaseDataEvent(useCase, changedFields.values()));

        ChangedFieldContainer container = new ChangedFieldContainer();
        container.putAll(changedFields);
        return container;
    }

    private Map<String, ChangedField> resolveActions(Field field, Action.ActionTrigger actionTrigger, Case useCase, Transition transition) {
        Map<String, ChangedField> changedFields = new HashMap<>();
        processActions(field, actionTrigger, useCase, transition, changedFields);
        return changedFields;
    }

    private void processActions(Field field, Action.ActionTrigger actionTrigger, Case useCase, Transition transition, Map<String, ChangedField> changedFields) {
        LinkedList<Action> fieldActions = new LinkedList<>();
        if (field.getActions() != null)
            fieldActions.addAll(DataFieldLogic.getActionByTrigger(field.getActions(), actionTrigger));
        if (transition.getDataSet().containsKey(field.getStringId()) && !transition.getDataSet().get(field.getStringId()).getActions().isEmpty())
            fieldActions.addAll(DataFieldLogic.getActionByTrigger(transition.getDataSet().get(field.getStringId()).getActions(), actionTrigger));

        if (fieldActions.isEmpty()) return;

        runActions(fieldActions, actionTrigger, useCase, transition, changedFields, actionTrigger == Action.ActionTrigger.SET);
    }

    private void runActions(List<Action> actions, Action.ActionTrigger trigger, Case useCase, Transition transition, Map<String, ChangedField> changedFields, boolean recursive) {
        actions.forEach(action -> {
            ChangedField changedField = actionsRunner.run(action, useCase);

            if (changedField.getId() == null) return;

            if (changedFields.containsKey(changedField.getId()))
                changedFields.get(changedField.getId()).merge(changedField);
            else
                changedFields.put(changedField.getId(), changedField);

            if ((changedField.getAttributes().containsKey("value") && changedField.getAttributes().get("value") != null) && recursive)
                processActions(useCase.getPetriNet().getField(changedField.getId()).get(), trigger,
                        useCase, transition, changedFields);
        });
    }

    private Object parseFieldsValues(JsonNode jsonNode) {
        ObjectNode node = (ObjectNode) jsonNode;
        Object value;
        switch (node.get("type").asText()) {
            case "date":
                value = LocalDate.parse(node.get("value").asText());
                break;
            case "boolean":
                value = node.get("value") != null && node.get("value").asBoolean();
                break;
            case "multichoice":
                ArrayNode arrayNode = (ArrayNode) node.get("value");
                HashSet<String> set = new HashSet<>();
                arrayNode.forEach(item -> set.add(item.asText()));
                value = set;
                break;
            case "user":
                if (node.get("value") == null) {
                    value = null;
                    break;
                }
                User user = userRepository.findByEmail(node.get("value").asText());
                user.setPassword(null);
                user.setGroups(null);
                user.setAuthorities(null);
                user.setUserProcessRoles(null);
                value = user;
                break;
            case "number":
                if (node.get("value") == null) {
                    value = 0.0;
                    break;
                }
                value = node.get("value").asDouble();
                break;
            default:
                if (node.get("value") == null) {
                    value = "null";
                    break;
                }
                value = node.get("value").asText();
                break;
        }
        if (value instanceof String && ((String) value).equalsIgnoreCase("null")) return null;
        else return value;
    }

    @Override
    @Transactional
    public void cancelTask(LoggedUser loggedUser, String taskId) {
        Task task = taskRepository.findOne(taskId);
        User user = userRepository.findOne(loggedUser.getId());
        Case useCase = workflowService.findOne(task.getCaseId());

        task = cancelTaskWithoutReload(task, useCase);
        reloadTasks(useCase, loggedUser.getId());

        publisher.publishEvent(new UserCancelTaskEvent(user, task, useCase));
    }

    @Override
    public void cancelTasksWithoutReload(Set<String> transitions, String caseId) {
        List<Task> tasks = taskRepository.findAllByTransitionIdInAndCaseId(transitions, caseId);
        Case useCase = null;
        for (Task task : tasks) {
            if (task.getUserId() != null) {
                if (useCase == null)
                    useCase = workflowService.findOne(task.getCaseId());
                cancelTaskWithoutReload(task, useCase);
            }
        }
    }

    private Task cancelTaskWithoutReload(Task task, Case useCase) {
        PetriNet net = useCase.getPetriNet();
        net.getArcsOfTransition(task.getTransitionId()).stream()
                .filter(arc -> arc.getSource() instanceof Place)
                .forEach(arc -> {
                    if (arc instanceof ResetArc) {
                        ((ResetArc) arc).setRemovedTokens(useCase.getResetArcTokens().get(arc.getStringId()));
                        useCase.getResetArcTokens().remove(arc.getStringId());
                    }
                    arc.rollbackExecution();
                });
        workflowService.updateMarking(useCase);

        task.setUserId(null);
        task.setStartDate(null);
        task = taskRepository.save(task);
        workflowService.save(useCase);

        return task;
    }

    @Override
    public FileSystemResource getFile(String taskId, String fieldId) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);

        if (field.isGenerated()) {
            field.getActions().forEach(action -> actionsRunner.run(action, useCase));
            if (useCase.getDataSet().get(fieldId).getValue() == null)
                return null;

            workflowService.save(useCase);
            return new FileSystemResource(field.getFilePath((String) useCase.getDataSet().get(fieldId).getValue()));

        } else {
            if (useCase.getDataSet().get(fieldId).getValue() == null)
                return null;
            return new FileSystemResource(field.getFilePath((String) useCase.getDataSet().get(fieldId).getValue()));
        }
    }

    @Override
    @Transactional
    public void delegateTask(LoggedUser loggedUser, String delegatedEmail, String taskId) throws TransitionNotExecutableException {
        User delegated = userRepository.findByEmail(delegatedEmail);
        User delegate = userRepository.findOne(loggedUser.getId());
        Task task = taskRepository.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());

        task.setUserId(delegated.getId());
        taskRepository.save(task);

        publisher.publishEvent(new UserDelegateTaskEvent(delegate, task, useCase, delegated));
    }

    @Override
    public boolean saveFile(String taskId, String fieldId, MultipartFile multipartFile) {
        try {
            Task task = taskRepository.findOne(taskId);
            Case useCase = workflowService.findOne(task.getCaseId());
            FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);

            String oldFile = null;
            if ((oldFile = (String) useCase.getDataSet().get(fieldId).getValue()) != null) {
                new File(field.getFilePath(oldFile)).delete();
                useCase.getDataSet().get(fieldId).setValue(null);
            }

            File file = new File(field.getFilePath(multipartFile.getOriginalFilename()));
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fout = new FileOutputStream(file);
            fout.write(multipartFile.getBytes());
            fout.close();

            useCase.getDataSet().get(fieldId).setValue(multipartFile.getOriginalFilename());
            workflowService.save(useCase);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
    @Transactional
    void reloadTasks(Case useCase, Long userId) {
        PetriNet net = useCase.getPetriNet();

        net.getTransitions().values().forEach(transition -> {
            List<Task> tasks = taskRepository.findAllByCaseId(useCase.getStringId());
            if (isExecutable(transition, net)) {
                if (taskIsNotPresent(tasks, transition, userId)) {
                    createFromTransition(transition, useCase);
                }
            } else {
                deleteUnassignedNotExecutableTasks(tasks, transition, useCase);
            }
        });
    }

    @Transactional
    void deleteUnassignedNotExecutableTasks(List<Task> tasks, Transition transition, Case useCase) {
        delete(tasks.stream()
                .filter(task -> task.getTransitionId().equals(transition.getStringId()) && task.getUserId() == null)
                .collect(Collectors.toList()), useCase);
    }

    @Transactional
    boolean taskIsNotPresent(List<Task> tasks, Transition transition, Long userId) {
        return tasks.stream().noneMatch(task -> task.getTransitionId().equals(transition.getStringId()));
    }

    @Transactional
    boolean isNotExecutable(Transition transition, PetriNet net) {
        return !isExecutable(transition, net);
    }

    @Transactional
    boolean isExecutable(Transition transition, PetriNet net) {
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);

        if (arcsOfTransition == null)
            return true;

        return arcsOfTransition.stream()
                .filter(arc -> arc.getDestination() == transition) // todo: from same source error
                .allMatch(Arc::isExecutable);
    }

    @Transactional
    void finishExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
        log.info("Finish execution of " + transition.getTitle() + " in case " + useCase.getTitle());
        execute(transition, useCase, arc -> arc.getSource() == transition);
        useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream()
                .filter(arc -> arc instanceof ResetArc)
                .forEach(arc -> useCase.getResetArcTokens().remove(arc.getStringId()));
    }

    @Transactional
    public void startExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
        log.info("Start execution of " + transition.getTitle() + " in case " + useCase.getTitle());
        execute(transition, useCase, arc -> arc.getDestination() == transition);
    }

    @Transactional
    protected void execute(Transition transition, Case useCase, Predicate<Arc> predicate) throws TransitionNotExecutableException {
        Supplier<Stream<Arc>> filteredSupplier = () -> useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream().filter(predicate);

        if (!filteredSupplier.get().allMatch(Arc::isExecutable))
            throw new TransitionNotExecutableException("Not all arcs can be executed.");

        filteredSupplier.get().forEach(arc -> {
            if (arc instanceof ResetArc) {
                useCase.getResetArcTokens().put(arc.getStringId(), ((Place) arc.getSource()).getTokens());
            }
            arc.execute();
        });

        workflowService.updateMarking(useCase);
    }

    private Task createFromTransition(Transition transition, Case useCase) {
        final Task task = Task.with()
                .title(transition.getTitle())
                .processId(useCase.getPetriNetId())
                .caseId(useCase.get_id().toString())
                .transitionId(transition.getImportId())
                .caseColor(useCase.getColor())
                .caseTitle(useCase.getTitle())
                .priority(transition.getPriority())
                .icon(transition.getIcon() == null ? useCase.getIcon() : transition.getIcon())
                .immediateDataFields(new LinkedHashSet<>(transition.getImmediateData()))
                .assignPolicy(transition.getAssignPolicy())
                .dataFocusPolicy(transition.getDataFocusPolicy())
                .finishPolicy(transition.getFinishPolicy())
                .build();

        for (Trigger trigger : transition.getTriggers()) {
            Trigger taskTrigger = trigger.clone();
            task.addTrigger(taskTrigger);

            if (taskTrigger instanceof TimeTrigger) {
                TimeTrigger timeTrigger = (TimeTrigger) taskTrigger;
                scheduleTaskExecution(task, timeTrigger.getStartDate(), useCase);
            } else if (taskTrigger instanceof AutoTrigger) {
                executeTransition(task, useCase);
                log.info("Auto trigger triggered");
                return null;
            }
        }
        for (Map.Entry<String, Set<RolePermission>> entry : transition.getRoles().entrySet()) {
            task.addRole(entry.getKey(), entry.getValue());
        }

        Transaction transaction = useCase.getPetriNet().getTransactionByTransition(transition);
        if (transaction != null) {
            task.setTransactionId(transaction.getStringId());
        }
        Task savedTask = taskRepository.save(task);

        useCase.addTask(savedTask);
        useCase = workflowService.save(useCase);

        publisher.publishEvent(new CreateTaskEvent(savedTask, useCase));

        return savedTask;
    }

    private Page<Task> loadUsers(Page<Task> tasks) {
        Map<Long, User> users = new HashMap<>();
        tasks.forEach(task -> {
            if (task.getUserId() != null) {
                if (users.containsKey(task.getUserId()))
                    task.setUser(users.get(task.getUserId()));
                else {
                    task.setUser(userRepository.findOne(task.getUserId()));
                    users.put(task.getUserId(), task.getUser());
                }
            }
        });

        return tasks;
    }

    public void delete(Iterable<? extends Task> tasks, Case useCase) {
        workflowService.removeTasksFromCase(tasks, useCase);
        taskRepository.delete(tasks);
    }

    public void delete(Iterable<? extends Task> tasks, String caseId) {
        workflowService.removeTasksFromCase(tasks, caseId);
        taskRepository.delete(tasks);
    }

    @Override
    public void deleteTasksByCase(String caseId) {
        delete(taskRepository.findAllByCaseId(caseId), caseId);
    }

    @Transactional
    protected void assignTaskToUser(User user, Task task, Case useCase) throws TransitionNotExecutableException {
        useCase.getPetriNet().initializeArcs();// TODO: 19/06/2017 remove?
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        startExecution(transition, useCase);
        task.setUserId(user.getId());
        task.setStartDate(LocalDateTime.now());

        workflowService.save(useCase);
        taskRepository.save(task);
        reloadTasks(useCase, user.getId());
    }

    @Transactional
    protected void executeTransition(Task task, Case useCase) {
        log.info("executeTransition");
        useCase = workflowService.decrypt(useCase);
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
        try {
            startExecution(transition, useCase);
            getData(task, useCase);
            validateData(transition, useCase);
            finishExecution(transition, useCase);

            workflowService.save(useCase);
            reloadTasks(useCase, -1L);
        } catch (TransitionNotExecutableException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    void validateData(Transition transition, Case useCase) {
        for (Map.Entry<String, DataFieldLogic> entry : transition.getDataSet().entrySet()) {
            if (!entry.getValue().isRequired())
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

    @Transactional
    protected void scheduleTaskExecution(Task task, LocalDateTime time, Case useCase) {
        log.info("Task " + task.getTitle() + " scheduled to run at " + time.toString());
        scheduler.schedule(() -> executeTransition(task, useCase), DateUtils.localDateTimeToDate(time));
        publisher.publishEvent(new TimeFinishTaskEvent(time, task, useCase));
    }
}