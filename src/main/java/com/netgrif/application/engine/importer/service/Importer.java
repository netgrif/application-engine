package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.configuration.LayoutFlexConfiguration;
import com.netgrif.application.engine.configuration.LayoutGridConfiguration;
import com.netgrif.application.engine.importer.model.*;
import com.netgrif.application.engine.importer.model.DataRef;
import com.netgrif.application.engine.importer.service.evaluation.IActionEvaluator;
import com.netgrif.application.engine.importer.service.evaluation.IFunctionEvaluator;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.importer.service.validation.IProcessValidator;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.Function;
import com.netgrif.application.engine.petrinet.domain.Place;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.*;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.CaseEvent;
import com.netgrif.application.engine.petrinet.domain.events.DataEvent;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.ProcessEvent;
import com.netgrif.application.engine.petrinet.domain.layout.LayoutContainer;
import com.netgrif.application.engine.petrinet.domain.layout.LayoutItem;
import com.netgrif.application.engine.petrinet.domain.layout.LayoutObjectType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Importer {

    public static final String FILE_EXTENSION = ".xml";

    protected com.netgrif.application.engine.importer.model.Process importedProcess;
    protected Process process;
    protected ProcessRole defaultRole;
    protected ProcessRole anonymousRole;

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
    protected RoleFactory roleFactory;

    @Autowired
    protected TriggerFactory triggerFactory;

    @Autowired
    protected ComponentFactory componentFactory;

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

    public Optional<Process> importPetriNet(InputStream xml) throws MissingPetriNetMetaDataException, MissingIconKeyException {
        try {
            initialize();
            unmarshallXml(xml);
            return createPetriNet();
        } catch (JAXBException e) {
            log.error("Importing Petri net failed: ", e);
        }
        return Optional.empty();
    }

    protected void initialize() {
        this.defaultRole = processRoleService.defaultRole();
        this.anonymousRole = processRoleService.anonymousRole();
    }

    public com.netgrif.application.engine.importer.model.Process unmarshallXml(InputStream xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(com.netgrif.application.engine.importer.model.Process.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        importedProcess = (com.netgrif.application.engine.importer.model.Process) jaxbUnmarshaller.unmarshal(xml);
        return importedProcess;
    }

    protected Optional<Process> createPetriNet() throws MissingPetriNetMetaDataException, MissingIconKeyException {
        initializePetriNet();

        process.addRole(defaultRole);
        process.addRole(anonymousRole);
        importedProcess.getI18N().forEach(this::addI18N);

        setMetaData();
        addAllDataTransition();

        // TODO: release/8.0.0 static resources
        importedProcess.getRole().forEach(this::createRole);
        importedProcess.getData().forEach(this::createDataSet);
        importedProcess.getPlace().forEach(this::createPlace);
        importedProcess.getTransition().forEach(this::createTransition);
        importedProcess.getArc().forEach(this::createArc);
        importedProcess.getFunction().forEach(this::createFunction);
        importedProcess.getRoleRef().forEach(this::createRoleRef);

//        addPredefinedRolesWithDefaultPermissions();

        if (importedProcess.getProcessEvents() != null) {
            importedProcess.getProcessEvents().getEvent().forEach(this::createProcessEvent);
        }
        if (importedProcess.getCaseEvents() != null) {
            importedProcess.getCaseEvents().getEvent().forEach(this::createCaseEvent);
        }

        functionEvaluator.evaluate(process.getFunctions());
        actionEvaluator.evaluate(actions, process.getFunctions());
        processValidator.validate(process);

        return Optional.of(process);
    }

    protected void initializePetriNet() throws IllegalArgumentException {
        Extension extension = importedProcess.getExtends();
        if (extension != null) {
            initializeWithChildPetriNet(extension);
        } else {
            process = new Process();
        }
    }

    protected void initializeWithChildPetriNet(Extension extension) {
        if (areExtensionAttributesEmpty(extension)) {
            throw new IllegalArgumentException("Parent identifier or version is empty.");
        }
        Process parentNet = petriNetService.getPetriNet(extension.getId(), Version.of(extension.getVersion()));
        if (parentNet == null) {
            throw new IllegalArgumentException("Parent petri net not found.");
        }
        process = parentNet.clone();
        // transition must be removed since it's going to be added later with proper data
        process.setCreationDate(LocalDateTime.now());
        process.getTransitions().remove(allDataConfiguration.getAllData().getId());
        process.setObjectId(new ObjectId());
        process.addParentIdentifier(new PetriNetIdentifier(
                parentNet.getIdentifier(),
                parentNet.getVersion(),
                parentNet.getObjectId()
        ));
        UniqueKeyMap<String, ProcessRole> processRolesWithNewIds = new UniqueKeyMap<>();
        for (Map.Entry<String, ProcessRole> entry : process.getRoles().entrySet()) {
            ObjectId newId = new ObjectId();
            entry.getValue().setId(newId);
            processRolesWithNewIds.put(newId.toString(), entry.getValue());
        }
        process.setRoles(processRolesWithNewIds);
    }

    protected static boolean areExtensionAttributesEmpty(Extension extension) {
        return extension.getId() == null || extension.getId().isBlank()
                || extension.getVersion() == null || extension.getVersion().isBlank();
    }
//
//    protected void resolveUserRef(com.netgrif.application.engine.importer.model.CaseUserRef userRef) {
//        com.netgrif.application.engine.importer.model.CaseLogic logic = userRef.getCaseLogic();
//        String usersId = userRef.getId();
//
//        if (logic == null || usersId == null) {
//            return;
//        }
//
//        net.addUserPermission(usersId, roleFactory.getProcessPermissions(logic));
//    }
//
//    protected void resolveProcessEvents(com.netgrif.application.engine.importer.model.ProcessEvents processEvents) {
//        if (processEvents != null && processEvents.getEvent() != null) {
//            net.setProcessEvents(createProcessEventsMap(processEvents.getEvent()));
//        }
//    }
//
//    protected void resolveCaseEvents(com.netgrif.application.engine.importer.model.CaseEvents caseEvents) {
//        if (caseEvents != null && caseEvents.getEvent() != null) {
//            net.setCaseEvents(createCaseEventsMap(caseEvents.getEvent()));
//        }
//    }
//
//    protected void resolveActionRefs(String actionId, Action action) {
//        Action referenced = actions.get(actionId);
//        if (referenced == null) {
//            throw new IllegalArgumentException("Invalid action reference with id [" + actionId + "]");
//        }
//        action.setDefinition(referenced.getDefinition());
//        action.setTrigger(referenced.getTrigger());
//    }

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

    protected void setMetaData() throws MissingPetriNetMetaDataException {
        checkMetaData(importedProcess.getId(), () -> {
            process.setImportId(importedProcess.getId());
            process.setIdentifier(importedProcess.getId());
        }, "<id>");
        checkMetaData(importedProcess.getVersion(), () -> {
            process.setVersion(Version.of(importedProcess.getVersion()));
        }, "<version>");
        checkMetaData(importedProcess.getTitle(), () -> {
            process.setTitle(toI18NString(importedProcess.getTitle()));
        }, "<title>");
        // TODO: release/8.0.0 extension from NAE-1973
        process.setIcon(importedProcess.getIcon());

        if (importedProcess.getCaseName() != null) {
            I18nExpression caseName = new I18nExpression(importedProcess.getCaseName().getValue());
            caseName.setDynamic(importedProcess.getCaseName().isDynamic());
            caseName.setKey(importedProcess.getCaseName().getId());
            caseName.setTranslations(i18n.get(caseName.getKey()).getTranslations());
            process.setDefaultCaseName(caseName);
        }
        createProperties(importedProcess.getProperties(), process.getProperties());

        if (!missingMetaData.isEmpty()) {
            throw new MissingPetriNetMetaDataException(missingMetaData);
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

//    private List<com.netgrif.application.engine.importer.model.Action> filterActionsByTrigger(List<com.netgrif.application.engine.importer.model.Action> actions, com.netgrif.application.engine.importer.model.DataEventType trigger) {
//        return actions.stream()
//                .filter(action -> action.getTrigger().equalsIgnoreCase(trigger.toString()))
//                .collect(Collectors.toList());
//    }
//
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
        process.addArc(arcImporter.getArc(importArc, this));
    }

    protected boolean isInputArc(com.netgrif.application.engine.importer.model.Arc importArc) {
        return this.process.getPlace(importArc.getSourceId()) != null;
    }

    protected Place getPlace(String id) {
        Place place = process.getPlace(id);
        if (place == null) {
            throw new IllegalArgumentException("Place with id [" + id + "] not found.");
        }
        return place;
    }

    protected Transition getTransition(String id) {
        Transition transition = process.getTransition(id);
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
        process.addDataSetField(field);
    }

    protected void createTransition(com.netgrif.application.engine.importer.model.Transition importTransition) throws MissingIconKeyException {
        Transition transition = new Transition();
        transition.setImportId(importTransition.getId());
        transition.setTitle(toI18NString(importTransition.getTitle()));
        createProperties(importTransition.getProperties(), transition.getProperties());
        transition.setIcon(importTransition.getIcon());
        transition.setAssignPolicy(toAssignPolicy(importTransition.getAssignPolicy()));
        transition.setFinishPolicy(toFinishPolicy(importTransition.getFinishPolicy()));
//
//        if (importTransition.getRoleRef() != null) {
//            importTransition.getRoleRef().forEach(roleRef ->
//                    addRoleLogic(transition, roleRef)
//            );
//        }
//
//        if (importTransition.getTrigger() != null) {
//            importTransition.getTrigger().forEach(trigger ->
//                    createTrigger(transition, trigger)
//            );
//        }
//
//        addPredefinedRolesWithDefaultPermissions(importTransition, transition);
//
        if (importTransition.getEvent() != null) {
            importTransition.getEvent().forEach(event -> transition.addEvent(createEvent(event)));
        }
        resolveLayoutContainer(importTransition, transition);

        process.addTransition(transition);
    }

    protected Event createEvent(com.netgrif.application.engine.importer.model.Event importedEvent) {
        Event event = new Event();
        event.setTitle(toI18NString(importedEvent.getTitle()));
        event.setType(com.netgrif.application.engine.importer.model.EventType.valueOf(importedEvent.getType().value().toUpperCase()));
        this.addBaseEventProperties(event, importedEvent);
        return event;
    }

    protected void addBaseEventProperties(com.netgrif.application.engine.petrinet.domain.events.BaseEvent event, com.netgrif.application.engine.importer.model.BaseEvent imported) {
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
        process.addProcessEvent(event);
    }

    protected void createCaseEvent(com.netgrif.application.engine.importer.model.CaseEvent importedEvent) {
        if (importedEvent == null) {
            return;
        }
        CaseEvent event = new CaseEvent();
        event.setType(CaseEventType.valueOf(importedEvent.getType().value().toUpperCase()));
        this.addBaseEventProperties(event, importedEvent);
        process.addCaseEvent(event);
    }

    protected void createFunction(com.netgrif.application.engine.importer.model.Function importedFunction) {
        Function function = new Function();
        function.setDefinition(importedFunction.getValue());
        function.setName(importedFunction.getName());
        function.setScope(FunctionScope.valueOf(importedFunction.getScope().name()));
        process.addFunction(function);
    }

    protected void createRoleRef(com.netgrif.application.engine.importer.model.CaseRoleRef roleRef) {
        // TODO: release/8.0.0
//        com.netgrif.application.engine.importer.model.CaseLogic logic = roleRef.getCaseLogic();
//        String roleId = net.getRoles().get(roleRef.getId()).getStringId();
//
//        if (logic == null || roleId == null) {
//            return;
//        }
//        if (logic.isView() != null && !logic.isView()) {
//            net.addNegativeViewRole(roleId);
//        }
//
//        net.addPermission(roleId, roleFactory.getProcessPermissions(logic));
    }

//    protected void addDefaultRole(Transition transition) {
//        if (!net.isDefaultRoleEnabled() || isDefaultRoleReferenced(transition)) {
//            return;
//        }
//
//        com.netgrif.application.engine.importer.model.RoleRefLogic logic = new com.netgrif.application.engine.importer.model.RoleRefLogic();
//        logic.setPerform(true);
//        transition.addRole(defaultRole.getStringId(), roleFactory.getPermissions(logic));
//    }
//
//    protected void addAnonymousRole(Transition transition) {
//        // TODO: NAE-1969 refactor
//        if (!net.isAnonymousRoleEnabled() || isAnonymousRoleReferenced(transition)) {
//            return;
//        }
//
//        com.netgrif.application.engine.importer.model.RoleRefLogic logic = new com.netgrif.application.engine.importer.model.RoleRefLogic();
//        logic.setPerform(true);
//        transition.addRole(anonymousRole.getStringId(), roleFactory.getPermissions(logic));
//    }

//
//    protected void addDefaultPermissions() {
//        if (!net.isDefaultRoleEnabled() || isDefaultRoleReferencedOnNet()) {
//            return;
//        }
//
//        com.netgrif.application.engine.importer.model.CaseLogic logic = new com.netgrif.application.engine.importer.model.CaseLogic();
//        logic.setCreate(true);
//        logic.setDelete(true);
//        logic.setView(true);
//        net.addPermission(defaultRole.getStringId(), roleFactory.getProcessPermissions(logic));
//    }
//
//    protected void addAnonymousPermissions() {
//        if (!net.isAnonymousRoleEnabled() || isAnonymousRoleReferencedOnNet()) {
//            return;
//        }
//
//        com.netgrif.application.engine.importer.model.CaseLogic logic = new com.netgrif.application.engine.importer.model.CaseLogic();
//        logic.setCreate(true);
//        logic.setView(true);
//        net.addPermission(anonymousRole.getStringId(), roleFactory.getProcessPermissions(logic));
//    }
//
//    protected void addRoleLogic(Transition transition, com.netgrif.application.engine.importer.model.RoleRef roleRef) {
//        com.netgrif.application.engine.importer.model.RoleRefLogic logic = roleRef.getLogic();
//        String roleId = getRole(roleRef.getId()).getStringId();
//
//        if (logic == null || roleId == null) {
//            return;
//        }
//        if (logic.isView() != null && !logic.isView()) {
//            transition.addNegativeViewRole(roleId);
//        }
//        transition.addRole(roleId, roleFactory.getPermissions(logic));
//    }

//    protected void addUserLogic(Transition transition, UserRef userRef) {
//        Logic logic = userRef.getLogic();
//        String userRefId = userRef.getId();
//
//        if (logic == null || userRefId == null) {
//            return;
//        }
//        transition.addUserRef(userRefId, roleFactory.getPermissions(logic));
//    }
//
//    protected void addDataLogic(Transition transition, DataRef dataRef) {
//        Logic logic = dataRef.getLogic();
//        try {
//            Field<?> field = getField(dataRef.getId());
//            String fieldId = field.getStringId();
//            if (logic == null || fieldId == null) {
//                return;
//            }
//
//            DataFieldBehavior behavior = new DataFieldBehavior();
//            if (logic.getBehavior() != null) {
//                Optional<Behavior> first = logic.getBehavior().stream().filter(this::isNotDeprecated).findFirst();
//                behavior.setBehavior(FieldBehavior.fromString(first.orElse(com.netgrif.application.engine.importer.model.Behavior.EDITABLE)));
//                behavior.setRequired(logic.getBehavior().stream().anyMatch(Behavior.REQUIRED::equals));
//                behavior.setImmediate(logic.getBehavior().stream().anyMatch(Behavior.IMMEDIATE::equals));
//            }
//            transition.setDataRefBehavior(field, behavior);
//        } catch (NullPointerException e) {
//            throw new IllegalArgumentException("Wrong dataRef id [" + dataRef.getId() + "] on transition [" + transition.getTitle() + "]", e);
//        }
//    }
//
//    // TODO: release/8.0.0 Behavior REQ,IMM,OPT deprecated
//    private boolean isNotDeprecated(Behavior behavior) {
//        return !Behavior.REQUIRED.equals(behavior) && !Behavior.IMMEDIATE.equals(behavior) && !Behavior.OPTIONAL.equals(behavior);
//    }
//
//    // TODO: release/8.0.0 check merge
//    /*protected void addDataComponent(Transition transition, DataRef dataRef) throws MissingIconKeyException {
//        String fieldId = getField(dataRef.getId()).getStringId();
//        Component component = null;
//        if ((dataRef.getComponent()) != null) {
//            component = componentFactory.buildComponent(dataRef.getComponent(), this, getField(dataRef.getId()));
//        }
//        transition.addDataSet(fieldId, null, null, null, component);
//    }*/
//    protected void addDataComponent(Transition transition, DataRef dataRef) throws MissingIconKeyException {
//        Field<?> field = getField(dataRef.getId());
//        Component component;
//        if ((dataRef.getComponent()) == null) {
//            component = field.getComponent();
//        } else {
//            component = componentFactory.buildComponent(dataRef.getComponent(), this, field);
//        }
//        transition.setDataRefComponent(field, component);
//    }
//
//
//    protected List<Action> buildActions(List<com.netgrif.application.engine.importer.model.Action> imported, String fieldId, String transitionId) {
//        return imported.stream()
//                .map(action -> parseAction(fieldId, transitionId, action))
//                .collect(Collectors.toList());
//    }

    protected String buildActionId(String actionId) {
        if (actionId == null) {
            actionId = new ObjectId().toString();
        }
        // TODO: release/8.0.0 optimize ids of actions to not use strings
        return this.process.getIdentifier() + "-" + actionId;
    }

//
//    protected void createTrigger(Transition transition, com.netgrif.application.engine.importer.model.Trigger importTrigger) {
//        com.netgrif.application.engine.workflow.domain.triggers.Trigger trigger = triggerFactory.buildTrigger(importTrigger);
//
//        transition.addTrigger(trigger);
//    }
//
    protected void createPlace(com.netgrif.application.engine.importer.model.Place importPlace) {
        Place place = new Place();
        place.setImportId(importPlace.getId());
        place.setTokens(importPlace.getTokens());
        place.setTitle(toI18NString(importPlace.getTitle()));
        // TODO: release/8.0.0 scope
        createProperties(importPlace.getProperties(), place.getProperties());
        process.addPlace(place);
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
        if (importRole.getEvent() != null) {
            importRole.getEvent().forEach(event -> role.addEvent(createEvent(event)));
        }

        role.setNetId(process.getStringId());
        process.addRole(role);
    }
//
//    protected Map<EventType, com.netgrif.application.engine.petrinet.domain.events.Event> createEventsMap(List<com.netgrif.application.engine.importer.model.Event> events) {
//        Map<EventType, com.netgrif.application.engine.petrinet.domain.events.Event> finalEvents = new HashMap<>();
//        events.forEach(event ->
//                finalEvents.put(com.netgrif.application.engine.importer.model.EventType.valueOf(event.getType().value().toUpperCase()), createEvent(null, event))
//        );
//
//        return finalEvents;
//    }

//    protected Map<com.netgrif.application.engine.importer.model.ProcessEventType, com.netgrif.application.engine.petrinet.domain.events.ProcessEvent> createProcessEventsMap(List<com.netgrif.application.engine.importer.model.ProcessEvent> events) {
//        Map<com.netgrif.application.engine.importer.model.ProcessEventType, com.netgrif.application.engine.petrinet.domain.events.ProcessEvent> finalEvents = new HashMap<>();
//        events.forEach(event ->
//                finalEvents.put(com.netgrif.application.engine.importer.model.ProcessEventType.valueOf(event.getType().value().toUpperCase()), addProcessEvent(event))
//        );
//
//        return finalEvents;
//    }
//
//    protected Map<com.netgrif.application.engine.importer.model.CaseEventType, com.netgrif.application.engine.petrinet.domain.events.CaseEvent> createCaseEventsMap(List<com.netgrif.application.engine.importer.model.CaseEvent> events) {
//        Map<com.netgrif.application.engine.importer.model.CaseEventType, com.netgrif.application.engine.petrinet.domain.events.CaseEvent> finalEvents = new HashMap<>();
//        events.forEach(event ->
//                finalEvents.put(com.netgrif.application.engine.importer.model.CaseEventType.valueOf(event.getType().value().toUpperCase()), addCaseEvent(event))
//        );
//
//        return finalEvents;
//    }
//

//
//    protected void addPredefinedRolesWithDefaultPermissions(com.netgrif.application.engine.importer.model.Transition importTransition, Transition transition) {
//        // Don't add if positive roles or triggers or positive user refs
//        if ((importTransition.getRoleRef() != null && importTransition.getRoleRef().stream().anyMatch(this::hasPositivePermission))
//                || (importTransition.getTrigger() != null && !importTransition.getTrigger().isEmpty())) {
//            return;
//        }
//        addDefaultRole(transition);
//        addAnonymousRole(transition);
//    }
//
//    protected boolean hasPositivePermission(com.netgrif.application.engine.importer.model.PermissionRef permissionRef) {
//        return (permissionRef.getLogic().isPerform() != null && permissionRef.getLogic().isPerform())
//                || (permissionRef.getLogic().isView() != null && permissionRef.getLogic().isView())
//                || (permissionRef.getLogic().isAssign() != null && permissionRef.getLogic().isAssign())
//                || (permissionRef.getLogic().isCancel() != null && permissionRef.getLogic().isCancel())
//                || (permissionRef.getLogic().isFinish() != null && permissionRef.getLogic().isFinish())
//                || (permissionRef.getLogic().isReassign() != null && permissionRef.getLogic().isReassign())
//                || (permissionRef.getLogic().isViewDisabled() != null && permissionRef.getLogic().isViewDisabled());
//    }

    //    protected void addPredefinedRolesWithDefaultPermissions() {
//        // only if no positive role associations and no positive user ref associations
//        if (net.getPermissions().values().stream().anyMatch(perms -> perms.containsValue(true))
//                || net.getUserRefs().values().stream().anyMatch(perms -> perms.containsValue(true))) {
//            return;
//        }
//
//        addDefaultPermissions();
//        addAnonymousPermissions();
//    }
//
//    protected boolean isDefaultRoleReferenced(Transition transition) {
//        return transition.getRoles().containsKey(defaultRole.getStringId());
//    }
//
//    protected boolean isDefaultRoleReferencedOnNet() {
//        return net.getPermissions().containsKey(defaultRole.getStringId());
//    }
//
//    protected boolean isAnonymousRoleReferenced(Transition transition) {
//        return transition.getRoles().containsKey(anonymousRole.getStringId());
//    }
//
//    protected boolean isAnonymousRoleReferencedOnNet() {
//        return net.getPermissions().containsKey(anonymousRole.getStringId());
//    }
//
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
            for (java.lang.reflect.Field field: importedGridContainer.getProperties().getClass().getDeclaredFields()) {
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

    protected com.netgrif.application.engine.petrinet.domain.DataRef resolveDataRef(com.netgrif.application.engine.importer.model.DataRef importedDataRef, Transition transition) {
        String fieldId = importedDataRef.getId();
        Field<?> field = process.getField(fieldId).get();
        com.netgrif.application.engine.petrinet.domain.DataRef dataRef = new com.netgrif.application.engine.petrinet.domain.DataRef(field);
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

    protected void addDataLogic(Field<?> field, Transition transition, DataRef importedDataRef, com.netgrif.application.engine.petrinet.domain.DataRef dataRef) {
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

    protected void addDataComponent(Field<?> field, DataRef importedDataRef, com.netgrif.application.engine.petrinet.domain.DataRef dataRef) throws MissingIconKeyException {
        Component component;
//            TODO: release/8.0.0
//        if ((importedDataRef.getComponent()) == null) {
            component = field.getComponent();
//        } else {
//            component = componentFactory.buildComponent(importedDataRef.getComponent(), this, field);
//        }
        dataRef.setComponent(component);
    }
}
