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
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldByFileFieldContainer;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

@Slf4j
@Service
public class DataService implements IDataService {

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
                    if (transition.getDataSet().get(fieldId).layoutExist() && transition.getDataSet().get(fieldId).getLayout().layoutFilled()) {
                        field.setLayout(transition.getDataSet().get(fieldId).getLayout().clone());
                    }
                    dataSetFields.add(field);
                }
            } else {
                if (transition.getDataSet().get(fieldId).isDisplayable()) {
                    Field field = fieldFactory.buildFieldWithValidation(useCase, fieldId);
                    field.setBehavior(transition.getDataSet().get(fieldId).applyBehavior());
                    if (transition.getDataSet().get(fieldId).layoutExist() && transition.getDataSet().get(fieldId).getLayout().layoutFilled()) {
                        field.setLayout(transition.getDataSet().get(fieldId).getLayout().clone());
                    }
                    dataSetFields.add(field);
                }
            }
        });

        workflowService.save(useCase);

        for (Field field : dataSetFields) {
            if (!(field instanceof NumberField))
                continue;
            DataField dataField = useCase.getDataSet().get(field.getImportId());
            if (dataField.getVersion().equals(0L) && dataField.getValue().equals(0.0)) {
                field.setValue(null);
            }
        }

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
        return setData(task, values);
    }

    private ChangedFieldContainer setData(Task task, ObjectNode values) {
        Case useCase = workflowService.findOne(task.getCaseId());
        ChangedFieldContainer container = new ChangedFieldContainer();

        log.info("[" + useCase.getStringId() + "]: Setting data of task " + task.getTransitionId() + " [" + task.getStringId() + "]");

        Map<String, ChangedField> changedFields = new HashMap<>();
        values.fields().forEachRemaining(entry -> {
            DataField dataField = useCase.getDataSet().get(entry.getKey());
            String fieldId = entry.getKey();
            if (entry.getKey().startsWith(task.getStringId())) {
                fieldId = fieldId.replace(task.getStringId() + "-", "");
                dataField = useCase.getDataField(fieldId);
            }
            if (dataField != null) {
                dataField.setValue(parseFieldsValues(entry.getValue(), dataField));
                Map<String, ChangedField> changedFieldMap = resolveActions(useCase.getPetriNet().getField(fieldId).get(),
                        Action.ActionTrigger.SET, useCase, useCase.getPetriNet().getTransition(task.getTransitionId()));
                mergeChanges(changedFields, changedFieldMap);
            } else try {
                if (entry.getKey().contains("-")) {
                    TaskRefFieldWrapper decoded = decodeTaskRefFieldId(entry.getKey());
                    Task referencedTask = taskService.findOne(decoded.getTaskId());
                    ChangedFieldContainer changedFieldContainer = setData(referencedTask, values);
                    for (Map.Entry<String, Map<String, Object>> stringMapEntry : changedFieldContainer.getChangedFields().entrySet()) {
                        Map<String, Object> entryValue = substituteTaskRefFieldBehavior(stringMapEntry.getValue(), referencedTask, task.getTransitionId());
                        container.getChangedFields().put(decoded.getTaskId() + "-" + stringMapEntry.getKey(), entryValue);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to set taskRef references fields", e);
            }
        });
        updateDataset(useCase);
        workflowService.save(useCase);
        publisher.publishEvent(new SaveCaseDataEvent(useCase, values, changedFields.values()));

        container.putAll(changedFields);
        return container;
    }

    private Map<String, Object> substituteTaskRefFieldBehavior(Map<String, Object> change, Task referencedTask, String refereeTransId) {
        if (change.containsKey("behavior")) {
            Map<String, Object> newBehavior = new HashMap<>();
            ((Map<String, Object>) change.get("behavior")).forEach((taskId, behavior) -> {
                String behaviorChangedOnTrans = taskId.equals(referencedTask.getTransitionId()) ?
                        refereeTransId : referencedTask.getStringId() + "-" + taskId;
                newBehavior.put(behaviorChangedOnTrans, behavior);
            });
            change.put("behavior", newBehavior);
        }
        return change;
    }

    private void substituteTaskRefFieldBehavior(ChangedField change, Task referencedTask, String refereeTransId) {
        substituteTaskRefFieldBehavior(change.getAttributes(), referencedTask, refereeTransId);
    }

    @Override
    public List<DataGroup> getDataGroups(String taskId, Locale locale) {
        return getDataGroups(taskId, locale, new HashSet<>(), 0);
    }

    private List<DataGroup> getDataGroups(String taskId, Locale locale, Set<String> collectedTaskIds, int level) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        PetriNet net = useCase.getPetriNet();
        Transition transition = net.getTransition(task.getTransitionId());

        log.info("Getting groups of task " + taskId + " in case " + useCase.getTitle() + " level: " + level);
        List<DataGroup> resultDataGroups = new ArrayList<>();

        List<Field> data = getData(taskId);
        Map<String, Field> dataFieldMap = data.stream().collect(Collectors.toMap(Field::getImportId, field -> field));
        List<DataGroup> dataGroups = transition.getDataGroups().values().stream().map(DataGroup::clone).collect(Collectors.toList());
        for (DataGroup dataGroup : dataGroups) {
            resultDataGroups.add(dataGroup);
            log.debug("Setting groups of task " + taskId + " in case " + useCase.getTitle() + " level: " + level + " " + dataGroup.getImportId());
            if (level != 0) dataGroup.setImportId(taskId + "-" + dataGroup.getStringId());

            List<Field> resources = new LinkedList<>();
            for (String dataFieldId : dataGroup.getData()) {
                Field field = net.getDataSet().get(dataFieldId);
                if (dataFieldMap.containsKey(dataFieldId)) {
                    if (field.getType() == FieldType.TASK_REF) {
                        resultDataGroups.addAll(collectTaskRefDataGroups((TaskField) dataFieldMap.get(dataFieldId), locale, collectedTaskIds, level));
                    } else {
                        Field resource = dataFieldMap.get(dataFieldId);
                        if (level != 0) resource.setImportId(taskId + "-" + resource.getImportId());
                        resources.add(resource);
                    }
                }
            }
            dataGroup.setFields(new DataFieldsResource(resources, locale));
        }

        return resultDataGroups;
    }

    private List<DataGroup> collectTaskRefDataGroups(TaskField taskRefField, Locale locale, Set<String> collectedTaskIds, int level) {
        List<String> taskIds = taskRefField.getValue();
        List<DataGroup> groups = new ArrayList<>();

        if (taskIds != null) {
            taskIds = taskIds.stream().filter(id -> !collectedTaskIds.contains(id)).collect(Collectors.toList());
            taskIds.forEach(id -> {
                collectedTaskIds.add(id);
                List<DataGroup> taskRefDataGroups = getDataGroups(id, locale, collectedTaskIds, level + 1);
                groups.addAll(taskRefDataGroups);
            });
        }

        return groups;
    }

    @Override
    public FileFieldInputStream getFileByTask(String taskId, String fieldId) {
        TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
        Task task = wrapper.getTask();
        String parsedFieldId = wrapper.getFieldId();

        return getFileByCase(task.getCaseId(), parsedFieldId);
    }

    @Override
    public FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name) {
        TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
        Task task = wrapper.getTask();
        String parsedFieldId = wrapper.getFieldId();

        return getFileByCaseAndName(task.getCaseId(), parsedFieldId, name);
    }

    @Override
    public FileFieldInputStream getFileByCase(String caseId, String fieldId) {
        Case useCase = workflowService.findOne(caseId);
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFile(useCase, field);
    }

    @Override
    public FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name) {
        Case useCase = workflowService.findOne(caseId);
        FileListField field = (FileListField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFileByName(useCase, field, name);
    }

    @Override
    public FileFieldInputStream getFileByName(Case useCase, FileListField field, String name) {
        field.getActions().forEach(action -> actionsRunner.run(action, useCase));
        if (useCase.getDataSet().get(field.getStringId()).getValue() == null)
            return null;

        workflowService.save(useCase);
        field.setValue((FileListFieldValue) useCase.getDataSet().get(field.getStringId()).getValue());

        Optional<FileFieldValue> fileField = field.getValue().getNamesPaths().stream().filter(namePath -> namePath.getName().equals(name)).findFirst();
        if (!fileField.isPresent() || fileField.get().getPath() == null) {
            log.error("File " + name + " not found!");
            return null;
        }

        try {
            return new FileFieldInputStream(field.isRemote() ? download(fileField.get().getPath()) :
                    new FileInputStream(fileField.get().getPath()), name);
        } catch (IOException e) {
            log.error("Getting file failed: ", e);
            return null;
        }
    }

    @Override
    public FileFieldInputStream getFile(Case useCase, FileField field) {
        field.getActions().forEach(action -> actionsRunner.run(action, useCase));
        if (useCase.getDataSet().get(field.getStringId()).getValue() == null)
            return null;

        workflowService.save(useCase);
        field.setValue((FileFieldValue) useCase.getDataSet().get(field.getStringId()).getValue());

        try {
            return new FileFieldInputStream(field, field.isRemote() ? download(field.getValue().getPath()) :
                    new FileInputStream(field.getValue().getPath()));
        } catch (IOException e) {
            log.error("Getting file failed: ", e);
            return null;
        }
    }

    @Override
    public InputStream download(String url) throws IOException {
        URL connection = new URL(url);
        return new BufferedInputStream(connection.openStream());
    }

    @Override
    public ChangedFieldByFileFieldContainer saveFile(String taskId, String fieldId, MultipartFile multipartFile) {
        try {
            TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
            Task task = wrapper.getTask();

            ImmutablePair<Case, FileField> pair = getCaseAndFileField(taskId, fieldId);
            FileField field = pair.getRight();
            Case useCase = pair.getLeft();

            ChangedFieldByFileFieldContainer container = new ChangedFieldByFileFieldContainer(false);

            if (field.isRemote()) {
                upload(useCase, field, multipartFile);
            } else {
                if (!saveLocalFile(useCase, field, multipartFile))
                    return container;
            }

            return getChangedFieldByFileFieldContainer(fieldId, task, useCase, container);
        } catch (IOException e) {
            log.error("Saving file failed: ", e);
            return new ChangedFieldByFileFieldContainer(false);
        }
    }

    @Override
    public ChangedFieldByFileFieldContainer saveFiles(String taskId, String fieldId, MultipartFile[] multipartFiles) {
        try {
            TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
            Task task = wrapper.getTask();

            ImmutablePair<Case, FileListField> pair = getCaseAndFileListField(taskId, fieldId);
            FileListField field = pair.getRight();
            Case useCase = pair.getLeft();

            ChangedFieldByFileFieldContainer container = new ChangedFieldByFileFieldContainer(false);

            if (field.isRemote()) {
                upload(useCase, field, multipartFiles);
            } else {
                if (!saveLocalFiles(useCase, field, multipartFiles))
                    return container;
            }

            return getChangedFieldByFileFieldContainer(fieldId, task, useCase, container);
        } catch (IOException e) {
            log.error("Saving files failed: ", e);
            return new ChangedFieldByFileFieldContainer(false);
        }
    }

    private ChangedFieldByFileFieldContainer getChangedFieldByFileFieldContainer(String fieldId, Task task, Case useCase,
                                                                                 ChangedFieldByFileFieldContainer container) {
        TaskRefFieldWrapper decodedTaskRef = null;
        try {
            decodedTaskRef = decodeTaskRefFieldId(fieldId);
            fieldId = decodedTaskRef.getFieldId();
        } catch (IllegalArgumentException e) {
            log.debug("fieldId is not referenced through taskRef", e);
        }

        Map<String, ChangedField> changedFields = resolveActions(useCase.getPetriNet().getField(fieldId).get(),
                Action.ActionTrigger.SET, useCase, useCase.getPetriNet().getTransition(task.getTransitionId()));

        if (decodedTaskRef != null) {
            Task referencedTask = taskService.findOne(decodedTaskRef.getTaskId());
            Map<String, ChangedField> substitutedChangedFields = new HashMap<>();
            for (Map.Entry<String, ChangedField> changedFieldEntry : changedFields.entrySet()) {
                ChangedField change = changedFieldEntry.getValue();
                substituteTaskRefFieldBehavior(change, referencedTask, task.getTransitionId());
                change.setId(decodedTaskRef.getTaskId() + "-" + changedFieldEntry.getKey());
                substitutedChangedFields.put(change.getId(), change);
            }
            changedFields = substitutedChangedFields;
        }

        container.putAll(changedFields);
        container.setIsSave(true);
        updateDataset(useCase);
        workflowService.save(useCase);
        return container;
    }

    private TaskRefFieldWrapper decodeTaskRefFieldId(String taskId, String fieldId) {
        try {
            TaskRefFieldWrapper decoded = decodeTaskRefFieldId(fieldId);
            Task task = taskService.findOne(decoded.getTaskId());
            decoded.setTask(task);
            return decoded;
        } catch (IllegalArgumentException e) {
            Task task = taskService.findOne(taskId);
            return new TaskRefFieldWrapper(task, task.getStringId(), fieldId);
        }
    }

    private TaskRefFieldWrapper decodeTaskRefFieldId(String fieldId) throws IllegalArgumentException {
        String[] split = fieldId.split("-", 2);
        if (split[0].length() == MONGO_ID_LENGTH && split.length == 2) {
            return new TaskRefFieldWrapper(null, split[0], split[1]);
        }

        throw new IllegalArgumentException("fieldId is not referenced through taskRef");
    }

    private boolean saveLocalFiles(Case useCase, FileListField field, MultipartFile[] multipartFiles) throws IOException {
        for (MultipartFile oneFile : multipartFiles) {
            if (field.getValue() != null && field.getValue().getNamesPaths() != null) {
                Optional<FileFieldValue> fileField = field.getValue().getNamesPaths().stream().filter(namePath -> namePath.getName().equals(oneFile.getOriginalFilename())).findFirst();
                if (fileField.isPresent()) {
                    new File(field.getFilePath(useCase.getStringId(), oneFile.getOriginalFilename())).delete();
                    field.getValue().getNamesPaths().remove(fileField.get());
                }
            }

            field.addValue(oneFile.getOriginalFilename(), field.getFilePath(useCase.getStringId(), oneFile.getOriginalFilename()));
            File file = new File(field.getFilePath(useCase.getStringId(), oneFile.getOriginalFilename()));

            writeFile(oneFile, file);
        }
        useCase.getDataSet().get(field.getStringId()).setValue(field.getValue());
        return true;
    }

    private boolean saveLocalFile(Case useCase, FileField field, MultipartFile multipartFile) throws IOException {
        if (useCase.getDataSet().get(field.getStringId()).getValue() != null) {
            new File(field.getFilePath(useCase.getStringId())).delete();
            useCase.getDataSet().get(field.getStringId()).setValue(null);
        }

        field.setValue(multipartFile.getOriginalFilename());
        field.getValue().setPath(field.getFilePath(useCase.getStringId()));
        File file = new File(field.getFilePath(useCase.getStringId()));
        writeFile(multipartFile, file);

        useCase.getDataSet().get(field.getStringId()).setValue(field.getValue());
        return true;
    }

    private void writeFile(MultipartFile multipartFile, File file) throws IOException {
        file.getParentFile().mkdirs();
        if (!file.createNewFile()) {
            file.delete();
            file.createNewFile();
        }

        FileOutputStream fout = new FileOutputStream(file);
        fout.write(multipartFile.getBytes());
        fout.close();
    }

    private boolean upload(Case useCase, FileField field, MultipartFile multipartFile) {
        throw new UnsupportedOperationException("Upload new file to the remote storage is not implemented yet.");
    }

    private boolean upload(Case useCase, FileListField field, MultipartFile[] multipartFiles) {
        throw new UnsupportedOperationException("Upload new files to the remote storage is not implemented yet.");
    }

    private boolean deleteRemote(Case useCase, FileField field) {
        throw new UnsupportedOperationException("Delete file from the remote storage is not implemented yet.");
    }

    private boolean deleteRemote(Case useCase, FileListField field, String name) {
        throw new UnsupportedOperationException("Delete file from the remote storage is not implemented yet.");
    }

    @Override
    public boolean deleteFile(String taskId, String fieldId) {
        ImmutablePair<Case, FileField> pair = getCaseAndFileField(taskId, fieldId);
        FileField field = pair.getRight();
        Case useCase = pair.getLeft();

        if (useCase.getDataSet().get(field.getStringId()).getValue() != null) {
            if (field.isRemote()) {
                deleteRemote(useCase, field);
            } else {
                new File(field.getValue().getPath()).delete();
            }
            useCase.getDataSet().get(field.getStringId()).setValue(null);
        }

        updateDataset(useCase);
        workflowService.save(useCase);
        return true;
    }

    private ImmutablePair<Case, FileField> getCaseAndFileField(String taskId, String fieldId) {
        TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
        Task task = wrapper.getTask();
        String parsedFieldId = wrapper.getFieldId();

        Case useCase = workflowService.findOne(task.getCaseId());
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(parsedFieldId);
        field.setValue((FileFieldValue) useCase.getDataField(field.getStringId()).getValue());

        return new ImmutablePair<>(useCase, field);
    }

    @Override
    public boolean deleteFileByName(String taskId, String fieldId, String name) {
        ImmutablePair<Case, FileListField> pair = getCaseAndFileListField(taskId, fieldId);
        FileListField field = pair.getRight();
        Case useCase = pair.getLeft();

        Optional<FileFieldValue> fileField = field.getValue().getNamesPaths().stream().filter(namePath -> namePath.getName().equals(name)).findFirst();

        if (fileField.isPresent()) {
            if (field.isRemote()) {
                deleteRemote(useCase, field, name);
            } else {
                new File(fileField.get().getPath()).delete();
                field.getValue().getNamesPaths().remove(fileField.get());
            }
            useCase.getDataSet().get(field.getStringId()).setValue(field.getValue());
        }

        updateDataset(useCase);
        workflowService.save(useCase);
        return true;
    }

    private ImmutablePair<Case, FileListField> getCaseAndFileListField(String taskId, String fieldId) {
        TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
        Task task = wrapper.getTask();
        String parsedFieldId = wrapper.getFieldId();

        Case useCase = workflowService.findOne(task.getCaseId());
        FileListField field = (FileListField) useCase.getPetriNet().getDataSet().get(parsedFieldId);
        field.setValue((FileListFieldValue) useCase.getDataField(field.getStringId()).getValue());
        return new ImmutablePair<>(useCase, field);
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

    private Object parseFieldsValues(JsonNode jsonNode, DataField dataField) {
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
                value = !node.get("value").isNull() && node.get("value").asBoolean();
                break;
            case "multichoice":
                value = parseMultichoiceFieldValues(node).stream().map(I18nString::new).collect(Collectors.toSet());
                break;
            case "multichoice_map":
                value = parseMultichoiceFieldValues(node);
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
                if (node.get("value").isNull()) {
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
                if (node.get("value").isNull()) {
                    value = 0.0;
                    break;
                }
                value = node.get("value").asDouble();
                break;
            case "file":
                if (node.get("value").isNull()) {
                    value = new FileFieldValue();
                    break;
                }
                value = FileFieldValue.fromString(node.get("value").asText());
                break;
            case "caseRef":
                List<String> list = parseListStringValues(node);
                validateCaseRefValue(list, dataField.getAllowedNets());
                value = list;
                break;
            case "taskRef":
                value = parseListStringValues(node);
                // TODO 29.9.2020: validate task ref value? is such feature desired?
                break;
            default:
                if (node.get("value").isNull()) {
                    value = null;
                    break;
                }
                value = node.get("value").asText();
                break;
        }
        if (value instanceof String && ((String) value).equalsIgnoreCase("null")) return null;
        else return value;
    }

    private Set<String> parseMultichoiceFieldValues(ObjectNode node) {
        ArrayNode arrayNode = (ArrayNode) node.get("value");
        HashSet<String> set = new HashSet<>();
        arrayNode.forEach(item -> set.add(item.asText()));
        return set;
    }

    private List<String> parseListStringValues(ObjectNode node) {
        ArrayNode arrayNode = (ArrayNode) node.get("value");
        ArrayList<String> list = new ArrayList<>();
        arrayNode.forEach(string -> list.add(string.asText()));
        return list;
    }

    public void validateCaseRefValue(List<String> value, List<String> allowedNets) throws IllegalArgumentException {
        List<Case> cases = workflowService.findAllById(value);
        Set<String> nets = new HashSet<>(allowedNets);
        cases.forEach(_case -> {
            if (!nets.contains(_case.getProcessIdentifier())) {
                throw new IllegalArgumentException(String.format("Case '%s' with id '%s' cannot be added to case ref, since it is an instance of process with identifier '%s', which is not one of the allowed nets", _case.getTitle(), _case.getStringId(), _case.getProcessIdentifier()));
            }
        });
    }

    @Data
    @AllArgsConstructor
    private class TaskRefFieldWrapper {
        private Task task;
        private String taskId;
        private String fieldId;
    }
}