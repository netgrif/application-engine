package com.netgrif.application.engine.workflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.validation.service.interfaces.IValidationService;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.DataFieldsResource;
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedField;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
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

    @Autowired
    protected IPetriNetService petriNetService;

    @Autowired
    protected IValidationService validation;

    @Value("${nae.image.preview.scaling.px:400}")
    protected int imageScale;

    @Value("${nae.validation.setData.enable:false}")
    protected boolean validationEnable;

    @Override
    public GetDataEventOutcome getData(String taskId) {
        return getData(taskId, null);
    }

    @Override
    public GetDataEventOutcome getData(String taskId, Map<String, String> params) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());

        return getData(task, useCase, params);
    }

    @Override
    public GetDataEventOutcome getData(Task task, Case useCase) {
        return getData(task, useCase, new HashMap<>());
    }

    @Override
    public GetDataEventOutcome getData(Task task, Case useCase, Map<String, String> params) {
        log.info("[" + useCase.getStringId() + "]: Getting data of task " + task.getTransitionId() + " [" + task.getStringId() + "]");
        IUser user = userService.getLoggedOrSystem();
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
            outcome.addOutcomes(resolveDataEvents(field, DataEventType.GET, EventPhase.PRE, useCase, task, params));
            historyService.save(new GetDataEventLog(task, useCase, EventPhase.PRE, user));

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
                    dataSetFields.add(validationField);
                }
            } else {
                if (transition.getDataSet().get(fieldId).isDisplayable()) {
                    Field validationField = fieldFactory.buildFieldWithValidation(useCase, fieldId, transition.getStringId());
                    validationField.setBehavior(transition.getDataSet().get(fieldId).applyBehavior());
                    if (transition.getDataSet().get(fieldId).layoutExist() && transition.getDataSet().get(fieldId).getLayout().layoutFilled()) {
                        validationField.setLayout(transition.getDataSet().get(fieldId).getLayout().clone());
                    }
                    dataSetFields.add(validationField);
                }
            }
            outcome.addOutcomes(resolveDataEvents(field, DataEventType.GET, EventPhase.POST, useCase, task, params));
            historyService.save(new GetDataEventLog(task, useCase, EventPhase.POST, user));
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

    private boolean isForbidden(String fieldId, Transition transition, DataField dataField) {
        if (dataField.getBehavior().containsKey(transition.getImportId())) {
            return dataField.isForbidden(transition.getImportId());
        } else {
            return transition.getDataSet().get(fieldId).isForbidden();
        }
    }

    @Override
    public SetDataEventOutcome setData(String taskId, ObjectNode values) {
        return setData(taskId, values, new HashMap<>());
    }

    @Override
    public SetDataEventOutcome setData(String taskId, ObjectNode values, Map<String, String> params) {
        Task task = taskService.findOne(taskId);
        return setData(task, values, params);
    }

    @Override
    public SetDataEventOutcome setData(Task task, ObjectNode values) {
        return setData(task, values, new HashMap<>());
    }

    @Override
    public SetDataEventOutcome setData(Task task, ObjectNode values, Map<String, String> params) {
        Case useCase = workflowService.findOne(task.getCaseId());
        IUser user = userService.getLoggedOrSystem();

        log.info("[" + useCase.getStringId() + "]: Setting data of task " + task.getTransitionId() + " [" + task.getStringId() + "]");

        if (task.getUserId() != null) {
            task.setUser(userService.findById(task.getUserId(), false));
        }
        SetDataEventOutcome outcome = new SetDataEventOutcome(useCase, task);
        values.fields().forEachRemaining(entry -> {
            String fieldId = entry.getKey();
            DataField dataField = useCase.getDataSet().get(fieldId);
            if (dataField != null) {
                Field field = useCase.getPetriNet().getField(fieldId).get();
                outcome.addOutcomes(resolveDataEvents(field, DataEventType.SET, EventPhase.PRE, useCase, task, params));
                if (outcome.getMessage() == null) {
                    Map<String, DataFieldLogic> dataSet = useCase.getPetriNet().getTransition(task.getTransitionId()).getDataSet();
                    if (field.getEvents().containsKey(DataEventType.SET) &&
                            ((DataEvent) field.getEvents().get(DataEventType.SET)).getMessage() != null) {
                        outcome.setMessage(((DataEvent) field.getEvents().get(DataEventType.SET)).getMessage());
                    } else if (dataSet.containsKey(fieldId)
                            && dataSet.get(fieldId).getEvents().containsKey(DataEventType.SET)
                            && dataSet.get(fieldId).getEvents().get(DataEventType.SET).getMessage() != null) {
                        outcome.setMessage(dataSet.get(fieldId).getEvents().get(DataEventType.SET).getMessage());
                    }
                }
                boolean modified = false;
                ChangedField changedField = new ChangedField();
                changedField.setId(fieldId);
                Object newValue = parseFieldsValues(entry.getValue(), dataField, task.getStringId());
                if (entry.getValue().has("value") || getFieldTypeFromNode((ObjectNode) entry.getValue()).equals("button")) {
                    dataField.setValue(newValue);
                    changedField.addAttribute("value", newValue);
                    modified = true;
                }
                List<String> allowedNets = parseAllowedNetsValue(entry.getValue());
                if (allowedNets != null) {
                    dataField.setAllowedNets(allowedNets);
                    changedField.addAttribute("allowedNets", allowedNets);
                    modified = true;
                }
                String fieldType = getFieldTypeFromNode((ObjectNode) entry.getValue());
                Map<String, Object> filterMetadata = parseFilterMetadataValue((ObjectNode) entry.getValue(), fieldType);
                if (filterMetadata != null) {
                    dataField.setFilterMetadata(filterMetadata);
                    changedField.addAttribute("filterMetadata", filterMetadata);
                    modified = true;
                }
                Map<String, I18nString> options = parseOptionsNode(entry.getValue(), fieldType);
                if (options != null) {
                    setDataFieldOptions(options, dataField, changedField, fieldType);
                    modified = true;
                }
                Set<I18nString> choices = parseChoicesNode((ObjectNode) entry.getValue(), fieldType);
                if (choices != null) {
                    dataField.setChoices(choices);
                    changedField.addAttribute("choices", choices.stream().map(i18nString -> i18nString.getTranslation(LocaleContextHolder.getLocale())).collect(Collectors.toSet()));
                    modified = true;
                }
                Map<String, String> properties = parseProperties(entry.getValue());
                if (properties != null) {
                    outcome.addOutcome(this.changeComponentProperties(useCase, task, field.getStringId(), properties));
                    modified = true;
                }
                if (modified) {
                    dataField.setLastModified(LocalDateTime.now());
                }
                if (validationEnable) {
                    validation.valid(useCase.getPetriNet().getDataSet().get(entry.getKey()), dataField);
                }
                outcome.addChangedField(fieldId, changedField);
                workflowService.save(useCase);
                historyService.save(new SetDataEventLog(task, useCase, EventPhase.PRE, Collections.singletonMap(fieldId, changedField), user));
                outcome.addOutcomes(resolveDataEvents(field, DataEventType.SET, EventPhase.POST, useCase, task, params));

                historyService.save(new SetDataEventLog(task, useCase, EventPhase.POST, null, user));
                applyFieldConnectedChanges(useCase, field);
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
                    if (field.getType() == FieldType.TASK_REF && shouldResolveTaskRefData(field, transition.getDataSet().get(field.getStringId()))) {
                        resultDataGroups.addAll(collectTaskRefDataGroups((TaskField) dataFieldMap.get(dataFieldId), locale, collectedTaskIds, level));
                    }
                }
            }
            dataGroup.setFields(new DataFieldsResource(resources, locale));
        }
        outcome.setData(resultDataGroups);
        return outcome;
    }

    private boolean shouldResolveTaskRefData(Field<?> field, DataFieldLogic dataRef) {
        if (dataRef.getComponent() != null) {
            return hasRequiredComponentProperty(dataRef.getComponent(), "resolve_data", "true");
        } else if (field.getComponent() != null) {
            return hasRequiredComponentProperty(field.getComponent(), "resolve_data", "true");
        }
        return true;
    }

    private boolean hasRequiredComponentProperty(Component component, String propertyName, String propertyValue) {
        return component != null
                && component.getProperties() != null
                && component.getProperties().containsKey(propertyName)
                && component.getProperties().get(propertyName).equals(propertyValue);
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
            dataGroup.setData(
                    dataGroup.getData().stream()
                            .filter(dataFieldMap::containsKey)
                            .map(dataFieldMap::get)
                            .sorted(Comparator.comparingInt(a -> a.getLayout().getY()))
                            .map(Field::getStringId)
                            .collect(Collectors.toCollection(LinkedHashSet::new))
            );
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
        return getFileByTaskAndName(taskId, fieldId, name, new HashMap<>());
    }

    @Override
    public FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name, Map<String, String> params) {
        Task task = taskService.findOne(taskId);
        return getFileByCaseAndName(task.getCaseId(), fieldId, name, params);
    }

    @Override
    public FileFieldInputStream getFileByCase(String caseId, Task task, String fieldId, boolean forPreview) {
        Case useCase = workflowService.findOne(caseId);
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFile(useCase, task, field, forPreview);
    }

    @Override
    public FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name) {
        return getFileByCaseAndName(caseId, fieldId, name, new HashMap<>());
    }

    @Override
    public FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name, Map<String, String> params) {
        Case useCase = workflowService.findOne(caseId);
        FileListField field = (FileListField) useCase.getPetriNet().getDataSet().get(fieldId);
        return getFileByName(useCase, field, name, params);
    }

    @Override
    public FileFieldInputStream getFileByName(Case useCase, FileListField field, String name) {
        return getFileByName(useCase, field, name, new HashMap<>());
    }

    @Override
    public FileFieldInputStream getFileByName(Case useCase, FileListField field, String name, Map<String, String> params) {
        runGetActionsFromFileField(field.getEvents(), useCase, params);
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
        return getFile(useCase, task, field, forPreview, new HashMap<>());
    }

    @Override
    public FileFieldInputStream getFile(Case useCase, Task task, FileField field, boolean forPreview, Map<String, String> params) {
        runGetActionsFromFileField(field.getEvents(), useCase, params);
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

    private void runGetActionsFromFileField(Map<DataEventType, DataEvent> events, Case useCase, Map<String, String> params) {
        if (events != null && !events.isEmpty() && events.containsKey(DataEventType.GET)) {
            DataEvent event = events.get(DataEventType.GET);
            event.getPreActions().forEach(action -> actionsRunner.run(action, useCase, params));
            event.getPostActions().forEach(action -> actionsRunner.run(action, useCase, params));
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
        return saveFile(taskId, fieldId, multipartFile, new HashMap<>());
    }

    @Override
    public SetDataEventOutcome saveFile(String taskId, String fieldId, MultipartFile multipartFile, Map<String, String> params) {
        Task task = taskService.findOne(taskId);
        ImmutablePair<Case, FileField> pair = getCaseAndFileField(taskId, fieldId);
        FileField field = pair.getRight();
        Case useCase = pair.getLeft();

        if (field.isRemote()) {
            upload(useCase, field, multipartFile);
        } else {
            saveLocalFile(useCase, field, multipartFile);
        }
        return new SetDataEventOutcome(useCase, task, getChangedFieldByFileFieldContainer(fieldId, task, useCase, params));
    }

    @Override
    public SetDataEventOutcome saveFiles(String taskId, String fieldId, MultipartFile[] multipartFiles) {
        return saveFiles(taskId, fieldId, multipartFiles, new HashMap<>());
    }

    @Override
    public SetDataEventOutcome saveFiles(String taskId, String fieldId, MultipartFile[] multipartFiles, Map<String, String> params) {
        Task task = taskService.findOne(taskId);
        ImmutablePair<Case, FileListField> pair = getCaseAndFileListField(taskId, fieldId);
        FileListField field = pair.getRight();
        Case useCase = pair.getLeft();

        if (field.isRemote()) {
            upload(useCase, field, multipartFiles);
        } else {
            saveLocalFiles(useCase, field, multipartFiles);
        }
        return new SetDataEventOutcome(useCase, task, getChangedFieldByFileFieldContainer(fieldId, task, useCase, params));
    }

    private List<EventOutcome> getChangedFieldByFileFieldContainer(String fieldId, Task referencingTask, Case useCase, Map<String, String> params) {
        List<EventOutcome> outcomes = new ArrayList<>(resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(), DataEventType.SET,
                EventPhase.PRE, useCase, referencingTask, params));
        outcomes.addAll(resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(), DataEventType.SET,
                EventPhase.POST, useCase, referencingTask, params));
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

    protected boolean upload(Case useCase, FileField field, MultipartFile multipartFile) {
        throw new UnsupportedOperationException("Upload new file to the remote storage is not implemented yet.");
    }

    protected boolean upload(Case useCase, FileListField field, MultipartFile[] multipartFiles) {
        throw new UnsupportedOperationException("Upload new files to the remote storage is not implemented yet.");
    }

    protected boolean deleteRemote(Case useCase, FileField field) {
        throw new UnsupportedOperationException("Delete file from the remote storage is not implemented yet.");
    }

    protected boolean deleteRemote(Case useCase, FileListField field, String name) {
        throw new UnsupportedOperationException("Delete file from the remote storage is not implemented yet.");
    }

    @Override
    public SetDataEventOutcome deleteFile(String taskId, String fieldId) {
        return deleteFile(taskId, fieldId, new HashMap<>());
    }

    @Override
    public SetDataEventOutcome deleteFile(String taskId, String fieldId, Map<String, String> params) {
        ImmutablePair<Case, FileField> pair = getCaseAndFileField(taskId, fieldId);
        FileField field = pair.getRight();
        Case useCase = pair.getLeft();
        Task task = taskService.findById(taskId);

        if (useCase.getDataSet().get(field.getStringId()).getValue() != null) {
            if (field.isRemote()) {
                deleteRemote(useCase, field);
            } else {
                new File(field.getValue().getPath()).delete();
                new File(field.getFilePreviewPath(useCase.getStringId())).delete();
            }
            useCase.getDataSet().get(field.getStringId()).setValue(null);
        }
        return new SetDataEventOutcome(useCase, task, getChangedFieldByFileFieldContainer(fieldId, task, useCase, params));
    }

    private ImmutablePair<Case, FileField> getCaseAndFileField(String taskId, String fieldId) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        FileField field = (FileField) useCase.getPetriNet().getDataSet().get(fieldId);
        field.setValue((FileFieldValue) useCase.getDataField(field.getStringId()).getValue());

        return new ImmutablePair<>(useCase, field);
    }

    @Override
    public SetDataEventOutcome deleteFileByName(String taskId, String fieldId, String name) {
        return deleteFileByName(taskId, fieldId, name, new HashMap<>());
    }

    @Override
    public SetDataEventOutcome deleteFileByName(String taskId, String fieldId, String name, Map<String, String> params) {
        ImmutablePair<Case, FileListField> pair = getCaseAndFileListField(taskId, fieldId);
        FileListField field = pair.getRight();
        Case useCase = pair.getLeft();
        Task task = taskService.findOne(taskId);

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
        return new SetDataEventOutcome(useCase, task, getChangedFieldByFileFieldContainer(fieldId, task, useCase, params));
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

    @Override
    public UserFieldValue makeUserFieldValue(String id) {
        IUser user = userService.resolveById(id, true);
        return new UserFieldValue(user);
    }

    private void updateDataset(Case useCase) {
        Case actual = workflowService.findOne(useCase.getStringId());
        actual.getDataSet().forEach((id, dataField) -> {
            if (dataField.isNewerThen(useCase.getDataField(id))) {
                useCase.getDataSet().put(id, dataField);
            }
        });
    }

    @Override
    public Case applyFieldConnectedChanges(Case useCase, String fieldId) {
        PetriNet petriNet = petriNetService.getPetriNet(useCase.getPetriNetId());
        Optional<Field> field = petriNet.getField(fieldId);
        if (field.isEmpty()) {
            throw new IllegalArgumentException("Field with given id [" + fieldId + "] does not exists on Petri net [" + petriNet.getStringId() + " " + petriNet.getIdentifier() + "]");
        }
        return applyFieldConnectedChanges(useCase, field.get());
    }

    @Override
    public Case applyFieldConnectedChanges(Case useCase, Field field) {
        switch (field.getType()) {
            case USERLIST:
                return workflowService.resolveUserRef(useCase);
            default:
                return useCase;
        }
    }

    @Override
    public SetDataEventOutcome changeComponentProperties(Case useCase, String transitionId, String fieldId,  Map<String, String> properties) {
        Predicate predicate = QTask.task.caseId.eq(useCase.getStringId()).and(QTask.task.transitionId.eq(transitionId));
        Task task = taskService.searchOne(predicate);
        return this.changeComponentProperties(useCase, task, fieldId, properties);
    }

    @Override
    public SetDataEventOutcome changeComponentProperties(Case useCase, Task task, String fieldId, Map<String, String> properties) {
        Component comp = useCase.getDataField(fieldId).getDataRefComponents().get(task.getTransitionId());
        return this.resolveComponentProperties(comp, useCase, task, fieldId, properties);
    }

    @Override
    public SetDataEventOutcome changeComponentProperties(Case useCase, String fieldId, Map<String, String> properties) {
        Component comp = useCase.getDataField(fieldId).getComponent();
        return this.resolveComponentProperties(comp, useCase, null, fieldId, properties);
    }

    private SetDataEventOutcome resolveComponentProperties(Component comp, Case useCase, Task task, String fieldId, Map<String, String> properties) {
        SetDataEventOutcome outcome = new SetDataEventOutcome(useCase, task);
        ChangedField changedField = new ChangedField();
        changedField.setId(fieldId);
        if (comp != null) {
            properties.forEach((key, value) -> comp.getProperties().put(key, value));
            changedField.addAttribute("component", comp);
            outcome.addChangedField(fieldId, changedField);
            if (task == null) {
                useCase.getDataField(fieldId).setComponent(comp);
            } else {
                useCase.getDataField(fieldId).addDataRefComponent(task.getTransitionId(), comp);
            }
        } else if (task == null) {
            log.debug("Setting component on field " + fieldId + " in case [" + useCase.getTitle() + "] as default");
            Component newComp = new Component("default", properties);
            useCase.getDataField(fieldId).setComponent(newComp);
            changedField.addAttribute("component", newComp);
            outcome.addChangedField(fieldId, changedField);
        } else {
            log.warn("Setting properties on field " + fieldId + " on task [" + task.getStringId() + "] in case [" + useCase.getTitle() + "] failed, field dont have component!");
        }
        outcome.setCase(workflowService.save(useCase));
        return outcome;
    }

    private List<EventOutcome> resolveDataEvents(Field field, DataEventType trigger, EventPhase phase, Case useCase, Task task, Map<String, String> params) {
        return eventService.processDataEvents(field, trigger, phase, useCase, task, params);
    }

    private Object parseFieldsValues(JsonNode jsonNode, DataField dataField, String taskId) {
        ObjectNode node = (ObjectNode) jsonNode;
        Object value;
        switch (getFieldTypeFromNode(node)) {
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
                value = parseMultichoiceFieldValues(node).stream().map(I18nString::new).collect(Collectors.toCollection(LinkedHashSet::new));
                break;
            case "multichoice_map":
                value = parseMultichoiceFieldValues(node);
                break;
            case "enumeration":
                if (node.get("value") == null || node.get("value").asText() == null || "null".equals(node.get("value").asText())) {
                    value = null;
                    break;
                }
                value = parseI18nString(node.get("value"));
                break;
            case "user":
                if (node.get("value") == null || node.get("value").isNull()) {
                    value = null;
                    break;
                }
//                User user = new User(userService.findById(node.get("value").asLong(), true));
//                user.setPassword(null);
//                user.setGroups(null);
//                user.setAuthorities(null);
//                user.setUserProcessRoles(null);
                value = makeUserFieldValue(node.get("value").asText());
                break;
            case "number":
                if (node.get("value") == null || node.get("value").isNull()) {
                    value = null;
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
                if (list == null) {
                    value = null;
                    break;
                }
                validateCaseRefValue(list, dataField.getAllowedNets());
                value = list;
                break;
            case "taskRef":
                List<String> listTask = parseListStringValues(node);
                validateTaskRefValue(listTask, taskId);
                value = listTask;
                break;
            case "stringCollection":
                value = parseListStringValues(node);
                break;
            case "userList":
                if (node.get("value") == null) {
                    value = null;
                    break;
                }
                value = makeUserListFieldValue(node);
                break;
            case "button":
                if (node.get("value") == null) {
                    if (dataField.getValue() == null) {
                        value = 1;
                        break;
                    }
                    value = Integer.parseInt(dataField.getValue().toString()) + 1;
                } else {
                    value = node.get("value").asInt();
                }
                break;
            case "i18n":
                if (node.get("value") == null) {
                    value = new I18nString("");
                    break;
                }
                value = parseI18nStringValues((ObjectNode) node.get("value"));
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
        HashSet<String> set = new LinkedHashSet<>();
        arrayNode.forEach(item -> set.add(item.asText()));
        return set;
    }

    private UserListFieldValue makeUserListFieldValue(ObjectNode nodes) {
        Set<String> userIds = new LinkedHashSet<>(parseListStringValues(nodes));
        return new UserListFieldValue(userIds.stream().map(this::makeUserFieldValue).collect(Collectors.toSet()));
    }

    private List<String> parseListStringValues(ObjectNode node) {
        return parseListString(node, "value");
    }

    private List<Long> parseListLongValues(ObjectNode node) {
        ArrayNode arrayNode = (ArrayNode) node.get("value");
        ArrayList<Long> list = new ArrayList<>();
        arrayNode.forEach(string -> list.add(string.asLong()));
        return list;
    }

    private List<String> parseAllowedNetsValue(JsonNode jsonNode) {
        ObjectNode node = (ObjectNode) jsonNode;
        String fieldType = getFieldTypeFromNode(node);
        if (Objects.equals(fieldType, FieldType.CASE_REF.getName()) || Objects.equals(fieldType, FieldType.FILTER.getName())) {
            return parseListStringAllowedNets(node);
        }
        return null;
    }

    private List<String> parseListStringAllowedNets(ObjectNode node) {
        return parseListString(node, "allowedNets");
    }

    private List<String> parseListString(ObjectNode node, String attributeKey) {
        ArrayNode arrayNode = (ArrayNode) node.get(attributeKey);
        if (arrayNode == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>();
        arrayNode.forEach(string -> list.add(string.asText()));
        return list;
    }

    private List<I18nString> parseListI18nString(ObjectNode node, String attributeKey) {
        ArrayNode arrayNode = (ArrayNode) node.get(attributeKey);
        if (arrayNode == null) {
            return null;
        }
        ArrayList<I18nString> list = new ArrayList<>();
        arrayNode.forEach(item -> {
            list.add(parseI18nString(item));
        });
        return list;
    }

    private I18nString parseI18nString(JsonNode node) {
        if (node.isTextual()) {
            return new I18nString(node.asText());
        }
        return parseI18nStringValues((ObjectNode) node);
    }

    private String getFieldTypeFromNode(ObjectNode node) {
        return node.get("type").asText();
    }

    private Map<String, Object> parseFilterMetadataValue(ObjectNode node, String fieldType) {
        if (Objects.equals(fieldType, FieldType.FILTER.getName())) {
            JsonNode filterMetadata = node.get("filterMetadata");
            if (filterMetadata == null) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(filterMetadata, new TypeReference<Map<String, Object>>() {
            });
        }
        return null;
    }

    private I18nString parseI18nStringValues(ObjectNode node) {
        String defaultValue = node.get("defaultValue") != null ? node.get("defaultValue").asText() : "";
        Map<String, String> translations = new HashMap<>();
        if (node.get("translations") != null) {
            node.get("translations").fields().forEachRemaining(entry ->
                    translations.put(entry.getKey(), entry.getValue().asText())
            );
        }
        return new I18nString(defaultValue, translations);
    }

    private Map<String, I18nString> parseOptionsNode(JsonNode node, String fieldType) {
        if (Objects.equals(fieldType, FieldType.ENUMERATION_MAP.getName()) || Objects.equals(fieldType, FieldType.MULTICHOICE_MAP.getName()) ||
                Objects.equals(fieldType, FieldType.ENUMERATION.getName()) || Objects.equals(fieldType, FieldType.MULTICHOICE.getName())) {
            return parseOptions(node);
        }
        return null;
    }

    private Map<String, I18nString> parseOptions(JsonNode node) {
        JsonNode optionsNode = node.get("options");
        if (optionsNode == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(I18nString.class, new I18nStringDeserializer());
        mapper.registerModule(module);
        Map<String, I18nString> optionsMapped = mapper.convertValue(optionsNode, new TypeReference<Map<String, I18nString>>() {});
        if (optionsMapped.isEmpty()) {
            return null;
        }
        return optionsMapped;
    }

    private void setDataFieldOptions(Map<String, I18nString> options, DataField dataField, ChangedField changedField, String fieldType) {
        if (Objects.equals(fieldType, FieldType.ENUMERATION_MAP.getName()) || Objects.equals(fieldType, FieldType.MULTICHOICE_MAP.getName())) {
            dataField.setOptions(options);
            changedField.addAttribute("options", options.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            e -> e.getValue().getTranslation(LocaleContextHolder.getLocale()))));
        } else {
            Set<I18nString> choices = new LinkedHashSet<>();
            options.forEach((key, value) -> choices.add(value));
            dataField.setChoices(choices);
            changedField.addAttribute("choices", choices
                    .stream()
                    .map(i18nString -> i18nString
                            .getTranslation(LocaleContextHolder.getLocale())).collect(Collectors.toSet()));
        }
    }

    private Set<I18nString> parseChoicesNode(ObjectNode node, String fieldType) {
        if (Objects.equals(fieldType, FieldType.ENUMERATION.getName()) || Objects.equals(fieldType, FieldType.MULTICHOICE.getName())) {
            List<I18nString> list = parseListI18nString(node, "choices");
            if (list != null) {
                return new HashSet<>(list);
            }
        }
        return null;
    }

    private Map<String, String> parseProperties(JsonNode node) {
        JsonNode propertiesNode =  node.get("properties");
        if (propertiesNode == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> propertiesMapped = mapper.convertValue(propertiesNode, new TypeReference<Map<String, String>>() {});
        if (propertiesMapped.isEmpty()) {
            return null;
        }
        return propertiesMapped;
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

    public void validateTaskRefValue(List<String> taskIds, String restrictedTaskId) throws IllegalArgumentException {
        if (taskIds != null && taskIds.contains(restrictedTaskId)) {
            throw new IllegalArgumentException(String.format("Task with id '%s' cannot be added to task ref, since it is a task which is displaying task ref", restrictedTaskId));
        }
    }
}
