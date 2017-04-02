package com.fmworkflow.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.domain.UserProcessRole;
import com.fmworkflow.auth.domain.UserRepository;
import com.fmworkflow.petrinet.domain.Arc;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.Place;
import com.fmworkflow.petrinet.domain.Transition;
import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotStartableException;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.CaseRepository;
import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.domain.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public List<Task> getAll(LoggedUser loggedUser) {
        User user = userRepository.findOne(loggedUser.getId());
        List<String> roles = new LinkedList<>(user.getUserProcessRoles()).stream().map(UserProcessRole::getRoleId).collect(Collectors.toList());
        return taskRepository.findAllByAssignRoleInOrDelegateRoleIn(roles,roles);
    }

    @Override
    public List<Task> findByCaseId(String caseId) {
        return null;//taskRepository.findByCaseId(caseId);
    }

    @Override
    public Task findById(Long id) {
        return taskRepository.findOne(id);
    }

    @Override
    public void createTasks(Case useCase) {
        PetriNet net = useCase.getPetriNet();
        Collection<Transition> transitions = net.getTransitions().values();

        for (Transition transition : transitions) {
            if (isExecutable(transition, net, useCase)) {
                Task task = new Task();
                task.setTitle(transition.getTitle());
                task.setCaseId(useCase.get_id().toString());
                task.setTransitionId(transition.getObjectId().toString());
                task.setCaseColor(useCase.getColor());
                task.setCaseTitle(useCase.getTitle());
                task.setPriority(transition.getPriority());
                task = taskRepository.save(task);
                task.setVisualId(net.getInitials());
                // TODO: 16. 3. 2017 there should be some fancy logic
//                task.setAssignRole(net.getRoles().get(transition.getRoles().keySet().stream().findFirst().orElseGet(null)).getStringId());
                figureOutProcessRoles(task,transition);
                taskRepository.save(task);
            }
        }
    }

    @Override
    public List<Task> findByUser(User user) {
        return taskRepository.findByUser(user);
    }

    @Override
    public List<Task> findUserFinishedTasks(User user) {
        return taskRepository.findByUserAndFinishDateNotNull(user);
    }

    @Override
    public List<Task> findByPetriNets(List<String> petriNets){
        StringBuilder caseQueryBuilder = new StringBuilder();
        petriNets.forEach(net -> {
            caseQueryBuilder.append("{$ref:\"petriNet\",$id:{$oid:\"");
            caseQueryBuilder.append(net);
            caseQueryBuilder.append("\"}},");
        });
        caseQueryBuilder.deleteCharAt(caseQueryBuilder.length()-1);
        BasicQuery caseQuery = new BasicQuery("{petriNet:{$in:["+caseQueryBuilder.toString()+"]}}","{_id:1}");
        List<Case> useCases = mongoTemplate.find(caseQuery,Case.class);
        return taskRepository.findByCaseIdIn(useCases.stream().map(Case::getStringId).collect(Collectors.toList()));
    }

    @Override
    public List<Task> findByTransitions(List<String> transitions){
        return taskRepository.findByTransitionIdIn(transitions);
    }

    //TODO: 2/4/2017 findByDataFields

    @Override
    @Transactional
    public void finishTask(Long userId, Long taskId) throws Exception {
        Task task = taskRepository.findOne(taskId);
        if (!task.getUser().getId().equals(userId)) {
            throw new Exception("User that is not assigned tried to finish task");
        }

        Case useCase = caseRepository.findOne(task.getCaseId());
        useCase.getPetriNet().initializeArcs();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        useCase.finishTransition(transition);
        task.setFinishDate(LocalDateTime.now());

        caseRepository.save(useCase);
        taskRepository.save(task);
        reloadTasks(useCase);
    }

    @Override
    @Transactional
    public void assignTask(User user, Long taskId) throws TransitionNotStartableException {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        useCase.getPetriNet().initializeArcs();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        useCase.startTransition(transition);
        task.setUser(user);
        task.setStartDate(LocalDateTime.now());

        caseRepository.save(useCase);
        taskRepository.save(task);
        reloadTasks(useCase);
    }

    @Override
    public List<Field> getData(Long taskId) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        Set<String> fieldsIds = transition.getDataSet().keySet();
        List<Field> dataSetFields = new ArrayList<>();
        fieldsIds.forEach(fieldId -> {
            Field field = useCase.getPetriNet().getDataSet().get(fieldId);
            field.setType(null);
            field.setValue(useCase.getDataSetValues().get(fieldId));
            field.setLogic(transition.applyDataLogic(fieldId, JsonNodeFactory.instance.objectNode()));

            dataSetFields.add(field);
        });

        return dataSetFields;
    }

    @Override
    public void setDataFieldsValues(Long taskId, ObjectNode values) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());

        values.fields().forEachRemaining( entry -> useCase.getDataSetValues().put(entry.getKey(),parseFieldsValues(entry.getValue())));
        caseRepository.save(useCase);
    }

    @Override
    @Transactional
    public void cancelTask(Long id, Long taskId) {
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        PetriNet net = useCase.getPetriNet();

        net.getArcsOfTransition(task.getTransitionId()).stream()
                .filter(arc -> arc.getSource() instanceof Place)
                .forEach(arc -> useCase.addActivePlace(arc.getSource().getStringId(), arc.getMultiplicity()));

        taskRepository.delete(taskId);
        caseRepository.save(useCase);
        reloadTasks(useCase);
    }

    @Override
    public FileSystemResource getFile(Long taskId, String fieldId){
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        if(useCase.getDataSetValues().get(fieldId) == null) return null;
        return new FileSystemResource("storage/"+fieldId+"-"+useCase.getDataSetValues().get(fieldId));
    }

    @Override
    @Transactional
    public void delegateTask(String delegatedEmail, Long taskId) throws TransitionNotStartableException {
        User delegated = userRepository.findByEmail(delegatedEmail);
        assignTask(delegated, taskId);
    }

    @Override
    public boolean saveFile(Long taskId, String fieldId, MultipartFile multipartFile){
        try {
            Task task = taskRepository.findOne(taskId);
            Case useCase = caseRepository.findOne(task.getCaseId());

            String oldFile = null;
            if((oldFile = (String)useCase.getDataSetValues().get(fieldId)) != null){
                new File("storage/"+fieldId+"-"+oldFile).delete();
                useCase.getDataSetValues().put(fieldId, null);
            }

            File file = new File("storage/" + fieldId +"-"+ multipartFile.getOriginalFilename());
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fout =new FileOutputStream(file);
            fout.write(multipartFile.getBytes());
            fout.close();

            useCase.getDataSetValues().put(fieldId, multipartFile.getOriginalFilename());
            caseRepository.save(useCase);

            return true;
        } catch (IOException e){
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
    private void reloadTasks(Case useCase) {
        taskRepository.deleteAllByCaseIdAndUserIsNull(useCase.getStringId());
        taskRepository.deleteAllByCaseIdAndFinishDateIsNotNull(useCase.getStringId());
        createTasks(useCase);
    }

    private Object parseFieldsValues(JsonNode jsonNode){
        ObjectNode node = (ObjectNode) jsonNode;
        Object value;
        switch (node.get("type").asText()){
            case "date":
                value = LocalDate.parse(node.get("value").asText());
                break;
            case "boolean":
                value = node.get("value").asBoolean();
                break;
            case "multichoice":
                ArrayNode arrayNode = (ArrayNode) node.get("value");
                HashSet<String> set = new HashSet<>();
                arrayNode.forEach(item -> set.add(item.asText()));
                value = set;
                break;
            case "number":
                value = node.get("value").asDouble();
                break;
            default:
                value = node.get("value").asText();
                break;
        }
        if(value instanceof String && ((String)value).equalsIgnoreCase("null")) return null;
        else return value;
    }

    private void figureOutProcessRoles(Task task, Transition transition){
        transition.getRoles().keySet().forEach((id) -> {
            ObjectNode node = transition.applyRoleLogic(id, JsonNodeFactory.instance.objectNode().put("roleIds",id));
            if(node.get("assign") != null && node.get("assign").asBoolean()) task.setAssignRole(id);
            if(node.get("delegate") != null && node.get("delegate").asBoolean()) task.setDelegateRole(id);
        });
    }

    private boolean isExecutable(Transition transition, PetriNet net, Case useCase) {
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);
        Map<String, Integer> activePlaces = useCase.getActivePlaces();

        if (arcsOfTransition == null)
            return true;

        for (Arc arc : arcsOfTransition) {
            if (arc.getDestination() == transition) {
                if (placeIsNotActive(activePlaces, arc) || placeHasInsufficientTokens(activePlaces, arc))
                    return false;
            }
        }

        return true;
    }

    private boolean placeHasInsufficientTokens(Map<String, Integer> activePlaces, Arc arc) {
        return activePlaces.get(arc.getSourceId().toString()) < arc.getMultiplicity();
    }

    private boolean placeIsNotActive(Map<String, Integer> activePlaces, Arc arc) {
        return !activePlaces.containsKey(arc.getSourceId().toString());
    }
}