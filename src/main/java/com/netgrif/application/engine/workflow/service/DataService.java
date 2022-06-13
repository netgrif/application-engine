package com.netgrif.application.engine.workflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.history.domain.dataevents.GetDataEventLog;
import com.netgrif.application.engine.history.domain.dataevents.SetDataEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.*;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEventType;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.EventNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.DataFieldsResource;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
public class DataService implements IDataService {

    public static final int MONGO_ID_LENGTH = 24;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected ITaskService taskService;

    @Autowired
    protected IWorkflowService workflowService;

    @Autowired
    protected IUserService userService;

    @Autowired
    protected FieldFactory fieldFactory;

    @Autowired
    protected FieldActionsRunner actionsRunner;

    @Autowired
    protected IEventService eventService;

    @Autowired
    protected IHistoryService historyService;

    @Value("${nae.image.preview.scaling.px:400}")
    protected int imageScale;

    @Override
    public GetDataEventOutcome getData(String taskId) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());

        return getData(task, useCase);
    }

    @Override
    public GetDataEventOutcome getData(Task task, Case useCase) {
        log.info("[" + useCase.getStringId() + "]: Getting data of task " + task.getTransitionId() + " [" + task.getStringId() + "]");
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());

        Set<String> fieldsIds = transition.getDataSet().keySet();
        List<Field> dataSetFields = new ArrayList<>();
        if (task.getUserId() != null) {
            task.setUser(userService.findById(task.getUserId(), false));
        }
        GetDataEventOutcome outcome = new GetDataEventOutcome(useCase, task);
        fieldsIds.forEach(fieldId -> {
            if (isForbidden(fieldId, transition, useCase.getDataField(fieldId)))
                return;
            Field field = useCase.getPetriNet().getField(fieldId).get();
            outcome.addOutcomes(resolveDataEvents(field, DataEventType.GET, EventPhase.PRE, useCase, task));
            historyService.save(new GetDataEventLog(task, useCase, EventPhase.PRE));

            if (outcome.getMessage() == null) {
                Map<String, DataFieldLogic> dataSet = useCase.getPetriNet().getTransition(task.getTransitionId()).getDataSet();
                if (field.getEvents().containsKey(DataEventType.GET)
                        && ((DataEvent) field.getEvents().get(DataEventType.GET)).getMessage() != null) {
                    outcome.setMessage(((DataEvent) field.getEvents().get(DataEventType.GET)).getMessage());
                } else if (dataSet.containsKey(fieldId)
                        && dataSet.get(fieldId).getEvents().containsKey(DataEventType.GET)
                        && dataSet.get(fieldId).getEvents().get(DataEventType.GET).getMessage() != null) {
                    outcome.setMessage(useCase.getPetriNet().getTransition(task.getTransitionId()).getDataSet().get(fieldId).getEvents().get(DataEventType.GET).getMessage());
                }
            }
            if (useCase.hasFieldBehavior(fieldId, transition.getStringId())) {
                if (useCase.getDataSet().get(fieldId).isDisplayable(transition.getStringId())) {
                    Field validationField = fieldFactory.buildFieldWithValidation(useCase, fieldId, transition.getStringId());
                    validationField.setBehavior(useCase.getDataSet().get(fieldId).applyBehavior(transition.getStringId()));
                    if (transition.getDataSet().get(fieldId).layoutExist() && transition.getDataSet().get(fieldId).getLayout().layoutFilled()) {
                        validationField.setLayout(transition.getDataSet().get(fieldId).getLayout().clone());
                    }
                    resolveComponents(validationField, transition);
                    dataSetFields.add(validationField);
                }
            } else {
                if (transition.getDataSet().get(fieldId).isDisplayable()) {
                    Field validationField = fieldFactory.buildFieldWithValidation(useCase, fieldId, transition.getStringId());
                    validationField.setBehavior(transition.getDataSet().get(fieldId).applyBehavior());
                    if (transition.getDataSet().get(fieldId).layoutExist() && transition.getDataSet().get(fieldId).getLayout().layoutFilled()) {
                        validationField.setLayout(transition.getDataSet().get(fieldId).getLayout().clone());
                    }
                    resolveComponents(validationField, transition);
                    dataSetFields.add(validationField);
                }
            }
            outcome.addOutcomes(resolveDataEvents(field, DataEventType.GET, EventPhase.POST, useCase, task));
            historyService.save(new GetDataEventLog(task, useCase, EventPhase.POST));
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
        outcome.setData(dataSetFields);
        return outcome;
    }

    private void resolveComponents(Field field, Transition transition) {
        Component transitionComponent = transition.getDataSet().get(field.getImportId()).getComponent();
        if (transitionComponent != null)
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
    public SetDataEventOutcome setData(String taskId, DataSet dataSet) {
        Task task = taskService.findOne(taskId);
        return setData(task, dataSet);
    }

    @Override
    public SetDataEventOutcome setData(Task task, DataSet dataSet) {
        Case useCase = workflowService.findOne(task.getCaseId());

        log.info("[" + useCase.getStringId() + "]: Setting data of task " + task.getTransitionId() + " [" + task.getStringId() + "]");

        if (task.getUserId() != null) {
            task.setUser(userService.findById(task.getUserId(), false));
        }
        SetDataEventOutcome outcome = new SetDataEventOutcome(useCase, task);
        dataSet.getFields().forEach((fieldId, value) -> {
            DataField dataField = useCase.getDataSet().get(fieldId);
            if (dataField != null) {
                Field field = useCase.getPetriNet().getField(fieldId).get();
                outcome.addOutcomes(resolveDataEvents(field, DataEventType.SET, EventPhase.PRE, useCase, task));
                if (outcome.getMessage() == null) {
                    Map<String, DataFieldLogic> caseDataSet = useCase.getPetriNet().getTransition(task.getTransitionId()).getDataSet();
                    if (field.getEvents().containsKey(DataEventType.SET) &&
                            ((DataEvent) field.getEvents().get(DataEventType.SET)).getMessage() != null) {
                        outcome.setMessage(((DataEvent) field.getEvents().get(DataEventType.SET)).getMessage());
                    } else if (caseDataSet.containsKey(fieldId)
                            && caseDataSet.get(fieldId).getEvents().containsKey(DataEventType.SET)
                            && caseDataSet.get(fieldId).getEvents().get(DataEventType.SET).getMessage() != null) {
                        outcome.setMessage(caseDataSet.get(fieldId).getEvents().get(DataEventType.SET).getMessage());
                    }
                }
                Object newValue = parseFieldsValues(value, dataField, field);
                dataField.setValue(newValue);
                ChangedField changedField = new ChangedField();
                changedField.setId(fieldId);
                changedField.addAttribute("value", newValue);
                List<String> allowedNets = parseAllowedNetsValue(value, field);
                if (allowedNets != null) {
                    dataField.setAllowedNets(allowedNets);
                    changedField.addAttribute("allowedNets", allowedNets);
                }
                Map<String, Object> filterMetadata = parseFilterMetadataValue(value, field);
                if (filterMetadata != null) {
                    dataField.setFilterMetadata(filterMetadata);
                    changedField.addAttribute("filterMetadata", filterMetadata);
                }
                outcome.addChangedField(fieldId, changedField);
                workflowService.save(useCase);
                historyService.save(new SetDataEventLog(task, useCase, EventPhase.PRE, Collections.singletonMap(fieldId, changedField)));
                outcome.addOutcomes(resolveDataEvents(field,
                        DataEventType.SET, EventPhase.POST, useCase, task));

                historyService.save(new SetDataEventLog(task, useCase, EventPhase.POST, null));
            }
        });
        updateDataset(useCase);
        outcome.setCase(workflowService.save(useCase));
        return outcome;
    }

    @Override
    public GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale) {
        return getDataGroups(taskId, locale, new HashSet<>(), 0, null);
    }

    private GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, Set<String> collectedTaskIds, int level, String parentTaskRefId) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        PetriNet net = useCase.getPetriNet();
        Transition transition = net.getTransition(task.getTransitionId());
        GetDataGroupsEventOutcome outcome = new GetDataGroupsEventOutcome(useCase, task);
        log.info("Getting groups of task " + taskId + " in case " + useCase.getTitle() + " level: " + level);
        List<DataGroup> resultDataGroups = new ArrayList<>();

        List<Field> data = getData(task, useCase).getData();
        Map<String, Field> dataFieldMap = data.stream().collect(Collectors.toMap(Field::getImportId, field -> field));
        List<DataGroup> dataGroups = transition.getDataGroups().values().stream().map(DataGroup::clone).collect(Collectors.toList());
        for (DataGroup dataGroup : dataGroups) {
            resolveTaskRefOrderOnGrid(dataGroup, dataFieldMap);
            resultDataGroups.add(dataGroup);
            log.debug("Setting groups of task " + taskId + " in case " + useCase.getTitle() + " level: " + level + " " + dataGroup.getImportId());

            List<Field> resources = new LinkedList<>();
            for (String dataFieldId : dataGroup.getData()) {
                Field field = net.getDataSet().get(dataFieldId);
                if (dataFieldMap.containsKey(dataFieldId)) {
                    Field resource = dataFieldMap.get(dataFieldId);
                    if (level != 0) {
                        dataGroup.setParentCaseId(useCase.getStringId());
                        resource.setParentCaseId(useCase.getStringId());
                        dataGroup.setParentTaskId(taskId);
                        dataGroup.setParentTransitionId(task.getTransitionId());
                        dataGroup.setParentTaskRefId(parentTaskRefId);
                        dataGroup.setNestingLevel(level);
                        resource.setParentTaskId(taskId);
                    }
                    resources.add(resource);
                    if (field.getType() == FieldType.TASK_REF) {
                        resultDataGroups.addAll(collectTaskRefDataGroups((TaskField) dataFieldMap.get(dataFieldId), locale, collectedTaskIds, level));
                    }
                }
            }
            dataGroup.setFields(new DataFieldsResource(resources, locale));
        }
        outcome.setData(resultDataGroups);
        return outcome;
    }

    private List<DataGroup> collectTaskRefDataGroups(TaskField taskRefField, Locale locale, Set<String> collectedTaskIds, int level) {
        List<String> taskIds = taskRefField.getValue();
        List<DataGroup> groups = new ArrayList<>();

        if (taskIds != null) {
            taskIds = taskIds.stream().filter(id -> !collectedTaskIds.contains(id)).collect(Collectors.toList());
            taskIds.forEach(id -> {
                collectedTaskIds.add(id);
                List<DataGroup> taskRefDataGroups = getDataGroups(id, locale, collectedTaskIds, level + 1, taskRefField.getStringId()).getData();
                resolveTaskRefBehavior(taskRefField, taskRefDataGroups);
                groups.addAll(taskRefDataGroups);
            });
        }

        return groups;
    }

    private void resolveTaskRefOrderOnGrid(DataGroup dataGroup, Map<String, Field> dataFieldMap) {
        if (dataGroup.getLayout() != null && Objects.equals(dataGroup.getLayout().getType(), "grid")) {
            dataGroup.setData(dataGroup.getData().stream().map(dataFieldMap::get).sorted(Comparator.comparingInt(a -> a.getLayout().getY())).map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new)));
        }
    }

    private void resolveTaskRefBehavior(TaskField taskRefField, List<DataGroup> taskRefDataGroups) {
        if (taskRefField.getBehavior().has("visible") && taskRefField.getBehavior().get("visible").asBoolean()) {
            taskRefDataGroups.forEach(dataGroup -> {
                dataGroup.getFields().getContent().forEach(field -> {
                    if (field.getBehavior().has("editable") && field.getBehavior().get("editable").asBoolean()) {
                        changeTaskRefBehavior(field, FieldBehavior.VISIBLE);
                    }
                });
            });
        } else if (taskRefField.getBehavior().has("hidden") && taskRefField.getBehavior().get("hidden").asBoolean()) {
            taskRefDataGroups.forEach(dataGroup -> {
                dataGroup.getFields().getContent().forEach(field -> {
                    if (!field.getBehavior().has("forbidden") || !field.getBehavior().get("forbidden").asBoolean())
                        changeTaskRefBehavior(field, FieldBehavior.HIDDEN);
                });
            });
        }
    }

    private void changeTaskRefBehavior(LocalisedField field, FieldBehavior behavior) {
        List<FieldBehavior> antonymBehaviors = Arrays.asList(behavior.getAntonyms());
        antonymBehaviors.forEach(beh -> field.getBehavior().remove(beh.name()));
        ObjectNode behaviorNode = JsonNodeFactory.instance.objectNode();
        behaviorNode.put(behavior.toString(), true);
        field.setBehavior(behaviorNode);
    }

    @Override
    public FileFieldInputStream getFileByTask(String taskId, String fieldId, boolean forPreview) throws FileNotFoundException {
        Task task = taskService.findOne(taskId);

        FileFieldInputStream fileFieldInputStream = getFileByCase(task.getCaseId(), task, fieldId, forPreview);

        if (fileFieldInputStream == null || fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File in field " + fieldId + " within task " + taskId + " was not found!");

        return fileFieldInputStream;
    }

    @Override
    public FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name) {
        Task task = taskService.findOne(taskId);
        return getFileByCaseAndName(task.getCaseId(), fieldId, name);
    }

    @Override
    public FileFieldInputStream getFileByCase(String caseId, Task task, String fieldId, boolean forPreview) {
        Case useCase = workflowService.findOne(caseId);
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFile(useCase, task, field, forPreview);
    }

    @Override
    public FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name) {
        Case useCase = workflowService.findOne(caseId);
        FileListField field = (FileListField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFileByName(useCase, field, name);
    }

    @Override
    public FileFieldInputStream getFileByName(Case useCase, FileListField field, String name) {
        runGetActionsFromFileField(field.getEvents(), useCase);
        if (useCase.getFieldValue(field.getStringId()) == null)
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
    public FileFieldInputStream getFile(Case useCase, Task task, FileField field, boolean forPreview) {
        runGetActionsFromFileField(field.getEvents(), useCase);
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

    private void runGetActionsFromFileField(Map<DataEventType, DataEvent> events, Case useCase) {
        if (events != null && !events.isEmpty() && events.containsKey(DataEventType.GET)) {
            DataEvent event = events.get(DataEventType.GET);
            event.getPreActions().forEach(action -> actionsRunner.run(action, useCase));
            event.getPostActions().forEach(action -> actionsRunner.run(action, useCase));
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
    public SetDataEventOutcome saveFile(String taskId, String fieldId, MultipartFile multipartFile) {
        Task task = taskService.findOne(taskId);
        ImmutablePair<Case, FileField> pair = getCaseAndFileField(taskId, fieldId);
        FileField field = pair.getRight();
        Case useCase = pair.getLeft();

        if (field.isRemote()) {
            upload(useCase, field, multipartFile);
        } else {
            saveLocalFile(useCase, field, multipartFile);
        }
        return new SetDataEventOutcome(useCase, task, getChangedFieldByFileFieldContainer(fieldId, task, useCase));
    }

    @Override
    public SetDataEventOutcome saveFiles(String taskId, String fieldId, MultipartFile[] multipartFiles) {
        Task task = taskService.findOne(taskId);
        ImmutablePair<Case, FileListField> pair = getCaseAndFileListField(taskId, fieldId);
        FileListField field = pair.getRight();
        Case useCase = pair.getLeft();

        if (field.isRemote()) {
            upload(useCase, field, multipartFiles);
        } else {
            saveLocalFiles(useCase, field, multipartFiles);
        }
        return new SetDataEventOutcome(useCase, task, getChangedFieldByFileFieldContainer(fieldId, task, useCase));
    }

    private List<EventOutcome> getChangedFieldByFileFieldContainer(String fieldId, Task referencingTask, Case useCase) {
        List<EventOutcome> outcomes = new ArrayList<>(resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(), DataEventType.SET,
                EventPhase.PRE, useCase, referencingTask));
        outcomes.addAll(resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(), DataEventType.SET,
                EventPhase.POST, useCase, referencingTask));
        updateDataset(useCase);
        workflowService.save(useCase);
        return outcomes;
    }

    private boolean saveLocalFiles(Case useCase, FileListField field, MultipartFile[] multipartFiles) {
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

            try {
                writeFile(oneFile, file);
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new EventNotExecutableException("File " + oneFile.getName() + " in case " + useCase.getStringId() + " could not be saved to file list field " + field.getStringId(), e);
            }
        }
        useCase.getDataSet().get(field.getStringId()).setValue(field.getValue());
        return true;
    }

    private boolean saveLocalFile(Case useCase, FileField field, MultipartFile multipartFile) {
        if (useCase.getDataSet().get(field.getStringId()).getValue() != null) {
            new File(field.getFilePath(useCase.getStringId())).delete();
            useCase.getDataSet().get(field.getStringId()).setValue(null);
        }

        field.setValue(multipartFile.getOriginalFilename());
        field.getValue().setPath(field.getFilePath(useCase.getStringId()));
        File file = new File(field.getFilePath(useCase.getStringId()));
        try {
            writeFile(multipartFile, file);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new EventNotExecutableException("File " + multipartFile.getName() + " in case " + useCase.getStringId() + " could not be saved to file field " + field.getStringId(), e);
        }

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
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);
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
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        FileListField field = (FileListField) useCase.getPetriNet().getDataSet().get(fieldId);
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

        List<Field> fields = task.getImmediateDataFields().stream().map(id -> fieldFactory.buildFieldWithoutValidation(useCase, id, task.getTransitionId())).collect(Collectors.toList());
        LongStream.range(0L, fields.size()).forEach(index -> fields.get((int) index).setOrder(index));

        return fields;
    }

    private void updateDataset(Case useCase) {
        Case actual = workflowService.findOne(useCase.getStringId());
        actual.getDataSet().forEach((id, dataField) -> {
            if (dataField.isNewerThen(useCase.getDataField(id))) {
                useCase.getDataSet().put(id, dataField);
            }
        });
    }

    private List<EventOutcome> resolveDataEvents(Field field, DataEventType trigger, EventPhase phase, Case useCase, Task task) {
        return eventService.processDataEvents(field, trigger, phase, useCase, task);
    }

    private Object parseFieldsValues(DataField newValueField, DataField dataField, Field petriNetField) {
        Object value = null;
        //TODO: NAE-1645 check if all types are parsed correctly
        switch (petriNetField.getType()) {
            case DATE:
                if (newValueField.getValue() == null) {
                    value = null;
                    break;
                }
                value = FieldFactory.parseDate(newValueField.getValue());
                break;
            case DATETIME:
                if (newValueField.getValue() == null) {
                    value = null;
                    break;
                }
                value = FieldFactory.parseDateTime(newValueField.getValue());
                break;
            case BOOLEAN:
                value = !(newValueField.getValue() == null) && ((Boolean) newValueField.getValue());
                break;
            case MULTICHOICE:
//                value = parseMultichoiceFieldValues(newValueField).stream().map(I18nString::new).collect(Collectors.toSet());
                // TODO: NAE-1645 string > i18nstring?
                break;
            case ENUMERATION:
                if (newValueField.getValue() == null) {
                    value = null;
                    break;
                }
                String val = (String) newValueField.getValue();
                value = new I18nString(val);
                break;
            case USER:
                if (newValueField.getValue() == null) {
                    value = null;
                    break;
                }
                // TODO: NAE-1645: remove?
//                User user = new User(userService.findById(newValueField.getValue().asLong(), true));
//                user.setPassword(null);
//                user.setGroups(null);
//                user.setAuthorities(null);
//                user.setUserProcessRoles(null);
                value = makeUserFieldValue((String) newValueField.getValue());
                break;
            case NUMBER:
                if (newValueField.getValue() == null) {
                    value = 0.0;
                    break;
                }
                value = newValueField.getValue();
                break;
            case FILE:
                if (newValueField.getValue() == null) {
                    value = new FileFieldValue();
                    break;
                }
                value = FileFieldValue.fromString((String) newValueField.getValue());
                break;
            case CASE_REF:
                List<String> list = (List<String>) newValueField.getValue();
                validateCaseRefValue(list, dataField.getAllowedNets());
                value = list;
                break;
            case TASK_REF:
                value = newValueField.getValue();
                // TODO 29.9.2020: validate task ref value? is such feature desired?
                break;
            case USERLIST:
                if (newValueField.getValue() == null) {
                    value = null;
                    break;
                }
                value = newValueField.getValue();
                break;
            default:
                value = newValueField.getValue(); // TODO: NAE-1645 check null value
                break;
        }
        if (value instanceof String && ((String) value).equalsIgnoreCase("null")) {
            return null;
        }
        return value;
    }

    protected UserFieldValue makeUserFieldValue(String id) {
        IUser user = userService.resolveById(id, true);
        return new UserFieldValue(user.getStringId(), user.getName(), user.getSurname(), user.getEmail());
    }

    private List<String> parseAllowedNetsValue(DataField dataField, Field field) {
        if (field.getType() == FieldType.CASE_REF || field.getType() == FieldType.FILTER) {
            return dataField.getAllowedNets();
        }
        return null;
    }

    // TODO: NAE-1645 wtf?
    private Map<String, Object> parseFilterMetadataValue(DataField dataField, Field field) {
//        if (field.getType() == FieldType.FILTER) {
//            JsonNode filterMetadata = node.get("filterMetadata");
//            if (filterMetadata == null) {
//                return null;
//            }
//            ObjectMapper mapper = new ObjectMapper();
//            return mapper.convertValue(filterMetadata, new TypeReference<Map<String, Object>>() {
//            });
//        }
        return null;
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
}