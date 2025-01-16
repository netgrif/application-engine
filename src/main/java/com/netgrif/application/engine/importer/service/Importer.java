package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.configuration.LayoutFlexConfiguration;
import com.netgrif.application.engine.configuration.LayoutGridConfiguration;
import com.netgrif.application.engine.importer.model.*;
import com.netgrif.application.engine.importer.model.DataRef;
import com.netgrif.application.engine.importer.model.Scope;
import com.netgrif.application.engine.importer.service.evaluation.IActionEvaluator;
import com.netgrif.application.engine.importer.service.evaluation.IFunctionEvaluator;
import com.netgrif.application.engine.importer.service.outcome.ProcessImportResult;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.importer.service.trigger.TriggerFactory;
import com.netgrif.application.engine.importer.service.validation.IProcessValidator;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.domain.Component;
import com.netgrif.application.engine.workflow.domain.Function;
import com.netgrif.application.engine.workflow.domain.Place;
import com.netgrif.application.engine.workflow.domain.Transition;
import com.netgrif.application.engine.workflow.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.dataset.logic.Expression;
import com.netgrif.application.engine.workflow.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.workflow.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.workflow.domain.events.BaseEvent;
import com.netgrif.application.engine.workflow.domain.events.CaseEvent;
import com.netgrif.application.engine.workflow.domain.events.DataEvent;
import com.netgrif.application.engine.workflow.domain.events.Event;
import com.netgrif.application.engine.workflow.domain.events.ProcessEvent;
import com.netgrif.application.engine.petrinet.domain.layout.LayoutContainer;
import com.netgrif.application.engine.petrinet.domain.layout.LayoutItem;
import com.netgrif.application.engine.petrinet.domain.layout.LayoutObjectType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.workflow.domain.throwable.MissingProcessMetaDataException;
import com.netgrif.application.engine.workflow.domain.Version;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.importer.model.Process;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Importer {

    public static final String FILE_EXTENSION = ".xml";

    @Getter
    protected Process importedProcess;
    @Getter
    protected ProcessImportResult result;

    protected ProcessRole defaultRole;
    protected ProcessRole anonymousRole;
    protected boolean isDefaultRoleEnabled = false;
    protected boolean isAnonymousRoleEnabled = false;

    protected Map<String, I18nString> i18n;
    protected List<String> missingMetaData;
    protected List<Action> actions;

    @Autowired
    protected AllDataConfiguration allDataConfiguration;

    @Autowired
    protected FieldFactory fieldFactory;

    @Autowired
    protected IPetriNetService service;

    @Autowired
    protected IProcessRoleService processRoleService;

    @Autowired
    protected IPetriNetService petriNetService;

    @Autowired
    protected ArcImporter arcImporter;

    @Autowired
    protected PermissionFactory permissionFactory;

    @Autowired
    protected TriggerFactory triggerFactory;

    @Autowired
    protected IActionEvaluator actionEvaluator;

    @Autowired
    protected IFunctionEvaluator functionEvaluator;

    @Autowired
    protected IProcessValidator processValidator;

    @Autowired
    private LayoutFlexConfiguration flexConfiguration;

    @Autowired
    private LayoutGridConfiguration gridConfiguration;

    public Importer() {
        this.i18n = new HashMap<>();
        this.missingMetaData = new ArrayList<>();
        this.actions = new LinkedList<>();
    }

    /**
     * todo javadoc
     * */
    public ProcessImportResult importProcess(InputStream xml) throws MissingProcessMetaDataException, MissingIconKeyException {
        try {
            initialize();
            unmarshallXml(xml);
            return handleUnmarshalledProcess();
        } catch (JAXBException e) {
            log.error("Importing Petri net failed: ", e);
            result.deleteCases();
            return result;
        }
    }

    public Process unmarshallXml(InputStream xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Process.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        importedProcess = (Process) jaxbUnmarshaller.unmarshal(xml);
        return importedProcess;
    }

    protected void initialize() {
        this.defaultRole = processRoleService.defaultRole();
        this.anonymousRole = processRoleService.anonymousRole();
        this.result = ProcessImportResult.EMPTY;
    }

    protected ProcessImportResult handleUnmarshalledProcess() throws MissingProcessMetaDataException, MissingIconKeyException {
        // todo 2026 generalization

        importedProcess.getI18N().forEach(this::addI18N);

        setMetaData();
        addAllDataTransition();

        importedProcess.getRole().forEach(this::createRole);
        importedProcess.getData().forEach(this::createDataSet);
        importedProcess.getPlace().forEach(this::createPlace);
        importedProcess.getTransition().forEach(this::createTransition);
        importedProcess.getArc().forEach(this::createArc);
        importedProcess.getFunction().forEach(this::createFunction);
        importedProcess.getRoleRef().forEach(this::createProcessPermissions);

        addPredefinedRolesWithDefaultPermissions();

        if (importedProcess.getProcessEvents() != null) {
            importedProcess.getProcessEvents().getEvent().forEach(this::createProcessEvent);
        }
        if (importedProcess.getCaseEvents() != null) {
            importedProcess.getCaseEvents().getEvent().forEach(this::createCaseEvent);
        }

        functionEvaluator.evaluate(result.getTemplateCase().getFunctions());
        functionEvaluator.evaluate(result.getProcessScopedCase().getFunctions());
        actionEvaluator.evaluate(actions, result.getTemplateCase().getFunctions());
        // todo 2026 action evaluator for processScopedCase?
//        processValidator.validate(result.getTemplateCase());

        return result;
    }

    protected void initializeWithChildPetriNet(Extension extension) {
        // todo 2026 generalization
//        if (areExtensionAttributesEmpty(extension)) {
//            throw new IllegalArgumentException("Parent identifier or version is empty.");
//        }
//        Process parentNet = petriNetService.getPetriNet(extension.getId(), Version.of(extension.getVersion()));
//        if (parentNet == null) {
//            throw new IllegalArgumentException("Parent petri net not found.");
//        }
//        process = parentNet.clone();
//        process.setCreationDate(LocalDateTime.now());
//        // transition must be removed since it's going to be added later with proper data
//        process.getTransitions().remove(allDataConfiguration.getAllData().getId());
//        process.setObjectId(new ObjectId());
//        process.addParentIdentifier(new PetriNetIdentifier(
//                parentNet.getIdentifier(),
//                parentNet.getVersion(),
//                parentNet.getObjectId()
//        ));
//        UniqueKeyMap<String, ProcessRole> processRolesWithNewIds = new UniqueKeyMap<>();
//        for (Map.Entry<String, ProcessRole> entry : process.getRoles().entrySet()) {
//            ObjectId newId = new ObjectId();
//            entry.getValue().setId(newId);
//            processRolesWithNewIds.put(newId.toString(), entry.getValue());
//        }
//        process.setRoles(processRolesWithNewIds);
    }

    protected static boolean areExtensionAttributesEmpty(Extension extension) {
        return extension.getId() == null || extension.getId().isBlank()
                || extension.getVersion() == null || extension.getVersion().isBlank();
    }

    protected void addI18N(com.netgrif.application.engine.importer.model.I18N importI18N) {
        String locale = importI18N.getLocale();
        importI18N.getI18NString().forEach(translation -> addTranslation(translation, locale));
    }

    protected void addTranslation(com.netgrif.application.engine.importer.model.I18NStringType i18NStringType, String locale) {
        String id = i18NStringType.getId();
        I18nString translation = i18n.computeIfAbsent(id, k -> new I18nString());
        translation.addTranslation(locale, i18NStringType.getValue());
    }

    public I18nString toI18NString(com.netgrif.application.engine.importer.model.I18NStringType imported) {
        if (imported == null) {
            return new I18nString("");
        }
        String key = imported.getValue();
        if (imported.getId() != null) {
            key = imported.getId();
        }
        if (imported instanceof com.netgrif.application.engine.importer.model.Option) {
            key = ((com.netgrif.application.engine.importer.model.Option) imported).getKey();
        }
        I18nString string = i18n.getOrDefault(imported.getId(), new I18nString(key, imported.getValue()));
        if (string.getDefaultValue() == null) {
            string.setDefaultValue(imported.getValue());
        }
        return string;
    }

    protected void setMetaData() throws MissingProcessMetaDataException {
        Case templateCase = result.getTemplateCase();
        checkMetaData(importedProcess.getId(), () -> {
            templateCase.setProcessIdentifier(importedProcess.getId());
        }, "<id>");
        checkMetaData(importedProcess.getVersion(), () -> {
            templateCase.setVersion(Version.of(importedProcess.getVersion()));
        }, "<version>");
        // todo 2026 ako vyuzit schema title? -> podla mna do process case-u
//        checkMetaData(importedProcess.getTitle(), () -> {
//            templateCase.setTitle(toI18NString(importedProcess.getTitle()));
//        }, "<title>");

        templateCase.setIcon(importedProcess.getIcon());

        if (importedProcess.getCaseName() != null) {
            I18nString caseTitle = new I18nString();
            if (importedProcess.getCaseName().isDynamic()) {
                caseTitle.setExpression(Expression.ofDynamic(importedProcess.getCaseName().getValue()));
            } else {
                caseTitle.setDefaultValue(importedProcess.getCaseName().getValue());
                caseTitle.setKey(importedProcess.getCaseName().getId());
                if (i18n.containsKey(caseTitle.getKey())) {
                    caseTitle.setTranslations(i18n.get(caseTitle.getKey()).getTranslations());
                }
            }
            templateCase.setTitle(caseTitle);
        } else {
            // todo 2026 preklady
            templateCase.setTitle(new I18nString(importedProcess.getTitle().getValue()));
        }
        createProperties(importedProcess.getProperties(), templateCase.getProperties());

        // TODO: release/8.0.0 rework
        this.isDefaultRoleEnabled = (importedProcess.isDefaultRole() != null && importedProcess.isDefaultRole());
        this.isAnonymousRoleEnabled = (importedProcess.isAnonymousRole() != null && importedProcess.isAnonymousRole());

        if (!missingMetaData.isEmpty()) {
            result.addToErrors(missingMetaData);
            throw new MissingProcessMetaDataException(missingMetaData);
        }
    }

    protected void checkMetaData(Object metadata, Runnable onPresent, String metadataTag) {
        if (metadata != null) {
            onPresent.run();
        } else {
            missingMetaData.add(metadataTag);
        }
    }

    protected void addAllDataTransition() {
        // TODO: release/8.0.0 extend default Object process
        com.netgrif.application.engine.importer.model.Transition allDataConfig = allDataConfiguration.getAllData();
        if (importedProcess.getTransition().stream().anyMatch(transition -> allDataConfig.getId().equals(transition.getId()))) {
            return;
        }
        com.netgrif.application.engine.importer.model.Transition allDataTransition = new com.netgrif.application.engine.importer.model.Transition();
        // TODO: release/8.0.0 merge forms from NAE-1966
        allDataTransition.setId(allDataConfig.getId());
        allDataTransition.setX(allDataConfig.getX());
        allDataTransition.setY(allDataConfig.getY());
        allDataTransition.setTitle(allDataConfig.getTitle());
        allDataTransition.setIcon(allDataConfig.getIcon());
        allDataTransition.setAssignPolicy(allDataConfig.getAssignPolicy());
        allDataTransition.setFinishPolicy(allDataConfig.getFinishPolicy());
        // TODO: release/8.0.0 all properties
        FlexContainer flex = new FlexContainer();
        for (com.netgrif.application.engine.importer.model.Data field : importedProcess.getData()) {
            FlexItem flexItem = new FlexItem();
            com.netgrif.application.engine.importer.model.DataRef dataRef = new com.netgrif.application.engine.importer.model.DataRef();

            dataRef.setId(field.getId());
            com.netgrif.application.engine.importer.model.DataRefLogic logic = new com.netgrif.application.engine.importer.model.DataRefLogic();
            logic.setBehavior(com.netgrif.application.engine.importer.model.Behavior.EDITABLE);
            dataRef.setLogic(logic);

            flexItem.setDataRef(dataRef);
            flex.getItem().add(flexItem);
        }
        allDataTransition.setFlex(flex);
        importedProcess.getTransition().add(allDataTransition);
    }

//    private void addActionsToEvent(List<Action> actions, com.netgrif.application.engine.importer.model.DataEventType type, Map<com.netgrif.application.engine.importer.model.DataEventType, DataEvent> events) {
//        if (actions.isEmpty()) return;
//        if (events.get(type) != null) {
//            events.get(type).addToActionsByDefaultPhase(actions);
//            return;
//        }
//        events.computeIfAbsent(type, k -> {
//            DataEvent event = new DataEvent();
//            event.setType(type);
//            event.addToActionsByDefaultPhase(actions);
//            event.setId(new ObjectId().toString());
//            return event;
//        });
//    }
//
//    protected void resolveDataRefActions(List<DataRef> dataRef, com.netgrif.application.engine.importer.model.Transition trans) {
//        dataRef.forEach(ref -> {
//            String fieldId = getField(ref.getId()).getStringId();
//            Map<com.netgrif.application.engine.importer.model.DataEventType, DataEvent> dataEvents = new HashMap<>();
//            List<Action> getActions = new ArrayList<>();
//            List<Action> setActions = new ArrayList<>();
//            if (ref.getEvent() != null && !ref.getEvent().isEmpty()) {
//                dataEvents = buildEvents(fieldId, ref.getEvent(), getTransition(trans.getId()).getStringId());
//                getTransition(trans.getId()).setDataEvents(fieldId, dataEvents);
//            }
//            if (ref.getLogic().getAction() != null) {
//                getActions = buildActions(filterActionsByTrigger(ref.getLogic().getAction(), com.netgrif.application.engine.importer.model.DataEventType.GET),
//                        fieldId, getTransition(trans.getId()).getStringId());
//                setActions = buildActions(filterActionsByTrigger(ref.getLogic().getAction(), com.netgrif.application.engine.importer.model.DataEventType.SET),
//                        fieldId, getTransition(trans.getId()).getStringId());
//            }
//            if (ref.getLogic().getActionRef() != null) {
//                List<Action> fromActionRefs = buildActionRefs(ref.getLogic().getActionRef());
//                getActions.addAll(fromActionRefs.stream()
//                        .filter(action -> action.isTriggeredBy(com.netgrif.application.engine.importer.model.DataEventType.GET)).collect(Collectors.toList()));
//                setActions.addAll(fromActionRefs.stream()
//                        .filter(action -> action.isTriggeredBy(com.netgrif.application.engine.importer.model.DataEventType.SET)).collect(Collectors.toList()));
//            }
//
//            addActionsToDataEvent(getActions, dataEvents, com.netgrif.application.engine.importer.model.DataEventType.GET);
//            addActionsToDataEvent(setActions, dataEvents, com.netgrif.application.engine.importer.model.DataEventType.SET);
//            getTransition(trans.getId()).setDataEvents(fieldId, dataEvents);
//        });
//    }
//
//    protected void addActionsToDataEvent(List<Action> actions, Map<com.netgrif.application.engine.importer.model.DataEventType, DataEvent> dataEvents, com.netgrif.application.engine.importer.model.DataEventType type) {
//        if (!dataEvents.containsKey(type) || dataEvents.get(type).getId() == null) {
//            dataEvents.put(type, createDefaultEvent(actions, type));
//        } else {
//            dataEvents.get(type).addToActionsByDefaultPhase(actions);
//        }
//    }
//
    protected void createArc(com.netgrif.application.engine.importer.model.Arc importArc) {
        com.netgrif.application.engine.workflow.domain.arcs.Arc<?, ?> arc = arcImporter.getArc(importArc, this);
        if (arc.getScope() != null && arc.getScope().equals(Scope.PROCESS)) {
            result.getProcessScopedCase().addArc(arc);
        } else if (arc.getScope() == null || arc.getScope().equals(Scope.USE_CASE)) {
            result.getTemplateCase().addArc(arc);
        } else {
            result.addToErrors(String.format("Arc [%s] has invalid scope", arc.getImportId()));
        }
    }

    protected boolean isInputArc(com.netgrif.application.engine.importer.model.Arc importArc) {
        return result.getTemplateCase().getPlace(importArc.getSourceId()) != null
                || result.getProcessScopedCase().getPlace(importArc.getSourceId()) != null;
    }

    protected Place getPlace(String id) {
        Place place = result.getTemplateCase().getPlace(id);
        if (place == null) {
            place = result.getProcessScopedCase().getPlace(id);
        }
        if (place == null) {
            throw new IllegalArgumentException("Place with id [" + id + "] not found.");
        }
        return place;
    }

    protected Transition getTransition(String id) {
        Transition transition = result.getTemplateCase().getTransition(id);
        if (transition == null) {
            transition = result.getProcessScopedCase().getTransition(id);
        }
        if (transition == null) {
            throw new IllegalArgumentException("Transition with id [" + id + "] not found.");
        }
        return transition;
    }

    protected void createDataSet(com.netgrif.application.engine.importer.model.Data importData) throws MissingIconKeyException {
        Field<?> field = fieldFactory.getField(importData, this);
        if (importData.getEvent() != null) {
            importData.getEvent().forEach(event -> field.addEvent(createDataEvent(event)));
        }
        createProperties(importData.getProperties(), field.getProperties());
        if (field.getScope() != null && field.getScope().equals(Scope.PROCESS)) {
            result.getProcessScopedCase().addDataSetField(field);
        } else if (field.getScope() == null || field.getScope().equals(Scope.USE_CASE)) {
            result.getTemplateCase().addDataSetField(field);
        } else {
            result.addToErrors(String.format("Field [%s] has invalid scope", field.getImportId()));
        }
    }

    protected void createTransition(com.netgrif.application.engine.importer.model.Transition importTransition) throws MissingIconKeyException {
        Transition transition = new Transition();
        transition.setImportId(importTransition.getId());
        transition.setTitle(toI18NString(importTransition.getTitle()));
        createProperties(importTransition.getProperties(), transition.getProperties());
        transition.setIcon(importTransition.getIcon());
        transition.setAssignPolicy(toAssignPolicy(importTransition.getAssignPolicy()));
        transition.setFinishPolicy(toFinishPolicy(importTransition.getFinishPolicy()));
        transition.setScope(importTransition.getScope());
        if (importTransition.getRoleRef() != null) {
            importTransition.getRoleRef().forEach(roleRef ->
                    createTaskPermissions(transition, roleRef)
            );
        }
        if (importTransition.getTrigger() != null) {
            importTransition.getTrigger().forEach(trigger ->
                    createTrigger(transition, trigger)
            );
        }

        addPredefinedRolesWithDefaultPermissions(importTransition, transition);

        if (importTransition.getEvent() != null) {
            importTransition.getEvent().forEach(event -> transition.addEvent(createEvent(event)));
        }
        resolveLayoutContainer(importTransition, transition);

        if (transition.getScope() != null && transition.getScope().equals(Scope.PROCESS)) {
            result.getProcessScopedCase().addTransition(transition);
        } else if (transition.getScope() == null || transition.getScope().equals(Scope.USE_CASE)) {
            result.getTemplateCase().addTransition(transition);
        } else {
            result.addToErrors(String.format("Transition [%s] has invalid scope", transition.getImportId()));
        }
    }

    protected Event createEvent(com.netgrif.application.engine.importer.model.Event importedEvent) {
        Event event = new Event();
        event.setTitle(toI18NString(importedEvent.getTitle()));
        event.setType(com.netgrif.application.engine.importer.model.EventType.valueOf(importedEvent.getType().value().toUpperCase()));
        this.addBaseEventProperties(event, importedEvent);
        return event;
    }

    protected void addBaseEventProperties(BaseEvent event, com.netgrif.application.engine.importer.model.BaseEvent imported) {
        event.setImportId(imported.getId());
        event.setMessage(toI18NString(imported.getMessage()));
        event.setPreActions(parsePhaseActions(com.netgrif.application.engine.importer.model.EventPhaseType.PRE, imported));
        event.setPostActions(parsePhaseActions(com.netgrif.application.engine.importer.model.EventPhaseType.POST, imported));
    }

    protected List<Action> parsePhaseActions(com.netgrif.application.engine.importer.model.EventPhaseType phase, com.netgrif.application.engine.importer.model.BaseEvent imported) {
        return imported.getActions().stream()
                .filter(actions -> actions.getPhase().equals(phase))
                .flatMap(actions -> actions.getAction().stream().map(this::createAction))
                .collect(Collectors.toList());
    }

    protected Action createAction(com.netgrif.application.engine.importer.model.Action importedAction) {
        Action action = new Action();
        action.setImportId(buildActionId(importedAction.getId()));
        action.setDefinition(importedAction.getValue());
        if (importedAction.getType() != null) {
            // TODO: release/8.0.0 add atribute "type" to data set actions
        }
        return action;
    }

    protected DataEvent createDataEvent(com.netgrif.application.engine.importer.model.DataEvent importedEvent) {
        DataEvent event = new DataEvent();
        event.setType(DataEventType.valueOf(importedEvent.getType().value().toUpperCase()));
        this.addBaseEventProperties(event, importedEvent);
        return event;
    }

    // TODO: release/8.0.0 refactor common event creation
    protected void createProcessEvent(com.netgrif.application.engine.importer.model.ProcessEvent importedEvent) {
        if (importedEvent == null) {
            return;
        }
        ProcessEvent event = new ProcessEvent();
        event.setType(ProcessEventType.valueOf(importedEvent.getType().value().toUpperCase()));
        this.addBaseEventProperties(event, importedEvent);
        result.getTemplateCase().addProcessEvent(event);
    }

    protected void createCaseEvent(com.netgrif.application.engine.importer.model.CaseEvent importedEvent) {
        if (importedEvent == null) {
            return;
        }
        CaseEvent event = new CaseEvent();
        event.setType(CaseEventType.valueOf(importedEvent.getType().value().toUpperCase()));
        this.addBaseEventProperties(event, importedEvent);
        result.getTemplateCase().addCaseEvent(event);
    }

    protected void createFunction(com.netgrif.application.engine.importer.model.Function importedFunction) {
        Function function = new Function();
        function.setDefinition(importedFunction.getValue());
        function.setName(importedFunction.getName());
        function.setScope(importedFunction.getScope());
        if (function.getScope() != null && function.getScope().equals(Scope.PROCESS)) {
            result.getProcessScopedCase().addFunction(function);
        } else if (function.getScope() == null || function.getScope().equals(Scope.USE_CASE)) {
            result.getTemplateCase().addFunction(function);
        } else {
            result.addToErrors(String.format("Function [%s] has invalid scope", function.getImportId()));
        }
    }

    protected void addPredefinedRolesWithDefaultPermissions() {
        if (isAnyPermissionTrue()) {
            return;
        }
        addDefaultRole();
        addAnonymousRole();
    }

    /**
     * todo javadoc
     *         // only if no positive role associations and no positive user ref associations
     * */
    protected boolean isAnyPermissionTrue() {
        return result.getTemplateCase().getPermissions().values().stream()
                .anyMatch(perms -> perms.containsValue(true));
    }

    protected void addPredefinedRolesWithDefaultPermissions(com.netgrif.application.engine.importer.model.Transition importTransition, Transition transition) {
        // Don't add if positive roles or triggers or positive user refs
        if ((importTransition.getRoleRef() != null && importTransition.getRoleRef().stream().anyMatch(this::hasPositivePermission))
                || (importTransition.getTrigger() != null && !importTransition.getTrigger().isEmpty())) {
            return;
        }
        addDefaultRole(transition);
        addAnonymousRole(transition);
    }

    protected void addDefaultRole() {
        addSystemRole(isDefaultRoleEnabled, defaultRole);
    }

    protected void addDefaultRole(Transition transition) {
        addSystemRole(transition, isDefaultRoleEnabled, defaultRole);
    }

    protected void addAnonymousRole() {
        addSystemRole(isAnonymousRoleEnabled, anonymousRole);
    }

    protected void addAnonymousRole(Transition transition) {
        addSystemRole(transition, isAnonymousRoleEnabled, anonymousRole);
    }

    protected void addSystemRole(boolean isEnabled, ProcessRole role) {
        if (!isEnabled || result.getTemplateCase().getPermissions().containsKey(role.getStringId())) {
            return;
        }

        com.netgrif.application.engine.importer.model.CaseLogic logic = new com.netgrif.application.engine.importer.model.CaseLogic();
        logic.setCreate(true);
        logic.setDelete(true); // TODO: release/8.0.0 anonymous can delete
        logic.setView(true);
        result.getTemplateCase().addPermission(role.getStringId(), permissionFactory.buildProcessPermissions(logic));
    }

    protected void addSystemRole(Transition transition, boolean isEnabled, ProcessRole role) {
        if (!isEnabled || transition.getPermissions().containsKey(role.getStringId())) {
            return;
        }

        com.netgrif.application.engine.importer.model.RoleRefLogic logic = new com.netgrif.application.engine.importer.model.RoleRefLogic();
        logic.setPerform(true);
        transition.addPermission(role.getStringId(), permissionFactory.buildTaskPermissions(logic));
    }

    protected void createProcessPermissions(com.netgrif.application.engine.importer.model.CaseRoleRef roleRef) {
        com.netgrif.application.engine.importer.model.CaseLogic logic = roleRef.getCaseLogic();
        ProcessRole role = result.getTemplateCase().getRole(roleRef.getId());
        if (logic == null || role == null) {
            // TODO: release/8.0.0 warn
            return;
        }
        result.getTemplateCase().addPermission(role.getStringId(), permissionFactory.buildProcessPermissions(logic));
    }

    protected void createTaskPermissions(Transition transition, com.netgrif.application.engine.importer.model.RoleRef roleRef) {
        com.netgrif.application.engine.importer.model.RoleRefLogic logic = roleRef.getLogic();
        ProcessRole role = result.getTemplateCase().getRole(roleRef.getId());
        if (logic == null || role == null) {
            // TODO: release/8.0.0 warn
            return;
        }
        transition.addPermission(role.getStringId(), permissionFactory.buildTaskPermissions(logic));
    }

    protected boolean hasPositivePermission(com.netgrif.application.engine.importer.model.PermissionRef permissionRef) {
        RoleRefLogic logic = permissionRef.getLogic();
        return isAnyTrue(
                logic.isPerform(),
                logic.isView(),
                logic.isAssign(),
                logic.isCancel(),
                logic.isFinish(),
                logic.isReassign(),
                logic.isViewDisabled()
        );
    }

    protected boolean isAnyTrue(Boolean... permissions) {
        return Arrays.stream(permissions).anyMatch(this::isTrue);
    }

    protected String buildActionId(String actionId) {
        if (actionId == null) {
            actionId = new ObjectId().toString();
        }
        // TODO: release/8.0.0 optimize ids of actions to not use strings
        return result.getTemplateCase().getProcessIdentifier() + "-" + actionId;
    }

    protected void createTrigger(Transition transition, com.netgrif.application.engine.importer.model.Trigger importTrigger) {
        transition.addTrigger(triggerFactory.buildTrigger(importTrigger));
    }

    protected void createPlace(com.netgrif.application.engine.importer.model.Place importPlace) {
        Place place = new Place();
        place.setImportId(importPlace.getId());
        place.setTokens(importPlace.getTokens());
        place.setTitle(toI18NString(importPlace.getTitle()));
        place.setScope(importPlace.getScope());
        // TODO: release/8.0.0 scope
        createProperties(importPlace.getProperties(), place.getProperties());

        if (place.getScope() != null && place.getScope().equals(Scope.PROCESS)) {
            result.getProcessScopedCase().addPlace(place);
        } else if (place.getScope() == null || place.getScope().equals(Scope.USE_CASE)) {
            result.getTemplateCase().addPlace(place);
        } else {
            result.addToErrors(String.format("Place [%s] has invalid scope", place.getImportId()));
        }
    }

    protected void createRole(com.netgrif.application.engine.importer.model.Role importRole) {
        if (importRole.getId().equals(ProcessRole.DEFAULT_ROLE)) {
            throw new IllegalArgumentException("Role ID '" + ProcessRole.DEFAULT_ROLE + "' is a reserved identifier, roles with this ID cannot be defined!");
        }
        if (importRole.getId().equals(ProcessRole.ANONYMOUS_ROLE)) {
            throw new IllegalArgumentException("Role ID '" + ProcessRole.ANONYMOUS_ROLE + "' is a reserved identifier, roles with this ID cannot be defined!");
        }

        ProcessRole role = new ProcessRole();
        role.setImportId(importRole.getId());
        role.setName(toI18NString(importRole.getTitle()));
        role.setScope(importRole.getScope());
        if (importRole.getEvent() != null) {
            importRole.getEvent().forEach(event -> role.addEvent(createEvent(event)));
        }

        // todo 2026 roles
        if (importRole.getScope() != null && !importRole.getScope().equals(Scope.PROCESS)) {
            result.addToErrors(String.format("Role [%s] has not valid scope.", importRole.getId()));
            return;
        }
        role.setNetId(result.getProcessScopedCase().getStringId());
        result.getProcessScopedCase().addRole(role);
    }

    protected com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy toAssignPolicy(com.netgrif.application.engine.importer.model.AssignPolicy policy) {
        if (policy == null || policy.value() == null) {
            return com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy.MANUAL;
        }

        return com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy.valueOf(policy.value().toUpperCase());
    }

    protected com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy toFinishPolicy(com.netgrif.application.engine.importer.model.FinishPolicy policy) {
        if (policy == null || policy.value() == null) {
            return com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy.MANUAL;
        }

        return com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy.valueOf(policy.value().toUpperCase());
    }

    protected void createProperties(com.netgrif.application.engine.importer.model.Properties propertiesXml, UniqueKeyMap<String, String> properties) {
        if (propertiesXml == null) {
            return;
        }
        propertiesXml.getProperty().forEach(property -> properties.put(property.getKey(), property.getValue()));
    }

    protected void resolveLayoutContainer(com.netgrif.application.engine.importer.model.Transition importTransition, Transition transition) {
        if (importTransition.getFlex() != null && importTransition.getGrid() != null) {
            throw new IllegalArgumentException("Found Flex and Grid container together in Transition {" + importTransition.getId() + "}");
        }
        if (importTransition.getFlex() != null) {
            transition.setLayoutContainer(getFlexLayoutContainer(importTransition.getFlex(), transition, 0));
        }
        if (importTransition.getGrid() != null) {
            transition.setLayoutContainer(getGridLayoutContainer(importTransition.getGrid(), transition, 0));
        }
    }

    protected LayoutContainer getFlexLayoutContainer(FlexContainer importedFlexContainer, Transition transition, int depth) {
        LayoutContainer layoutContainer = new LayoutContainer();
        layoutContainer.setImportId(importedFlexContainer.getId());
        layoutContainer.setLayoutType(LayoutObjectType.FLEX);

        Map<String, String> layoutContainerProperties = new HashMap<>(depth == 0 ? flexConfiguration.getRoot() : flexConfiguration.getContainer());
        if (importedFlexContainer.getProperties() != null) {
            for (java.lang.reflect.Field containerPropertyField : importedFlexContainer.getProperties().getClass().getDeclaredFields()) {
                try {
                    resolveFieldProperty(containerPropertyField, layoutContainerProperties, importedFlexContainer.getProperties());
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Unexpected property in Flex Container {" + importedFlexContainer.getId() + "}");
                }
            }
        }
        layoutContainer.setProperties(layoutContainerProperties);

        for (FlexItem flexItem : importedFlexContainer.getItem()) {
            layoutContainer.addLayoutItem(getLayoutItem(importedFlexContainer.getId(), flexItem, transition, depth));
        }

        return layoutContainer;
    }

    protected LayoutContainer getGridLayoutContainer(GridContainer importedGridContainer, Transition transition, int depth) {
        LayoutContainer layoutContainer = new LayoutContainer();
        layoutContainer.setImportId(importedGridContainer.getId());
        layoutContainer.setLayoutType(LayoutObjectType.GRID);

        Map<String, String> layoutProperties = new HashMap<>(depth == 0 ? gridConfiguration.getRoot() : gridConfiguration.getContainer());
        if (importedGridContainer.getProperties() != null) {
            for (java.lang.reflect.Field field : importedGridContainer.getProperties().getClass().getDeclaredFields()) {
                try {
                    resolveFieldProperty(field, layoutProperties, importedGridContainer.getProperties());
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Unexpected property in Grid Container {" + importedGridContainer.getId() + "}");
                }
            }
        }
        layoutContainer.setProperties(layoutProperties);

        for (GridItem gridItem : importedGridContainer.getItem()) {
            layoutContainer.addLayoutItem(getLayoutItem(importedGridContainer.getId(), gridItem, transition, depth));
        }

        return layoutContainer;
    }

    private LayoutItem getLayoutItem(String containerId, com.netgrif.application.engine.importer.model.LayoutItem importedLayoutItem, Transition transition, int depth) {
        if (BooleanUtils.toInteger(importedLayoutItem.getFlex() != null) + BooleanUtils.toInteger(importedLayoutItem.getGrid() != null) + BooleanUtils.toInteger(importedLayoutItem.getDataRef() != null) > 1) {
            throw new IllegalArgumentException("Found Flex/Grid/DataRef together in Layout Container {" + containerId + "}");
        }
        LayoutItem layoutItem = new LayoutItem();
        layoutItem.setLayoutType(importedLayoutItem instanceof GridItem ? LayoutObjectType.GRID : LayoutObjectType.FLEX);

        boolean isFlex = importedLayoutItem instanceof FlexItem;
        Map<String, String> layoutItemProperties = isFlex ? new HashMap<>(flexConfiguration.getChildren()) : new HashMap<>(gridConfiguration.getChildren());
        Object itemProperties = !isFlex ? ((GridItem) importedLayoutItem).getProperties() : ((FlexItem) importedLayoutItem).getProperties();
        if (itemProperties != null) {
            for (java.lang.reflect.Field itemPropertyField : itemProperties.getClass().getDeclaredFields()) {
                try {
                    resolveFieldProperty(itemPropertyField, layoutItemProperties, itemProperties);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Unexpected property in Grid Item of Grid Container {" + containerId + "}");
                }
            }
        }
        layoutItem.setProperties(layoutItemProperties);

        if (importedLayoutItem.getFlex() != null) {
            layoutItem.setContainer(getFlexLayoutContainer(importedLayoutItem.getFlex(), transition, depth + 1));
        } else if (importedLayoutItem.getGrid() != null) {
            layoutItem.setContainer(getGridLayoutContainer(importedLayoutItem.getGrid(), transition, depth + 1));
        } else if (importedLayoutItem.getDataRef() != null) {
            layoutItem.setDataRefId(resolveDataRef(importedLayoutItem.getDataRef(), transition).getFieldId());
        }
        return layoutItem;
    }

    protected void resolveFieldProperty(java.lang.reflect.Field field, Map<String, String> layoutProperties, Object containerProperties)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String propertyName = field.getAnnotation(XmlElement.class) == null || field.getAnnotation(XmlElement.class).name().equals("##default")
                ? field.getName()
                : field.getAnnotation(XmlElement.class).name();
        field.setAccessible(true);
        if (field.get(containerProperties) == null) {
            return;
        }
        if (field.getType().equals(String.class) || field.getType().equals(Integer.class)) {
            layoutProperties.put(propertyName, field.get(containerProperties).toString());
        } else {
            Method valueMethod = field.get(containerProperties).getClass().getMethod("value");
            layoutProperties.put(propertyName, valueMethod.invoke(field.get(containerProperties)).toString());
        }
    }

    protected com.netgrif.application.engine.workflow.domain.DataRef resolveDataRef(com.netgrif.application.engine.importer.model.DataRef importedDataRef, Transition transition) {
        String fieldId = importedDataRef.getId();
        Field<?> field = result.getTemplateCase().getField(fieldId).orElseGet(() -> result.getProcessScopedCase().getField(fieldId).get());
        com.netgrif.application.engine.workflow.domain.DataRef dataRef = new com.netgrif.application.engine.workflow.domain.DataRef(field);
        if (!transition.getDataSet().containsKey(fieldId)) {
            transition.getDataSet().put(fieldId, dataRef);
        } else {
            throw new IllegalArgumentException("Field with id [" + fieldId + "] occurs multiple times in transition [" + transition.getStringId() + "]");
        }
        if (importedDataRef.getEvent() != null) {
            importedDataRef.getEvent().forEach(event -> dataRef.addEvent(createDataEvent(event)));
        }
        addDataLogic(field, transition, importedDataRef, dataRef);
        addDataComponent(field, importedDataRef, dataRef);
        return dataRef;
    }

    protected void addDataLogic(Field<?> field, Transition transition, DataRef importedDataRef, com.netgrif.application.engine.workflow.domain.DataRef dataRef) {
        DataRefLogic logic = importedDataRef.getLogic();
        try {
            String fieldId = field.getStringId();
            if (logic == null || fieldId == null) {
                return;
            }

            DataFieldBehavior behavior = new DataFieldBehavior();
            if (logic.getBehavior() != null) {
                behavior.setBehavior(FieldBehavior.fromXml(logic.getBehavior()));
                behavior.setRequired(logic.isRequired() != null ? logic.isRequired() : false);
                behavior.setImmediate(logic.isImmediate() != null ? logic.isImmediate() : false);
            }
            dataRef.setBehavior(behavior);
            field.setBehavior(transition.getImportId(), behavior);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Wrong dataRef id [" + importedDataRef.getId() + "] on transition [" + transition.getTitle() + "]", e);
        }
    }

    protected void addDataComponent(Field<?> field, DataRef importedDataRef, com.netgrif.application.engine.workflow.domain.DataRef dataRef) throws MissingIconKeyException {
        Component component = field.getComponent();
        if (importedDataRef.getComponent() != null) {
            component = createComponent(importedDataRef.getComponent());
        }
        dataRef.setComponent(component);
    }

    public Component createComponent(com.netgrif.application.engine.importer.model.Component importedComponent) {
        Component component = new Component(importedComponent.getId());
        createProperties(importedComponent.getProperties(), component.getProperties());
        return component;
    }

    protected boolean isTrue(Boolean permission) {
        return (permission != null && permission);
    }
}
