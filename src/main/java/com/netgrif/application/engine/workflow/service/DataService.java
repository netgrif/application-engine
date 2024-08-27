package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.history.domain.dataevents.GetDataEventLog;
import com.netgrif.application.engine.history.domain.dataevents.SetDataEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.DataEventType;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.LayoutType;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.*;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.transaction.NaeTransaction;
import com.netgrif.application.engine.validation.service.interfaces.IValidationService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import com.netgrif.application.engine.workflow.domain.EventNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.taskoutcomes.TaskEventOutcome;
import com.netgrif.application.engine.workflow.domain.params.GetDataParams;
import com.netgrif.application.engine.workflow.domain.params.SetDataParams;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.util.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataService implements IDataService {

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ActionRunner actionsRunner;

    @Autowired
    private IEventService eventService;

    @Autowired
    private IHistoryService historyService;

    @Autowired
    private MongoTransactionManager transactionManager;

    @Autowired
    private IValidationService validation;

    @Value("${nae.image.preview.scaling.px:400}")
    private int imageScale;

    @Value("${nae.validation.setData.enable:false}")
    private boolean validationEnable;

    /**
     * Gets the data of the {@link Task} provided by {@link GetDataParams}
     *
     * @param getDataParams parameters where the Task is specified
     * <br>
     * <b>Required parameters</b>
     * <ul>
     *      <li>taskId or task</li>
     *      <li>user</li>
     * </ul>
     *
     * @return outcome containing the fields present in Task
     * */
    @Override
    public GetDataEventOutcome getData(GetDataParams getDataParams) {
        fillMissingAttributes(getDataParams);

        Case useCase = getDataParams.getUseCase();
        Task task = getDataParams.getTask();

        log.info("[{}]: Getting data of task {} [{}]", useCase.getStringId(), task.getTransitionId(), task.getStringId());

        if (getDataParams.isTransactional()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .timeout(TransactionDefinition.TIMEOUT_DEFAULT)
                    .forceCreation(false)
                    .transactionManager(transactionManager)
                    .event(new Closure<GetDataEventOutcome>(null) {
                        @Override
                        public GetDataEventOutcome call() {
                            return doGetData(getDataParams);
                        }
                    })
                    .build();
            transaction.begin();
            return (GetDataEventOutcome) transaction.getResultOfEvent();
        } else {
            return doGetData(getDataParams);
        }
    }

    private GetDataEventOutcome doGetData(GetDataParams getDataParams) {
        Case useCase = getDataParams.getUseCase();
        Task task = getDataParams.getTask();
        IUser user = getDataParams.getUser();
        Transition transition = useCase.getPetriNet().getTransition(task.getTransitionId());
        Map<String, DataRef> dataRefs = transition.getDataSet();
        List<DataRef> dataSetFields = new ArrayList<>();
        GetDataEventOutcome outcome = new GetDataEventOutcome(useCase, task);

        dataRefs.forEach((fieldId, dataRef) -> {
            Field<?> field = useCase.getDataSet().get(fieldId);
            DataFieldBehavior behavior = field.getBehaviors().get(task.getTransitionId());
            // TODO: release/8.0.0 behavior is null
            if (behavior.isForbidden()) {
                return;
            }
            outcome.addOutcomes(resolveDataEvents(field, DataEventType.GET, EventPhase.PRE, useCase, task, null,
                    getDataParams.getParams()));

            historyService.save(new GetDataEventLog(task, useCase, EventPhase.PRE, user));

            if (outcome.getMessage() == null) {
                setOutcomeMessage(task, useCase, outcome, fieldId, field, DataEventType.GET);
            }
            dataRef.setField(field);
            dataRef.setFieldId(fieldId);
            dataRef.setBehavior(behavior);
            dataSetFields.add(dataRef);
            // TODO: release/8.0.0 params into outcome?
            outcome.addOutcomes(resolveDataEvents(field, DataEventType.GET, EventPhase.POST, useCase, task, null,
                    getDataParams.getParams()));

            historyService.save(new GetDataEventLog(task, useCase, EventPhase.POST, user));
        });

        workflowService.save(useCase);
        outcome.setData(dataSetFields);
        return outcome;
    }

    private void fillMissingAttributes(GetDataParams getDataParams) throws IllegalArgumentException {
        if (getDataParams.getUser() == null) {
            throw new IllegalArgumentException("User must be provided on get data.");
        }
        if (getDataParams.getTask() == null) {
            Task task = taskService.findOne(getDataParams.getTaskId());
            getDataParams.setTask(task);
        }
        if (getDataParams.getUseCase() == null) {
            Case useCase = workflowService.findOne(getDataParams.getTask().getCaseId());
            getDataParams.setUseCase(useCase);
        }
    }

    /**
     * Sets the provided data by {@link SetDataParams}
     *
     * @param setDataParams parameters containing the data to be set within the {@link Case} or {@link Task} object
     * <br>
     * <b>Required parameters</b>
     * <ul>
     *      <li>taskId or task or useCase</li>
     *      <li>user</li>
     * </ul>
     *
     * @return outcome containing changed fields, updated Task and Case
     * */
    @Override
    public SetDataEventOutcome setData(SetDataParams setDataParams) {
        fillMissingAttributes(setDataParams);

        Task task = setDataParams.getTask();

        log.info("[{}]: Setting data of task {} [{}]", task.getStringId(), task.getTransitionId(), task.getStringId());
        if (task.getUserId() != null) {
            task.setUser(userService.findById(task.getUserId()));
        }

        if (setDataParams.isTransactional()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .timeout(TransactionDefinition.TIMEOUT_DEFAULT)
                    .forceCreation(false)
                    .transactionManager(transactionManager)
                    .event(new Closure<SetDataEventOutcome>(null) {
                        @Override
                        public SetDataEventOutcome call() {
                            return doSetData(setDataParams);
                        }
                    })
                    .build();
            transaction.begin();
            return (SetDataEventOutcome) transaction.getResultOfEvent();
        } else {
            return doSetData(setDataParams);
        }
    }

    private SetDataEventOutcome doSetData(SetDataParams setDataParams) {
        Task task = setDataParams.getTask();
        List<EventOutcome> outcomes = new ArrayList<>();
        for (Map.Entry<String, Field<?>> stringFieldEntry : setDataParams.getDataSet().getFields().entrySet()) {
            String fieldId = stringFieldEntry.getKey();
            Field<?> newDataField = stringFieldEntry.getValue();
            SetDataEventOutcome setDataEventOutcome = setDataField(task, fieldId, newDataField, setDataParams.getUser());
            outcomes.add(setDataEventOutcome);
        }
        Case useCase = workflowService.findOne(task.getCaseId());
        return new SetDataEventOutcome(useCase, task, outcomes);
    }

    private void fillMissingAttributes(SetDataParams setDataParams) {
        if (setDataParams.getUser() == null) {
            throw new IllegalArgumentException("User must be provided on set data.");
        }
        if (setDataParams.getTask() == null) {
            if (setDataParams.getTaskId() != null) {
                Task task = taskService.findOne(setDataParams.getTaskId());
                setDataParams.setTask(task);
            } else if (setDataParams.getUseCase() != null) {
                Task fakeTask = Task.with()
                        .id(new ObjectId())
                        .caseId(setDataParams.getUseCase().getStringId())
                        .title(new I18nString("Fake"))
                        .transitionId("fake")
                        .build();
                setDataParams.setTask(fakeTask);
            } else {
                throw new IllegalArgumentException("Cannot set data without provided task.");
            }
        }
    }

    @Override
    public SetDataEventOutcome setDataField(Task task, String fieldId, Field<?> newDataField, IUser user) {
        return setDataField(task, fieldId, newDataField, user, new HashMap<>());
    }

    // TODO: release/8.0.0 check
    @Override
    public SetDataEventOutcome setDataField(Task task, String fieldId, Field<?> newDataField, IUser user, Map<String, String> params) {
        // TODO: NAE-1859 permissions?
        Case useCase = workflowService.findOne(task.getCaseId());
        SetDataEventOutcome outcome = new SetDataEventOutcome(useCase, task);
        Optional<Field<?>> fieldOptional = useCase.getPetriNet().getField(fieldId);
        if (fieldOptional.isEmpty()) {
            throw new IllegalArgumentException("[" + useCase.getStringId() + "] Field " + fieldId + " does not exist in case " + useCase.getTitle() + " of process " + useCase.getPetriNet().getStringId());
        }
        Field<?> field = fieldOptional.get();
        // PRE
        List<EventOutcome> preSetOutcomes = resolveDataEvents(field, DataEventType.SET, EventPhase.PRE, useCase, task,
                newDataField, params);
        if (!preSetOutcomes.isEmpty()) {
            outcome.addOutcomes(preSetOutcomes);
            useCase = workflowService.findOne(task.getCaseId());
        }
        historyService.save(new SetDataEventLog(task, useCase, EventPhase.PRE, DataSet.of(fieldId, newDataField), user));

        // EXECUTION
        if (outcome.getMessage() == null) {
            setOutcomeMessage(task, useCase, outcome, fieldId, field, DataEventType.SET);
        }
        useCase.getDataSet().get(fieldId).applyChanges(newDataField);
        if (validationEnable) {
            validation.valid(useCase.getDataSet().get(fieldId));
        }
        useCase = workflowService.save(useCase);
        outcome.addChangedField(fieldId, newDataField);
        historyService.save(new SetDataEventLog(task, useCase, EventPhase.EXECUTION, DataSet.of(fieldId, newDataField), user));

        // POST
        List<EventOutcome> postSetOutcomes = resolveDataEvents(field, DataEventType.SET, EventPhase.POST, useCase, task,
                newDataField, params);
        if (!postSetOutcomes.isEmpty()) {
            outcome.addOutcomes(postSetOutcomes);
            useCase = workflowService.findOne(task.getCaseId());
        }
        historyService.save(new SetDataEventLog(task, useCase, EventPhase.POST, DataSet.of(fieldId, newDataField), user));

        useCase = applyFieldConnectedChanges(useCase, field);
        outcome.setCase(useCase);

        return outcome;
    }

    private void setOutcomeMessage(Task task, Case useCase, TaskEventOutcome outcome, String fieldId, Field<?> field, DataEventType type) {
        Map<String, DataRef> caseDataSet = useCase.getPetriNet().getTransition(task.getTransitionId()).getDataSet();
        I18nString message = null;
        if (field.getEvents().containsKey(type)) {
            message = field.getEvents().get(type).getMessage();
        } else if (caseDataSet.containsKey(fieldId) && caseDataSet.get(fieldId).getEvents().containsKey(type)) {
            message = caseDataSet.get(fieldId).getEvents().get(type).getMessage();
        }
        outcome.setMessage(message);
    }

    @Override
    public GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, LoggedUser loggedUser) {
        IUser user = userService.getUserFromLoggedUser(loggedUser);
        return getDataGroups(taskId, locale, user);
    }

    @Override
    public GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, IUser user) {
        return getDataGroups(taskId, locale, new HashSet<>(), 0, null, user);
    }

    private GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, Set<String> collectedTaskIds, int level, String parentTaskRefId, IUser user) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        PetriNet net = useCase.getPetriNet();
        Transition transition = net.getTransition(task.getTransitionId());
        GetDataGroupsEventOutcome outcome = new GetDataGroupsEventOutcome(useCase, task);
        log.info("Getting groups of task " + taskId + " in case " + useCase.getTitle() + " level: " + level);
        List<DataGroup> resultDataGroups = new ArrayList<>();
        List<DataRef> data = getData(new GetDataParams(task, useCase, user)).getData();
        Map<String, DataRef> dataFieldMap = data.stream().collect(Collectors.toMap(DataRef::getFieldId, field -> field));
        List<DataGroup> dataGroups = transition.getDataGroups().values().stream().map(DataGroup::clone).collect(Collectors.toList());
        for (DataGroup dataGroup : dataGroups) {
            resolveTaskRefOrderOnGrid(dataGroup, dataFieldMap);
            resultDataGroups.add(dataGroup);
            log.debug("Setting groups of task " + taskId + " in case " + useCase.getTitle() + " level: " + level + " " + dataGroup.getImportId());

            LinkedHashMap<String, DataRef> resources = new LinkedHashMap<>();
            for (String dataFieldId : dataGroup.getData()) {
                Field<?> field = net.getDataSet().get(dataFieldId);
                if (!dataFieldMap.containsKey(dataFieldId)) {
                    continue;
                }
                DataRef resource = dataFieldMap.get(dataFieldId);
                if (level != 0) {
                    dataGroup.setParentCaseId(useCase.getStringId());
                    resource.setParentCaseId(useCase.getStringId());
                    dataGroup.setParentTaskId(taskId);
                    dataGroup.setParentTransitionId(task.getTransitionId());
                    dataGroup.setParentTaskRefId(parentTaskRefId);
                    dataGroup.setNestingLevel(level);
                    resource.setParentTaskId(taskId);
                }
                resources.put(dataFieldId, resource);
                // TODO: release/8.0.0
                // if (field.getType() == FieldType.TASK_REF && shouldResolveTaskRefData(field, transition.getDataSet().get(field.getStringId()))) {
                if (field.getType() == DataType.TASK_REF) {
                    resultDataGroups.addAll(collectTaskRefDataGroups(dataFieldMap.get(dataFieldId), locale, collectedTaskIds, level, user));
                }
            }
            dataGroup.setDataRefs(resources);
        }
        outcome.setData(resultDataGroups);
        return outcome;
    }

    // TODO: release/8.0.0 needed?
//    private boolean shouldResolveTaskRefData(Field<?> field, DataFieldLogic dataRef) {
//        if (dataRef.getComponent() != null) {
//            return hasRequiredComponentProperty(dataRef.getComponent(), "resolve_data", "true");
//        } else if (field.getComponent() != null) {
//            return hasRequiredComponentProperty(field.getComponent(), "resolve_data", "true");
//        }
//        return true;
//    }

    // TODO: release/8.0.0 refactor?
    private boolean hasRequiredComponentProperty(Component component, String propertyName, String propertyValue) {
        return component != null
                && component.getProperties() != null
                && component.getProperties().containsKey(propertyName)
                && component.getProperties().get(propertyName).equals(propertyValue);
    }

    private List<DataGroup> collectTaskRefDataGroups(DataRef taskRefField, Locale locale, Set<String> collectedTaskIds, int level, IUser user) {
        List<String> taskIds = ((TaskField) taskRefField.getField()).getRawValue();
        if (taskIds == null) {
            return new ArrayList<>();
        }

        List<DataGroup> groups = new ArrayList<>();
        taskIds.stream()
                .filter(id -> !collectedTaskIds.contains(id))
                .forEach(id -> {
                    collectedTaskIds.add(id);
                    List<DataGroup> taskRefDataGroups = getDataGroups(id, locale, collectedTaskIds, level + 1, taskRefField.getFieldId(), user).getData();
                    resolveTaskRefBehavior(taskRefField, taskRefDataGroups);
                    groups.addAll(taskRefDataGroups);
                });

        return groups;
    }

    private void resolveTaskRefOrderOnGrid(DataGroup dataGroup, Map<String, DataRef> dataFieldMap) {
        if (dataGroup.getLayout() == null || dataGroup.getLayout().getType() != LayoutType.GRID) {
            return;
        }
        dataGroup.setData(dataGroup.getData().stream().filter(dataFieldMap::containsKey).map(dataFieldMap::get).sorted(Comparator.comparingInt(a -> a.getLayout().getY())).map(DataRef::getFieldId).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private void resolveTaskRefBehavior(DataRef taskRefField, List<DataGroup> taskRefDataGroups) {
        if (taskRefField.getBehavior().getBehavior() == FieldBehavior.VISIBLE) {
            taskRefDataGroups.forEach(dataGroup -> {
                dataGroup.getDataRefs().values().forEach(field -> {
                    if (field.getBehavior().getBehavior() == FieldBehavior.EDITABLE) {
                        field.getBehavior().setBehavior(FieldBehavior.VISIBLE);
                    }
                });
            });
        } else if (taskRefField.getBehavior().getBehavior() == FieldBehavior.HIDDEN) {
            taskRefDataGroups.forEach(dataGroup -> {
                dataGroup.getDataRefs().values().forEach(field -> {
                    if (field.getBehavior().getBehavior() != FieldBehavior.FORBIDDEN) {
                        field.getBehavior().setBehavior(FieldBehavior.HIDDEN);
                    }
                });
            });
        }
    }

    @Override
    public FileFieldInputStream getFileByTask(String taskId, String fieldId, boolean forPreview) throws FileNotFoundException {
        Task task = taskService.findOne(taskId);
        FileFieldInputStream fileFieldInputStream = getFileByCase(task.getCaseId(), task, fieldId, forPreview);

        if (FileFieldInputStream.isEmpty(fileFieldInputStream)) {
            throw new FileNotFoundException("File in field " + fieldId + " within task " + taskId + " was not found!");
        }

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
        FileListField caseField = (FileListField) useCase.getDataSet().get(field.getStringId());
        if (caseField.getRawValue() == null) {
            return null;
        }

        workflowService.save(useCase);
        field.setRawValue(caseField.getRawValue());

        Optional<FileFieldValue> fileField = field.getRawValue().getNamesPaths().stream().filter(namePath -> namePath.getName().equals(name)).findFirst();
        if (fileField.isEmpty() || fileField.get().getPath() == null) {
            log.error("File {} not found!", name);
            return null;
        }

        try {
            return new FileFieldInputStream(field.isRemote() ? download(fileField.get().getPath()) : new FileInputStream(fileField.get().getPath()), name);
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
        // TODO: release/8.0.0 check params pass to other functions
        runGetActionsFromFileField(field.getEvents(), useCase, params);
        FileField caseField = (FileField) useCase.getDataSet().get(field.getStringId());
        if (caseField.getRawValue() == null) {
            return null;
        }

        workflowService.save(useCase);
        field.setRawValue(caseField.getRawValue());

        try {
            if (forPreview) {
                return getFilePreview(field, useCase);
            } else {
                return new FileFieldInputStream(field, field.isRemote() ? download(field.getValue().getValue().getPath()) : new FileInputStream(field.getValue().getValue().getPath()));
            }
        } catch (IOException e) {
            log.error("Getting file failed: ", e);
            return null;
        }
    }

    private void runGetActionsFromFileField(Map<DataEventType, DataEvent> events, Case useCase, Map<String, String> params) {
        // TODO: release/8.0.0
        // if (events != null && !events.isEmpty() && events.containsKey(DataEventType.GET)) {
        if (events == null || events.isEmpty() || !events.containsKey(DataEventType.GET)) {
            return;
        }
        DataEvent event = events.get(DataEventType.GET);
        event.getPreActions().forEach(action -> actionsRunner.run(action, useCase, params));
        event.getPostActions().forEach(action -> actionsRunner.run(action, useCase, params));
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
            file = new File(field.getValue().getValue().getPath());
        }
        int dot = file.getName().lastIndexOf(".");
        PreviewExtension fileType = PreviewExtension.resolveType((dot == -1) ? "" : file.getName().substring(dot + 1));
        BufferedImage image = getBufferedImageFromFile(file, fileType);
        if (image.getWidth() > imageScale || image.getHeight() > imageScale) {
            image = scaleImagePreview(image);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, !fileType.extension.equals(PreviewExtension.PDF.extension) ? fileType.extension : PreviewExtension.JPG.extension, os);
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

    private BufferedImage getBufferedImageFromFile(File file, PreviewExtension fileType) throws IOException {
        BufferedImage image;
        if (fileType.equals(PreviewExtension.PDF)) {
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
        InputStream is = download(field.getValue().getValue().getPath());
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
        Case useCase = workflowService.findOne(task.getCaseId());
        FileField field = (FileField) useCase.getDataSet().get(fieldId);

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
        List<EventOutcome> outcomes = new ArrayList<>();
        // TODO: release/8.0.0 changed value, use set data
        outcomes.addAll(resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(), DataEventType.SET, EventPhase.PRE, useCase, referencingTask, null, params));
        outcomes.addAll(resolveDataEvents(useCase.getPetriNet().getField(fieldId).get(), DataEventType.SET, EventPhase.POST, useCase, referencingTask, null, params));
        updateDataset(useCase);
        workflowService.save(useCase);
        return outcomes;
    }

    private boolean saveLocalFiles(Case useCase, FileListField field, MultipartFile[] multipartFiles) {
        for (MultipartFile oneFile : multipartFiles) {
            FileListFieldValue value = field.getRawValue();
            if (value != null && value.getNamesPaths() != null) {
                Optional<FileFieldValue> fileField = value.getNamesPaths().stream().filter(namePath -> namePath.getName().equals(oneFile.getOriginalFilename())).findFirst();
                if (fileField.isPresent()) {
                    new File(field.getFilePath(useCase.getStringId(), oneFile.getOriginalFilename())).delete();
                    value.getNamesPaths().remove(fileField.get());
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
        ((FileListField) useCase.getDataSet().get(field.getStringId())).setRawValue(field.getRawValue());
        return true;
    }

    private boolean saveLocalFile(Case useCase, FileField field, MultipartFile multipartFile) {
        if (useCase.getDataSet().get(field.getStringId()).getRawValue() != null) {
            new File(field.getFilePath(useCase.getStringId())).delete();
            useCase.getDataSet().get(field.getStringId()).setValue(null);
        }

        field.setValue(multipartFile.getOriginalFilename());
        field.getValue().getValue().setPath(field.getFilePath(useCase.getStringId()));
        File file = new File(field.getFilePath(useCase.getStringId()));
        try {
            writeFile(multipartFile, file);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new EventNotExecutableException("File " + multipartFile.getName() + " in case " + useCase.getStringId() + " could not be saved to file field " + field.getStringId(), e);
        }

        ((FileField) useCase.getDataSet().get(field.getStringId())).setRawValue(field.getValue().getValue());
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
    public SetDataEventOutcome deleteFile(String taskId, String fieldId) {
        return deleteFile(taskId, fieldId, new HashMap<>());
    }

    @Override
    public SetDataEventOutcome deleteFile(String taskId, String fieldId, Map<String, String> params) {
        Task task = taskService.findById(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        FileField field = (FileField) useCase.getDataSet().get(fieldId);

        if (useCase.getDataSet().get(field.getStringId()).getValue() != null) {
            if (field.isRemote()) {
                deleteRemote(useCase, field);
            } else {
                new File(field.getRawValue().getPath()).delete();
                new File(field.getFilePreviewPath(useCase.getStringId())).delete();
            }
            useCase.getDataSet().get(field.getStringId()).setValue(null);
        }
        // TODO: release/8.0.0 6.2.5
        return new SetDataEventOutcome(useCase, task, getChangedFieldByFileFieldContainer(fieldId, task, useCase, params));
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

        Optional<FileFieldValue> fileField = field.getValue().getValue().getNamesPaths().stream().filter(namePath -> namePath.getName().equals(name)).findFirst();

        if (fileField.isPresent()) {
            if (field.isRemote()) {
                deleteRemote(useCase, field, name);
            } else {
                new File(fileField.get().getPath()).delete();
                field.getValue().getValue().getNamesPaths().remove(fileField.get());
            }
            ((FileListField) useCase.getDataSet().get(field.getStringId())).setRawValue(field.getValue().getValue());
        }
        // TODO: release/8.0.0 6.2.5
        return new SetDataEventOutcome(useCase, task, getChangedFieldByFileFieldContainer(fieldId, task, useCase, params));
    }

    private ImmutablePair<Case, FileListField> getCaseAndFileListField(String taskId, String fieldId) {
        Task task = taskService.findOne(taskId);
        Case useCase = workflowService.findOne(task.getCaseId());
        FileListField field = (FileListField) useCase.getPetriNet().getDataSet().get(fieldId);
        field.setRawValue(((FileListFieldValue) useCase.getDataSet().get(field.getStringId()).getRawValue()));
        return new ImmutablePair<>(useCase, field);
    }

    @Override
    public Page<Task> setImmediateFields(Page<Task> tasks) {
        tasks.getContent().forEach(task -> task.setImmediateData(getImmediateFields(task)));
        return tasks;
    }

    @Override
    public List<Field<?>> getImmediateFields(Task task) {
        Case useCase = workflowService.findOne(task.getCaseId());
        List<Field<?>> fields = task.getImmediateDataFields().stream().map(f -> useCase.getDataSet().get(f)).collect(Collectors.toList());
//        TODO: release/8.0.0 order?
//        LongStream.range(0L, fields.size()).forEach(index -> fields.get((int) index).setOrder(index));
        return fields;
    }

    private void updateDataset(Case useCase) {
        Case actual = workflowService.findOne(useCase.getStringId());
        actual.getDataSet().getFields().forEach((id, dataField) -> {
            if (dataField.isNewerThen(useCase.getDataSet().get(id))) {
                useCase.getDataSet().put(id, dataField);
            }
        });
    }

    private List<EventOutcome> resolveDataEvents(Field<?> field, DataEventType trigger, EventPhase phase, Case useCase, Task task, Field<?> newDataField, Map<String, String> params) {
        return eventService.processDataEvents(field, trigger, phase, useCase, task, newDataField, params);
    }

    @Override
    public UserFieldValue makeUserFieldValue(String id) {
        IUser user = userService.resolveById(id);
        return new UserFieldValue(user.getStringId(), user.getName(), user.getSurname(), user.getEmail());
    }

    @Override
    public Case applyFieldConnectedChanges(Case useCase, Field<?> field) {
        Field<?> caseField = useCase.getDataSet().get(field.getStringId());
        if (!(caseField instanceof UserListField)) {
            return useCase;
        }
        return workflowService.resolveUserRef(useCase);
    }

    // TODO: release/8.0.0 change component properties, parse object node

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
