package com.netgrif.workflow.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.usecase.SaveCaseDataEvent;
import com.netgrif.workflow.importer.service.FieldFactory;
import com.netgrif.workflow.petrinet.domain.*;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.responsebodies.DataFieldsResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Service
public class DataService implements IDataService {

    private static final Logger log = LoggerFactory.getLogger(DataService.class);

    public static final int MONGO_ID_LENGTH = 24;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IUserService userService;

    @Autowired
    private FieldFactory fieldFactory;

    @Autowired
    private FieldActionsRunner actionsRunner;

    @Override
    public List<Field> getData(String taskId) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());

        return getData(task, useCase);
    }

    @Override
    public List<Field> getData(Task task, Case useCase) {
        log.info("[" + useCase.getStringId() + "]: Getting data of task " + task.getTransitionId() + " [" + task.getStringId() + "]");
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        Set<String> fieldsIds = transition.getDataSet().keySet();
        List<Field> dataSetFields = new ArrayList<>();

        fieldsIds.forEach(fieldId -> {
            if (isForbidden(fieldId, transition, useCase.getDataField(fieldId)))
                return;

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

        workflowService.save(useCase);

        LongStream.range(0L, dataSetFields.size())
                .forEach(index -> dataSetFields.get((int) index).setOrder(index));

        return dataSetFields;
    }

    private boolean isForbidden(String fieldId, Transition transition, DataField dataField) {
        if (dataField.getBehavior().containsKey(transition.getImportId())) {
            return dataField.isForbidden(transition.getImportId());
        } else {
            return transition.getDataSet().get(fieldId).isForbidden();
        }
    }

    @Override
    public ChangedFieldContainer setData(String taskId, ObjectNode values) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        ChangedFieldContainer container = new ChangedFieldContainer();

        log.info("[" + useCase.getStringId() + "]: Setting data of task " + task.getTransitionId() + " [" + task.getStringId() + "]");

        Map<String, ChangedField> changedFields = new HashMap<>();
        values.fields().forEachRemaining(entry -> {
            DataField dataField = useCase.getDataSet().get(entry.getKey());
            String fieldId = entry.getKey();
            if (entry.getKey().startsWith(taskId)) {
                fieldId = fieldId.replace(taskId + "-", "");
                dataField = useCase.getDataField(fieldId);
            }
            if (dataField != null) {
                dataField.setValue(parseFieldsValues(entry.getValue()));
                Map<String, ChangedField> changedFieldMap = resolveActions(useCase.getPetriNet().getField(fieldId).get(),
                        Action.ActionTrigger.SET, useCase, useCase.getPetriNet().getTransition(task.getTransitionId()));
                mergeChanges(changedFields, changedFieldMap);
            } else try {
                if (entry.getKey().contains("-")) {
                    String[] parts = entry.getKey().split("-");
                    ChangedFieldContainer changedFieldContainer = setData(parts[0], values);
                    for (Map.Entry<String, Map<String, Object>> stringMapEntry : changedFieldContainer.getChangedFields().entrySet()) {
                        container.getChangedFields().put(parts[0] + "-" + stringMapEntry.getKey(), stringMapEntry.getValue());
                    }
                }
            } catch (Exception e) {
                log.error("Faile", e);
            }
        });
        updateDataset(useCase);
        workflowService.save(useCase);
        publisher.publishEvent(new SaveCaseDataEvent(useCase, values, changedFields.values()));

        container.putAll(changedFields);
        return container;
    }

    @Override
    public List<DataGroup> getDataGroups(String taskId, Locale locale) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        PetriNet net = useCase.getPetriNet();
        Transition transition = net.getTransition(task.getTransitionId());

        List<Field> data1 = getData(taskId);
        Map<String, Field> dataFieldMap = data1.stream().collect(Collectors.toMap(Field::getImportId, field -> field));
        ArrayList<DataGroup> dataGroups = new ArrayList<>(transition.getDataGroups().values());
        for (DataGroup dataGroup : dataGroups) {
            List<Field> resources = new LinkedList<>();
            for (String datum : dataGroup.getData()) {
                Field field = net.getDataSet().get(datum);
                if (field.getType() == FieldType.TASK_REF) {
                    collectTaskRefDataGroups((TaskField) dataFieldMap.get(datum), resources);
                } else if (dataFieldMap.containsKey(datum)) {
                    resources.add(dataFieldMap.get(datum));
                }
            }
            dataGroup.setFields(new DataFieldsResource(resources, locale));
        }

        return dataGroups;
    }

    private void collectTaskRefDataGroups(TaskField taskRefField, List<Field> resources) {
        collectTaskRefDataGroups(taskRefField, resources, new HashSet<>());
    }

    private void collectTaskRefDataGroups(TaskField taskRefField, List<Field> resources, Set<String> collectedTasks) {
        List<String> taskIds = taskRefField.getValue();
        if (taskIds == null) {
            return;
        }

        taskIds = taskIds.stream().filter(it -> !collectedTasks.contains(it)).collect(Collectors.toList());
        taskIds.forEach(taskId -> {
            List<Field> data = getData(taskId);
            Map<String, Field> dataFieldMap = data.stream().collect(Collectors.toMap(Field::getImportId, field -> field));

            collectedTasks.add(taskId);
            data.forEach(field -> {
                if (field.getType() == FieldType.TASK_REF) {
                    collectTaskRefDataGroups((TaskField) dataFieldMap.get(field.getImportId()), resources, collectedTasks);
                } else {
                    field.setImportId(taskId + "-" + field.getImportId());
                    field.setOrder((long) (resources.size() - 1));
                    resources.add(field);
                }
            });
        });
    }

    @Override
    public FileFieldInputStream getFileByTask(String taskId, String fieldId) {
        TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
        Task task = wrapper.getTask();
        String parsedFieldId = wrapper.getParsedFieldId();

        return getFileByCase(task.getCaseId(), parsedFieldId);
    }

    @Override
    public FileFieldInputStream getFileByCase(String caseId, String fieldId) {
        Case useCase = workflowService.findOne(caseId);
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFile(useCase, field);
    }

    @Override
    public FileFieldInputStream getFile(Case useCase, FileField field) {
        field.getActions().forEach(action -> actionsRunner.run(action, useCase));
        if (useCase.getDataSet().get(field.getStringId()).getValue() == null)
            return null;

        workflowService.save(useCase);
        field.setValue((FileFieldValue) useCase.getDataSet().get(field.getStringId()).getValue());

        if (field.isRemote()) {
            try {
                return new FileFieldInputStream(field, download(field.getValue().getPath()));
            } catch (IOException e) {
                log.error("Getting file failed: ", e);
                return null;
            }
        } else {
            try {
                return new FileFieldInputStream(field, new FileInputStream(field.getFilePath(useCase.getStringId())));
            } catch (FileNotFoundException e) {
                log.error("Getting file failed: ", e);
                return null;
            }
        }
    }

    @Override
    public InputStream download(String url) throws IOException {
        URL connection = new URL(url);
        return new BufferedInputStream(connection.openStream());
    }

    @Override
    public boolean saveFile(String taskId, String fieldId, MultipartFile multipartFile) {
        try {
            TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
            Task task = wrapper.getTask();
            String parsedFieldId = wrapper.getParsedFieldId();

            Case useCase = workflowService.findOne(task.getCaseId());
            FileField field = (FileField) useCase.getPetriNet().getDataSet().get(parsedFieldId);
            field.setValue((FileFieldValue) useCase.getDataField(field.getStringId()).getValue());

            if (field.isRemote()) {
                upload(useCase, field, multipartFile);
            } else {
                if (!saveLocalFile(useCase, field, multipartFile))
                    return false;
            }

            field.getActions().forEach(action -> actionsRunner.run(action, useCase));
            workflowService.save(useCase);

            return true;
        } catch (IOException e) {
            log.error("Saving file failed: ", e);
            return false;
        }
    }

    private TaskRefFieldWrapper decodeTaskRefFieldId(String taskId, String fieldId) {
        Task task;
        String parsedFieldId;
        try {
            String[] parts = decodeTaskRefFieldId(fieldId);
            task = taskService.findOne(parts[0]);
            parsedFieldId = parts[1];

        } catch (IllegalArgumentException e) {
            task = taskService.findOne(taskId);
            parsedFieldId = fieldId;
        }

        return new TaskRefFieldWrapper(task, parsedFieldId);
    }


    private String[] decodeTaskRefFieldId(String fieldId) {
        String[] split = fieldId.split("-", 2);
        if (split[0].length() == MONGO_ID_LENGTH && split.length == 2) {
            return split;
        }

        throw new IllegalArgumentException("fieldId is not referenced through taskRef");
    }


    public boolean saveLocalFile(Case useCase, FileField field, MultipartFile multipartFile) throws IOException {
        if (useCase.getDataSet().get(field.getStringId()).getValue() != null) {
            new File(field.getFilePath(useCase.getStringId())).delete();
            useCase.getDataSet().get(field.getStringId()).setValue(null);
        }

        field.setValue(multipartFile.getOriginalFilename());
        field.getValue().setPath(field.getFilePath(useCase.getStringId()));
        File file = new File(field.getFilePath(useCase.getStringId()));
        file.getParentFile().mkdirs();
        if (!file.createNewFile()) {
            file.delete();
            file.createNewFile();
        }

        FileOutputStream fout = new FileOutputStream(file);
        fout.write(multipartFile.getBytes());
        fout.close();

        useCase.getDataSet().get(field.getStringId()).setValue(field.getValue());
        return true;
    }

    public boolean upload(Case useCase, FileField field, MultipartFile multipartFile) {
        throw new UnsupportedOperationException("Upload new file to the remote storage is not implemented yet.");
    }

    @Override
    public Page<Task> setImmediateFields(Page<Task> tasks) {
        tasks.getContent().forEach(task -> task.setImmediateData(getImmediateFields(task)));
        return tasks;
    }

    @Override
    public List<Field> getImmediateFields(Task task) {
        Case useCase = workflowService.findOne(task.getCaseId());

        List<Field> fields = task.getImmediateDataFields().stream().map(id -> fieldFactory.buildFieldWithoutValidation(useCase, id)).collect(Collectors.toList());
        LongStream.range(0L, fields.size()).forEach(index -> fields.get((int) index).setOrder(index));

        return fields;
    }

    @Override
    public Map<String, ChangedField> runActions(List<Action> actions, String useCaseId, Transition transition) {
        log.info("[" + useCaseId + "]: Running actions of transition " + transition.getStringId());
        Map<String, ChangedField> changedFields = new HashMap<>();
        if (actions.isEmpty())
            return changedFields;

        Case case$ = workflowService.findOne(useCaseId);
        actions.forEach(action -> {
            Map<String, ChangedField> changedField = actionsRunner.run(action, case$);
            if (changedField.isEmpty())
                return;
            mergeChanges(changedFields, changedField);
            runActionsOnChanged(Action.ActionTrigger.SET, case$, transition, changedFields, true, changedField);
        });
        workflowService.save(case$);
        return changedFields;
    }

    private void updateDataset(Case useCase) {
        Case actual = workflowService.findOne(useCase.getStringId());
        actual.getDataSet().forEach((id, dataField) -> {
            if (dataField.isNewerThen(useCase.getDataField(id))) {
                useCase.getDataSet().put(id, dataField);
            }
        });
    }

    public Map<String, ChangedField> resolveActions(Field field, Action.ActionTrigger actionTrigger, Case useCase, Transition transition) {
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
            Map<String, ChangedField> currentChangedFields = actionsRunner.run(action, useCase);
            if (currentChangedFields.isEmpty())
                return;

            mergeChanges(changedFields, currentChangedFields);
            runActionsOnChanged(trigger, useCase, transition, changedFields, recursive, currentChangedFields);
        });
    }

    private void runActionsOnChanged(Action.ActionTrigger trigger, Case useCase, Transition transition, Map<String, ChangedField> changedFields, boolean recursive, Map<String, ChangedField> newChangedField) {
        newChangedField.forEach((s, changedField) -> {
            if ((changedField.getAttributes().containsKey("value") && changedField.getAttributes().get("value") != null) && recursive) {
                Field field = useCase.getField(s);
                processActions(field, trigger, useCase, transition, changedFields);
            }
        });
    }

    private void mergeChanges(Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedFields) {
        newChangedFields.forEach((s, changedField) -> {
            if (changedFields.containsKey(s))
                changedFields.get(s).merge(changedField);
            else
                changedFields.put(s, changedField);
        });
    }

    private Object parseFieldsValues(JsonNode jsonNode) {
        ObjectNode node = (ObjectNode) jsonNode;
        Object value;
        switch (node.get("type").asText()) {
            case "date":
                value = FieldFactory.parseDate(node.get("value").asText());
                break;
            case "dateTime":
                value = FieldFactory.parseDateTime(node.get("value").asText());
                break;
            case "boolean":
                value = node.get("value") != null && node.get("value").asBoolean();
                break;
            case "multichoice":
                ArrayNode arrayNode = (ArrayNode) node.get("value");
                HashSet<I18nString> set = new HashSet<>();
                arrayNode.forEach(item -> set.add(new I18nString(item.asText())));
                value = set;
                break;
            case "enumeration":
                String val = node.get("value").asText();
                if (val == null) {
                    value = null;
                    break;
                }
                value = new I18nString(val);
                break;
            case "user":
                if (node.get("value") == null) {
                    value = null;
                    break;
                }
                User user = userService.findById(node.get("value").asLong(), true);
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
            case "file":
                if (node.get("value") == null) {
                    value = new FileFieldValue();
                    break;
                }
                value = FileFieldValue.fromString(node.get("value").asText());
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

    @Data
    @AllArgsConstructor
    private class TaskRefFieldWrapper {
        private Task task;
        private String parsedFieldId;
    }
}