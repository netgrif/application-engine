package com.netgrif.workflow.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.usecase.SaveCaseDataEvent;
import com.netgrif.workflow.importer.service.FieldFactory;
import com.netgrif.workflow.petrinet.domain.*;
import com.netgrif.workflow.petrinet.domain.Component;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldByFileFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.responsebodies.DataFieldsResource;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
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

    @Value("${nae.image.preview.scaling.px:400}")
    private int imageScale;

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

            resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(), Action.ActionTrigger.GET, EventPhase.PRE, useCase, transition);

            if (useCase.hasFieldBehavior(fieldId, transition.getStringId())) {
                if (useCase.getDataSet().get(fieldId).isDisplayable(transition.getStringId())) {
                    Field field = fieldFactory.buildFieldWithValidation(useCase, fieldId);
                    field.setBehavior(useCase.getDataSet().get(fieldId).applyBehavior(transition.getStringId()));
                    if (transition.getDataSet().get(fieldId).layoutExist() && transition.getDataSet().get(fieldId).getLayout().layoutFilled()) {
                        field.setLayout(transition.getDataSet().get(fieldId).getLayout().clone());
                    }
                    resolveComponents(field, transition);
                    dataSetFields.add(field);
                }
            } else {
                if (transition.getDataSet().get(fieldId).isDisplayable()) {
                    Field field = fieldFactory.buildFieldWithValidation(useCase, fieldId);
                    field.setBehavior(transition.getDataSet().get(fieldId).applyBehavior());
                    if (transition.getDataSet().get(fieldId).layoutExist() && transition.getDataSet().get(fieldId).getLayout().layoutFilled()) {
                        field.setLayout(transition.getDataSet().get(fieldId).getLayout().clone());
                    }
                    resolveComponents(field, transition);
                    dataSetFields.add(field);
                }
            }
            resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(), Action.ActionTrigger.GET, EventPhase.POST, useCase, transition);
        });

        workflowService.save(useCase);

        dataSetFields.stream().filter(field -> field instanceof NumberField).forEach(field -> {
            DataField dataField = useCase.getDataSet().get(field.getImportId());
            if (dataField.getVersion().equals(0L) && dataField.getValue().equals(0.0)) {
                field.setValue(null);
            }
        });

        LongStream.range(0L, dataSetFields.size())
                .forEach(index -> dataSetFields.get((int) index).setOrder(index));

        return dataSetFields;
    }

    private void resolveComponents(Field field, Transition transition){
        Component transitionComponent = transition.getDataSet().get(field.getImportId()).getComponent();
        if(transitionComponent != null)
            field.setComponent(transitionComponent);
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
                Map<String, ChangedField> changedFieldMap = resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(),
                        Action.ActionTrigger.SET, EventPhase.PRE, useCase, useCase.getPetriNet().getTransition(task.getTransitionId()));

                dataField.setValue(parseFieldsValues(entry.getValue(), dataField));

                changedFieldMap.putAll(resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(),
                        Action.ActionTrigger.SET, EventPhase.POST, useCase, useCase.getPetriNet().getTransition(task.getTransitionId())));
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
                resolveTaskRefBehavior(taskRefField, taskRefDataGroups);
                groups.addAll(taskRefDataGroups);
            });
        }

        return groups;
    }

    private void resolveTaskRefBehavior(TaskField taskRefField, List<DataGroup> taskRefDataGroups){
        if(taskRefField.getBehavior().has("visible") && taskRefField.getBehavior().get("visible").asBoolean()){
            taskRefDataGroups.forEach(dataGroup -> {
                dataGroup.getFields().getContent().forEach(field -> {
                    if(field.getBehavior().has("editable") && field.getBehavior().get("editable").asBoolean()){
                        changeTaskRefBehavior(field, FieldBehavior.VISIBLE);
                    }
                });
            });
        } else if (taskRefField.getBehavior().has("hidden") && taskRefField.getBehavior().get("hidden").asBoolean()){
            taskRefDataGroups.forEach(dataGroup -> {
                dataGroup.getFields().getContent().forEach(field -> {
                    if(!field.getBehavior().has("forbidden") || !field.getBehavior().get("forbidden").asBoolean())
                        changeTaskRefBehavior(field, FieldBehavior.HIDDEN);
                });
            });
        }
    }

    private void changeTaskRefBehavior(LocalisedField field, FieldBehavior behavior){
        List<FieldBehavior> antonymBehaviors = Arrays.asList(behavior.getAntonyms());
        antonymBehaviors.forEach(beh -> field.getBehavior().remove(beh.name()));
        ObjectNode behaviorNode = JsonNodeFactory.instance.objectNode();
        behaviorNode.put(behavior.toString(), true);
        field.setBehavior(behaviorNode);
    }

    @Override
    public FileFieldInputStream getFileByTask(String taskId, String fieldId, boolean forPreview) {
        TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
        Task task = wrapper.getTask();
        String parsedFieldId = wrapper.getFieldId();

        return getFileByCase(task.getCaseId(), parsedFieldId, forPreview);
    }

    @Override
    public FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name) {
        TaskRefFieldWrapper wrapper = decodeTaskRefFieldId(taskId, fieldId);
        Task task = wrapper.getTask();
        String parsedFieldId = wrapper.getFieldId();

        return getFileByCaseAndName(task.getCaseId(), parsedFieldId, name);
    }

    @Override
    public FileFieldInputStream getFileByCase(String caseId, String fieldId, boolean forPreview) {
        Case useCase = workflowService.findOne(caseId);
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFile(useCase, field, forPreview);
    }

    @Override
    public FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name) {
        Case useCase = workflowService.findOne(caseId);
        FileListField field = (FileListField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFileByName(useCase, field, name);
    }

    @Override
    public FileFieldInputStream getFileByName(Case useCase, FileListField field, String name) {
        field.getEvents().forEach(dataEvent -> {
            dataEvent.getActions().get(EventPhase.PRE).forEach(action -> actionsRunner.run(action, useCase));
            dataEvent.getActions().get(EventPhase.POST).forEach(action -> actionsRunner.run(action, useCase));
        });
        if (useCase.getDataSet().get(field.getStringId()).getValue() == null)
            return null;

        workflowService.save(useCase);
        field.setValue((FileListFieldValue) useCase.getFieldValue(field.getStringId()));

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
    public FileFieldInputStream getFile(Case useCase, FileField field, boolean forPreview) {
        field.getEvents().forEach(dataEvent -> {
            dataEvent.getActions().get(EventPhase.PRE).forEach(action -> actionsRunner.run(action, useCase));
            dataEvent.getActions().get(EventPhase.POST).forEach(action -> actionsRunner.run(action, useCase));
        });
        if (useCase.getFieldValue(field.getStringId()) == null)
            return null;

        workflowService.save(useCase);
        field.setValue((FileFieldValue) useCase.getFieldValue(field.getStringId()));

        try {
            if (forPreview) {
                return getFilePreview(field, useCase);
            } else {
                return new FileFieldInputStream(field, field.isRemote() ? download(field.getValue().getPath()) :
                        new FileInputStream(field.getValue().getPath()));
            }
        } catch (IOException e) {
            log.error("Getting file failed: ", e);
            return null;
        }
    }

    private FileFieldInputStream getFilePreview(FileField field, Case useCase) throws IOException {
        File localPreview = new File(field.getFilePreviewPath(useCase.getStringId()));
        if (localPreview.exists()) {
            return new FileFieldInputStream(field, new FileInputStream(localPreview));
        }
        File file;
        if (field.isRemote()) {
            file = getRemoteFile(field);
        } else {
            file = new File(field.getValue().getPath());
        }
        int dot = file.getName().lastIndexOf(".");
        FileFieldDataType fileType = FileFieldDataType.resolveType((dot == -1) ? "" : file.getName().substring(dot + 1));
        BufferedImage image = getBufferedImageFromFile(file, fileType);
        if (image.getWidth() > imageScale || image.getHeight() > imageScale) {
            image = scaleImagePreview(image);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, !fileType.extension.equals(FileFieldDataType.PDF.extension) ? fileType.extension : FileFieldDataType.JPG.extension, os);
        saveFilePreview(localPreview, os);
        return new FileFieldInputStream(field, new ByteArrayInputStream(os.toByteArray()));
    }

    private void saveFilePreview(File localPreview, ByteArrayOutputStream os) throws IOException {
        localPreview.getParentFile().mkdirs();
        localPreview.createNewFile();
        FileOutputStream fos = new FileOutputStream(localPreview);
        fos.write(os.toByteArray());
        fos.close();
    }

    private BufferedImage getBufferedImageFromFile(File file, FileFieldDataType fileType) throws IOException {
        BufferedImage image;
        if (fileType.equals(FileFieldDataType.PDF)) {
            PDDocument document = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(document);
            image = renderer.renderImage(0);
        } else {
            image = ImageIO.read(file);

        }
        return image;
    }

    private BufferedImage scaleImagePreview(BufferedImage image) {
        float ratio = image.getHeight() > image.getWidth() ? image.getHeight() / (float) imageScale : image.getWidth() / (float) imageScale;
        int targetWidth = Math.round(image.getWidth() / ratio);
        int targetHeight = Math.round(image.getHeight() / ratio);
        Image targetImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        image = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawImage(targetImage, 0, 0, null);
        return image;
    }

    private File getRemoteFile(FileField field) throws IOException {
        File file;
        InputStream is = download(field.getValue().getPath());
        file = File.createTempFile(field.getStringId(), "pdf");
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(is, fos);
        return file;
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

        Map<String, ChangedField> changedFields = resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(),Action.ActionTrigger.SET,
                EventPhase.PRE, useCase, useCase.getPetriNet().getTransition(task.getTransitionId()));
        changedFields.putAll(resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(),Action.ActionTrigger.SET,
                EventPhase.POST, useCase, useCase.getPetriNet().getTransition(task.getTransitionId())));

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
                new File(field.getFilePreviewPath(useCase.getStringId())).delete();
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
            runEventActionsOnChanged(case$, transition, changedFields, changedField, Action.ActionTrigger.SET,true);
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

    private Map<String, ChangedField> resolveDataEvents(Field field, Action.ActionTrigger trigger, EventPhase phase, Case useCase, Transition transition) {
        Map<String, ChangedField> changedFields = new HashMap<>();
        processDataEvents(field, trigger, phase, useCase, changedFields, transition);
        return changedFields;
    }

    private void processDataEvents(Field field, Action.ActionTrigger actionTrigger, EventPhase phase, Case useCase, Map<String, ChangedField> changedFields, Transition transition){
        LinkedList<Action> fieldActions = new LinkedList<>();
        if (field.getEvents() != null){
            fieldActions.addAll(DataFieldLogic.getEventAction(field.getEvents(), actionTrigger, phase));
        }
        if (transition.getDataSet().containsKey(field.getStringId()) && !transition.getDataSet().get(field.getStringId()).getEvents().isEmpty())
            fieldActions.addAll(DataFieldLogic.getEventAction(transition.getDataSet().get(field.getStringId()).getEvents(), actionTrigger, phase));

        if (fieldActions.isEmpty()) return;

        runEventActions(useCase, transition, fieldActions, changedFields, actionTrigger);
    }

    private void runEventActions(Case useCase, Transition transition, List<Action> actions, Map<String, ChangedField> changedFields, Action.ActionTrigger trigger){
        actions.forEach(action -> {
            Map<String, ChangedField> currentChangedFields = actionsRunner.run(action, useCase);
            if (currentChangedFields.isEmpty())
                return;

            mergeChanges(changedFields, currentChangedFields);
            runEventActionsOnChanged(useCase, transition, changedFields, currentChangedFields, trigger,trigger == Action.ActionTrigger.SET);
        });
    }

    private void runEventActionsOnChanged(Case useCase, Transition transition, Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedField, Action.ActionTrigger trigger, boolean recursive) {
        newChangedField.forEach((s, changedField) -> {
            if ((changedField.getAttributes().containsKey("value") && changedField.getAttributes().get("value") != null) && recursive) {
                Field field = useCase.getField(s);
                processDataEvents(field, trigger, EventPhase.PRE, useCase, changedFields, transition);
                processDataEvents(field, trigger, EventPhase.POST, useCase, changedFields, transition);
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
                if (node.get("value") == null || node.get("value").isNull()) {
                    value = null;
                    break;
                }
                value = FieldFactory.parseDate(node.get("value").asText());
                break;
            case "dateTime":
                if (node.get("value") == null || node.get("value").isNull()) {
                    value = null;
                    break;
                }
                value = FieldFactory.parseDateTime(node.get("value").asText());
                break;
            case "boolean":
                value = !(node.get("value") == null || node.get("value").isNull()) && node.get("value").asBoolean();
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
                if (node.get("value") == null || node.get("value").isNull()) {
                    value = null;
                    break;
                }
                User user = new User(userService.findById(node.get("value").asLong(), true));
                user.setPassword(null);
                user.setGroups(null);
                user.setAuthorities(null);
                user.setUserProcessRoles(null);
                value = user;
                break;
            case "number":
                if (node.get("value") == null || node.get("value").isNull()) {
                    value = 0.0;
                    break;
                }
                value = node.get("value").asDouble();
                break;
            case "file":
                if (node.get("value") == null || node.get("value").isNull()) {
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
                if (node.get("value") == null || node.get("value").isNull()) {
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