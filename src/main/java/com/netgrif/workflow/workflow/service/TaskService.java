package com.netgrif.workflow.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.petrinet.domain.*;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.petrinet.domain.dataset.logic.logic.FileFieldLogic;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.FieldValidationRunner;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository;
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
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

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    //    @Override
//    public Page<Task> getAll(LoggedUser loggedUser, Pageable pageable) {
//        User user = userRepository.findOne(loggedUser.getId());
//        List<String> roles = new LinkedList<>(user.getUserProcessRoles()).stream().map(UserProcessRole::getRoleId).collect(Collectors.toList());
//        return loadUsers(taskRepository.findAllByAssignRoleInOrDelegateRoleIn(pageable, roles, roles));
//    }
    @Override
    public Page<Task> getAll(LoggedUser loggedUser, Pageable pageable) {
        List<Task> tasks;
        if (loggedUser.getProcessRoles().isEmpty()) {
            tasks = new ArrayList<>();
            return new PageImpl<Task>(tasks, pageable, 0L);
        } else {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("{$or:[");
            loggedUser.getProcessRoles().forEach(role -> {
                queryBuilder.append("{\"roles.");
                queryBuilder.append(role);
                queryBuilder.append("\":{$exists:true}},");
            });
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
            queryBuilder.append("]}");
            BasicQuery query = new BasicQuery(queryBuilder.toString());
            query = (BasicQuery) query.with(pageable);
            tasks = mongoTemplate.find(query, Task.class);
            return loadUsers(new PageImpl<Task>(tasks, pageable,
                    mongoTemplate.count(new BasicQuery(queryBuilder.toString(), "{_id:1}"), Task.class)));
        }
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
    public void finishTask(Long userId, String taskId) throws Exception {
        Task task = taskRepository.findOne(taskId);
        // TODO: 14. 4. 2017 replace with @PreAuthorize
        if (!task.getUserId().equals(userId)) {
            throw new Exception("User that is not assigned tried to finish task");
        }

        Case useCase = caseRepository.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        finishExecution(transition, useCase);
        task.setFinishDate(LocalDateTime.now());

        caseRepository.save(useCase);
        taskRepository.save(task);
        reloadTasks(useCase);
    }

    @Override
    @Transactional
    public void assignTask(User user, String taskId) throws TransitionNotExecutableException {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        useCase.getPetriNet().initializeArcs();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        startExecution(transition, useCase);
        task.setUserId(user.getId());
        task.setStartDate(LocalDateTime.now());

        caseRepository.save(useCase);
        taskRepository.save(task);
        reloadTasks(useCase);

//        moveAttributes(task);
    }

    @Override
    public List<Field> getData(String taskId) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        Set<String> fieldsIds = transition.getDataSet().keySet();
        List<Field> dataSetFields = new ArrayList<>();

        fieldsIds.forEach(fieldId -> {
            if (useCase.hasFieldBehavior(fieldId, transition.getStringId())) {
                if (useCase.getDataSet().get(fieldId).isDisplayable(transition.getStringId())) {
                    Field field = buildField(useCase, fieldId);
                    field.setBehavior(useCase.getDataSet().get(fieldId).applyBehavior(transition.getStringId()));
                    dataSetFields.add(field);
                }
            } else {
                if (transition.getDataSet().get(fieldId).isDisplayable()) {
                    Field field = buildField(useCase, fieldId);
                    field.setBehavior(transition.getDataSet().get(fieldId).applyBehavior());
                    dataSetFields.add(field);
                }
            }
        });
        LongStream.range(0L, dataSetFields.size())
                .forEach(index -> dataSetFields.get((int) index).setOrder(index));

        return dataSetFields;
    }

    private Field buildField(Case useCase, String fieldId) {
        Field field = useCase.getPetriNet().getDataSet().get(fieldId);
        field.setValue(useCase.getDataSet().get(fieldId).getValue());
        if (field instanceof ValidableField && ((ValidableField) field).getValidationRules() != null)
            ((ValidableField) field).setValidationJS(FieldValidationRunner
                    .toJavascript(field, ((ValidableField) field).getValidationRules()));
        resolveDataValues(field);
        return field;
    }

    private void resolveDataValues(Field field) {
        if (field instanceof DateField) {
            ((DateField) field).convertValue();
        } else if (field instanceof NumberField && field.getValue() instanceof Integer) {
            field.setValue(((Integer) field.getValue()).doubleValue());
        }
    }

    @Override
    public ObjectNode setData(String taskId, ObjectNode values) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());

        Map<String, ChangedField> changedFields = new HashMap<>();
        values.fields().forEachRemaining(entry -> {
            useCase.getDataSet().get(entry.getKey()).setValue(parseFieldsValues(entry.getValue()));
            changedFields.put(entry.getKey(), new ChangedField(entry.getKey()));
            runActions(useCase.getPetriNet().getTransition(task.getTransitionId()).getDataSet().get(entry.getKey()).getActions(), useCase, changedFields);
            changedFields.remove(entry.getKey());
        });

        caseRepository.save(useCase);

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        changedFields.forEach((id, field) -> node.set(id, field.toJson()));
        return node;
    }

    private void runActions(Set<String> actions, Case useCase, Map<String, ChangedField> changedFields) {
        actions.forEach(action -> {
            ChangedField field = FieldActionsRunner.run(action, useCase);

            if (changedFields.containsKey(field.getId()))
                changedFields.get(field.getId()).merge(field);
            else
                changedFields.put(field.getId(), field);

            if (field.getValue() != null)
                getTransitionsByField(field.getId(), useCase.getPetriNet()).forEach(transition ->
                        runActions(transition.getDataSet().get(field.getId()).getActions(), useCase, changedFields)
                );
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
                User user = userRepository.findByEmail(node.get("value").asText());
                user.setPassword(null);
                user.setOrganizations(null);
                user.setAuthorities(null);
                user.setUserProcessRoles(null);
                value = user;
                break;
            case "number":
                value = node.get("value").asDouble();
                break;
            default:
                value = node.get("value").asText();
                break;
        }
        if (value instanceof String && ((String) value).equalsIgnoreCase("null")) return null;
        else return value;
    }

    private List<Transition> getTransitionsByField(String field, PetriNet net) {
        List<Transition> transitions = new ArrayList<>();
        net.getTransitions().forEach((transId, trans) -> {
            if (trans.getDataSet().containsKey(field))
                transitions.add(trans);
        });
        return transitions;
    }

    @Override
    @Transactional
    public void cancelTask(Long id, String taskId) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        PetriNet net = useCase.getPetriNet();

        net.getArcsOfTransition(task.getTransitionId()).stream()
                .filter(arc -> arc.getSource() instanceof Place)
                .forEach(Arc::rollbackExecution);
        useCase.updateActivePlaces();

        taskRepository.delete(taskId);
        caseRepository.save(useCase);
        reloadTasks(useCase);

//        moveAttributes(task);
    }

    @Transactional
    private void moveAttributes(Task oldTask) {
        Task newTask = taskRepository.findByTransitionIdAndCaseId(oldTask.getTransitionId(), oldTask.getCaseId());
        newTask.setRequiredFilled(oldTask.getRequiredFilled());

        taskRepository.save(newTask);
    }

    @Override
    public FileSystemResource getFile(String taskId, String fieldId) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);

        if(field.isGenerated()){
            List<Object> result = new FileFieldLogic(useCase,field).executeLogic();
            if(result == null) return null;
            if(result.isEmpty()) return null;

            caseRepository.save(useCase);
            return new FileSystemResource((File)result.get(0));

        } else {
            if (useCase.getDataSet().get(fieldId).getValue() == null) return null;
            return new FileSystemResource(field.getFilePath((String)useCase.getDataSet().get(fieldId).getValue()));
        }
    }

    @Override
    @Transactional
    public void delegateTask(Long userId, String delegatedEmail, String taskId) throws TransitionNotExecutableException {
        User delegated = userRepository.findByEmail(delegatedEmail);
        User delegate = userRepository.findOne(userId);

        Task task = taskRepository.findOne(taskId);
        task.setUserId(delegated.getId());
        taskRepository.save(task);
    }

    @Override
    public boolean saveFile(String taskId, String fieldId, MultipartFile multipartFile) {
        try {
            Task task = taskRepository.findOne(taskId);
            Case useCase = caseRepository.findOne(task.getCaseId());
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
            caseRepository.save(useCase);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reloads all tasks of given case.
     * 1. delete unassigned tasks
     * 2. delete finished tasks
     * 3. generate new tasks
     */
    @Transactional
    void reloadTasks(Case useCase) {
        taskRepository.deleteAllByCaseIdAndUserIdIsNull(useCase.getStringId());
        taskRepository.deleteAllByCaseIdAndFinishDateIsNotNull(useCase.getStringId());
        createTasks(useCase);
    }

//    private void figureOutProcessRoles(Task task, Transition transition) {
//        transition.getRoles().keySet().forEach((id) -> {
//            ObjectNode node = transition.applyRoleLogic(id, JsonNodeFactory.instance.objectNode().put("roleIds", id));
//            if (node.get("assign") != null && node.get("assign").asBoolean()) task.setAssignRole(id);
//            if (node.get("delegate") != null && node.get("delegate").asBoolean()) task.setDelegateRole(id);
//        });
//    }

    @Transactional
    boolean isExecutable(Transition transition, PetriNet net) {
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);

        if (arcsOfTransition == null)
            return true;

        return arcsOfTransition.stream()
                .filter(arc -> arc.getDestination() == transition)
                .allMatch(Arc::isExecutable);
    }

    @Transactional
    void finishExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
        execute(transition, useCase, arc -> arc.getSource() == transition);
    }

    @Transactional
    public void startExecution(Transition transition, Case useCase) throws TransitionNotExecutableException {
        execute(transition, useCase, arc -> arc.getDestination() == transition);
    }

    private void execute(Transition transition, Case useCase, Predicate<Arc> predicate) throws TransitionNotExecutableException {
        Supplier<Stream<Arc>> filteredSupplier = () -> useCase.getPetriNet().getArcsOfTransition(transition.getStringId()).stream().filter(predicate);

        if (!filteredSupplier.get().allMatch(Arc::isExecutable))
            throw new TransitionNotExecutableException("Not all arcs can be executed.");

        filteredSupplier.get().forEach(Arc::execute);

        useCase.updateActivePlaces();
    }

    private Task createFromTransition(Transition transition, Case useCase) {
        Task task = new Task();

        task.setTitle(transition.getTitle());
        task.setCaseId(useCase.get_id().toString());
        task.setTransitionId(transition.getObjectId().toString());
        task.setCaseColor(useCase.getColor());
        task.setCaseTitle(useCase.getTitle());
        task.setPriority(transition.getPriority());
        for (Map.Entry<String, Set<RolePermission>> entry : transition.getRoles().entrySet()) {
            task.addRole(entry.getKey(), entry.getValue());
        }
        task = taskRepository.save(task);

        Transaction transaction = useCase.getPetriNet().getTransactionByTransition(transition);
        if (transaction != null) {
            task.setTransactionId(transaction.getStringId());
        }

        return task;
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

    public void deleteTasksByCase(String caseId){
        taskRepository.deleteAllByCaseId(caseId);
    }
}