package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Process;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.importer.service.validation.IActionValidator;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.ArcFactory;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class Importer {

    public static final String FILE_EXTENSION = ".xml";

    protected com.netgrif.application.engine.importer.model.Process process;
    protected PetriNet net;
    protected ProcessRole defaultRole;
    protected ProcessRole anonymousRole;

    protected Map<String, I18nString> i18n;

    @Autowired
    protected AllDataConfiguration allDataConfiguration;

    @Autowired
    protected FieldFactory fieldFactory;

    @Autowired
    protected FunctionFactory functionFactory;

    @Autowired
    protected IPetriNetService service;

    @Autowired
    protected IProcessRoleService processRoleService;

    @Autowired
    protected ArcFactory arcFactory;

    @Autowired
    protected RoleFactory roleFactory;

    @Autowired
    protected TriggerFactory triggerFactory;

    @Autowired
    protected IActionValidator actionValidator;

    @Autowired
    protected ActionRunner actionsRunner;

    @Autowired
    protected FileStorageConfiguration fileStorageConfiguration;

    @Autowired
    protected ComponentFactory componentFactory;

    @Autowired
    protected IFieldActionsCacheService actionsCacheService;

    public Optional<PetriNet> importPetriNet(InputStream xml) throws MissingPetriNetMetaDataException, MissingIconKeyException {
        try {
            initialize();
            unmarshallXml(xml);
            return createPetriNet();
        } catch (JAXBException e) {
            log.error("Importing Petri net failed: ", e);
        }
        return Optional.empty();
    }

    public Optional<PetriNet> importPetriNet(File xml) throws MissingPetriNetMetaDataException, MissingIconKeyException {
        try {
            return importPetriNet(new FileInputStream(xml));
        } catch (FileNotFoundException e) {
            log.error("Importing Petri net failed: ", e);
        }
        return Optional.empty();
    }

    protected void initialize() {
        this.defaultRole = processRoleService.defaultRole();
        this.anonymousRole = processRoleService.anonymousRole();
        this.net.addRole(defaultRole);
        this.net.addRole(anonymousRole);
    }

    public Process unmarshallXml(InputStream xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Process.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        process = (Process) jaxbUnmarshaller.unmarshal(xml);
        return process;
    }

    public Path saveNetFile(PetriNet net, InputStream xmlFile) throws IOException {
        File savedFile = new File(fileStorageConfiguration.getStorageArchived() + net.getStringId() + "-" + net.getTitle() + FILE_EXTENSION);
        net.setImportXmlPath(savedFile.getPath());
//        copyInputStreamToFile(xmlFile, savedFile);
        return savedFile.toPath();
    }

    protected Optional<PetriNet> createPetriNet() throws MissingPetriNetMetaDataException, MissingIconKeyException {
////        documentValidator.checkConflictingAttributes(process, process.getUsersRef(), process.getUserRef(), "usersRef", "userRef");
////        documentValidator.checkDeprecatedAttributes(process);
        net = new PetriNet();
//        process.getI18N().forEach(this::addI18N);
//
//        setMetaData();
//        addAllDataTransition();
//
//        process.getRole().forEach(this::createRole);
//        process.getData().forEach(this::createDataSet);
//        process.getPlace().forEach(this::createPlace);
//        process.getTransition().forEach(this::createTransition);
//        process.getArc().forEach(this::createArc);
//        process.getData().forEach(this::resolveDataActions);
//        process.getTransition().forEach(this::resolveTransitionActions);
//        process.getFunction().forEach(this::createFunction);
//        process.getRoleRef().forEach(this::resolveRoleRef);
//
//        addPredefinedRolesWithDefaultPermissions();
//
//        resolveProcessEvents(process.getProcessEvents());
//        resolveCaseEvents(process.getCaseEvents());
//        evaluateFunctions();
//        actions.forEach(this::evaluateActions);
//
//        if (process.getCaseName() != null && process.getCaseName().isDynamic()) {
//            net.setDefaultCaseNameExpression(new Expression(process.getCaseName().getValue(), process.getCaseName().isDynamic()));
//        } else {
//            net.setDefaultCaseName(toI18NString(process.getCaseName()));
//        }
//        createProperties(process.getProperties());
//
        return Optional.of(net);
    }

//    protected void addAllDataTransition() {
//        com.netgrif.application.engine.importer.model.Transition allDataConfig = allDataConfiguration.getAllData();
//        if (process.getTransition().stream().anyMatch(transition -> allDataConfig.getId().equals(transition.getId()))) {
//            return;
//        }
//        com.netgrif.application.engine.importer.model.Transition allDataTransition = new com.netgrif.application.engine.importer.model.Transition();
//        TODO: NAE-1969 merge forms from NAE-1966
//        com.netgrif.application.engine.importer.model.DataGroup configDataGroup = allDataConfig.getDataGroup().get(0);
//        int y = 0;
//        allDataTransition.setId(allDataConfig.getId());
//        allDataTransition.setX(allDataConfig.getX());
//        allDataTransition.setY(allDataConfig.getY());
//        allDataTransition.setLabel(allDataConfig.getLabel());
//        allDataTransition.setIcon(allDataConfig.getIcon());
//        allDataTransition.setPriority(allDataConfig.getPriority());
//        allDataTransition.setAssignPolicy(allDataConfig.getAssignPolicy());
//        allDataTransition.setFinishPolicy(allDataConfig.getFinishPolicy());
//        // TODO: NAE-1858: all properties
//        com.netgrif.application.engine.importer.model.DataGroup allDataGroup = new com.netgrif.application.engine.importer.model.DataGroup();
//        for (Data field : process.getData()) {
//            DataRef dataRef = new DataRef();
//            dataRef.setId(field.getId());
//            Layout layout = new Layout();
//            layout.setCols(configDataGroup.getCols());
//            layout.setRows(1);
//            layout.setX(0);
//            layout.setY(y);
//            layout.setOffset(0);
//            layout.setTemplate(com.netgrif.application.engine.importer.model.Template.MATERIAL);
//            layout.setAppearance(com.netgrif.application.engine.importer.model.Appearance.OUTLINE);
//            dataRef.setLayout(layout);
//            Logic logic = new Logic();
//            logic.getBehavior().add(com.netgrif.application.engine.importer.model.Behavior.EDITABLE);
//            dataRef.setLogic(logic);
//            allDataGroup.getDataRef().add(dataRef);
//            y++;
//        }
//        allDataTransition.getDataGroup().add(allDataGroup);
//        process.getTransition().add(allDataTransition);
//    }

//    protected void resolveRoleRef(com.netgrif.application.engine.importer.model.CaseRoleRef roleRef) {
//        com.netgrif.application.engine.importer.model.CaseLogic logic = roleRef.getCaseLogic();
//        String roleId = getRole(roleRef.getId()).getStringId();
//
//        if (logic == null || roleId == null) {
//            return;
//        }
//        if (logic.isView() != null && !logic.isView()) {
//            net.addNegativeViewRole(roleId);
//        }
//
//        net.addPermission(roleId, roleFactory.getProcessPermissions(logic));
//    }//
//    protected void createFunction(com.netgrif.application.engine.importer.model.Function function) {
//        com.netgrif.application.engine.petrinet.domain.Function fun = functionFactory.getFunction(function);
//
//        net.addFunction(fun);
//        functions.add(fun);
//    }
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
//    protected void evaluateFunctions() {
//        try {
//            actionsCacheService.evaluateFunctions(functions);
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Could not evaluate functions: " + e.getMessage(), e);
//        }
//    }
//
//    protected void evaluateActions(String s, Action action) {
//        try {
//            actionsRunner.getActionCode(action, functions, true);
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Could not evaluate action[" + action.getImportId() + "]: \n " + action.getDefinition(), e);
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

//    protected void addI18N(com.netgrif.application.engine.importer.model.I18N importI18N) {
//        String locale = importI18N.getLocale();
//        importI18N.getI18NString().forEach(translation -> addTranslation(translation, locale));
//    }
//
//    protected void addTranslation(com.netgrif.application.engine.importer.model.I18NStringType i18NStringType, String locale) {
//        String id = i18NStringType.getId();
//        I18nString translation = getI18n(id);
//        if (translation == null) {
//            translation = new I18nString();
//            i18n.put(id, translation);
//        }
//        translation.addTranslation(locale, i18NStringType.getValue());
//    }

    //    protected void applyMapping(Mapping mapping) throws MissingIconKeyException {
//        Transition transition = getTransition(mapping.getTransitionRef());
//        mapping.getRoleRef().forEach(roleRef -> addRoleLogic(transition, roleRef));
//        mapping.getDataRef().forEach(dataRef -> addDataLogic(transition, dataRef));
//        for (com.netgrif.application.engine.importer.model.DataGroup dataGroup : mapping.getDataGroup()) {
//            addDataGroup(transition, dataGroup, mapping.getDataGroup().indexOf(dataGroup));
//        }
//        mapping.getTrigger().forEach(trigger -> addTrigger(transition, trigger));
//    }
//
//    protected void resolveDataActions(Data data) {
//        String fieldId = data.getId();
//        if (data.getEvent() != null && !data.getEvent().isEmpty()) {
//            getField(fieldId).setEvents(buildEvents(fieldId, data.getEvent(), null));
//        }
//        if (data.getAction() != null) {
//            Map<com.netgrif.application.engine.importer.model.DataEventType, DataEvent> events = getField(fieldId).getEvents();
//
//            List<com.netgrif.application.engine.importer.model.Action> filteredActions = filterActionsByTrigger(data.getAction(), com.netgrif.application.engine.importer.model.DataEventType.GET);
//            addActionsToEvent(buildActions(filteredActions, fieldId, null), com.netgrif.application.engine.importer.model.DataEventType.GET, events);
//
//            filteredActions = filterActionsByTrigger(data.getAction(), com.netgrif.application.engine.importer.model.DataEventType.SET);
//            addActionsToEvent(buildActions(filteredActions, fieldId, null), com.netgrif.application.engine.importer.model.DataEventType.SET, events);
//        }
//    }
//
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
//    protected void addActionRefs(Data data) {
//        if (data.getActionRef() != null) {
//            List<Action> actions = buildActionRefs(data.getActionRef());
//            getField(data.getId()).addActions(actions.stream().filter(action -> action.getTrigger() == com.netgrif.application.engine.importer.model.DataEventType.GET).collect(Collectors.toList()), com.netgrif.application.engine.importer.model.DataEventType.GET);
//            getField(data.getId()).addActions(actions.stream().filter(action -> action.getTrigger() == com.netgrif.application.engine.importer.model.DataEventType.SET).collect(Collectors.toList()), com.netgrif.application.engine.importer.model.DataEventType.SET);
//        }
//    }
//
//    protected List<Action> buildActionRefs(List<ActionRef> actionRefs) {
//        return actionRefs.stream().map(ref -> actions.get(ref.getId())).collect(Collectors.toList());
//    }
//
//    protected Action fromActionRef(ActionRef actionRef) {
//        Action placeholder = new Action();
//        placeholder.setImportId(actionRef.getId());
//        this.actionRefs.put(actionRef.getId(), placeholder);
//        return placeholder;
//    }
//
//    protected void resolveTransitionActions(com.netgrif.application.engine.importer.model.Transition trans) {
//        if (trans.getDataRef() != null) {
//            resolveDataRefActions(trans.getDataRef(), trans);
//        }
//        if (trans.getDataGroup() != null) {
//            trans.getDataGroup().forEach(ref -> {
//                if (ref.getDataRef() != null) {
//                    resolveDataRefActions(ref.getDataRef(), trans);
//                }
//            });
//        }
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
//    protected DataEvent createDefaultEvent(List<Action> actions, com.netgrif.application.engine.importer.model.DataEventType type) {
//        DataEvent event = new DataEvent();
//        event.setType(type);
//        event.setId(new ObjectId().toString());
//        event.addToActionsByDefaultPhase(actions);
//        return event;
//    }

//    protected void createArc(com.netgrif.application.engine.importer.model.Arc importArc) {
//        com.netgrif.application.engine.petrinet.domain.arcs.Arc arc = arcFactory.getArc(importArc);
//        arc.setImportId(importArc.getId());
//        arc.setSource(getNode(importArc.getSourceId()));
//        arc.setDestination(getNode(importArc.getDestinationId()));
//        // TODO: NAE-1969 multiplicity
//        if (importArc.getBreakpoint() != null) {
//            importArc.getBreakpoint().forEach(position -> arc.getBreakpoints().add(new Position(position.getX(), position.getY())));
//        }
//        createProperties(importArc.getProperties(), arc.getProperties());
//        net.addArc(arc);
//    }
//
//    protected Node getNode(String id) {
//        if (net.getPlace(id) != null) {
//            return net.getPlace(id);
//        } else if (net.getTransition(id) != null) {
//            return net.getTransition(id);
//        }
//        throw new IllegalArgumentException("Node with id [" + id + "] not found.");
//    }
//
//    protected void createDataSet(com.netgrif.application.engine.importer.model.Data importData) throws MissingIconKeyException {
//        Field<?> field = fieldFactory.getField(importData, this);
//
//        net.addDataSetField(field);
//    }
//
//    protected void createTransition(com.netgrif.application.engine.importer.model.Transition importTransition) throws MissingIconKeyException {
//        TODO: NAE-1969 fix
//        transitionValidator.checkConflictingAttributes(importTransition, importTransition.getUsersRef(), importTransition.getUserRef(), "usersRef", "userRef");
//        transitionValidator.checkDeprecatedAttributes(importTransition);

//        Transition transition = new Transition();
//        transition.setImportId(importTransition.getId());
//        transition.setTitle(toI18NString(importTransition.getTitle()));
//        transition.setPosition(importTransition.getX(), importTransition.getY());
//        createProperties(importTransition.getProperties(), transition.getProperties());
    // TODO: NAE-1969
//        if (importTransition.getLayout() != null) {
//            transition.setLayout(new TaskLayout(importTransition));
//        }
//        transition.setIcon(importTransition.getIcon());
//        transition.setAssignPolicy(toAssignPolicy(importTransition.getAssignPolicy()));
//        transition.setFinishPolicy(toFinishPolicy(importTransition.getFinishPolicy()));
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
//        if (importTransition.getEvent() != null) {
//            importTransition.getEvent().forEach(event ->
//                    transition.addEvent(createEvent(transition.getImportId(), event))
//            );
//        }
//
//        net.addTransition(transition);
//        transitions.put(importTransition.getId(), transition);
//    }
//
//    protected com.netgrif.application.engine.petrinet.domain.events.Event createEvent(String transitionId, com.netgrif.application.engine.importer.model.Event imported) {
//        com.netgrif.application.engine.petrinet.domain.events.Event event = new com.netgrif.application.engine.petrinet.domain.events.Event();
//        event.setImportId(imported.getId());
//        event.setMessage(toI18NString(imported.getMessage()));
//        event.setTitle(toI18NString(imported.getTitle()));
//        event.setType(com.netgrif.application.engine.importer.model.EventType.valueOf(imported.getType().value().toUpperCase()));
//        event.setPostActions(parsePostActions(transitionId, imported));
//        event.setPreActions(parsePreActions(transitionId, imported));
//        return event;
//    }

//    protected com.netgrif.application.engine.petrinet.domain.events.ProcessEvent addProcessEvent(com.netgrif.application.engine.importer.model.ProcessEvent imported) {
//        com.netgrif.application.engine.petrinet.domain.events.ProcessEvent event = new com.netgrif.application.engine.petrinet.domain.events.ProcessEvent();
//        event.setMessage(toI18NString(imported.getMessage()));
//        event.setImportId(imported.getId());
//        event.setType(com.netgrif.application.engine.importer.model.ProcessEventType.valueOf(imported.getType().value().toUpperCase()));
//        event.setPostActions(parsePostActions(null, imported));
//        event.setPreActions(parsePreActions(null, imported));
//
//        return event;
//    }
//
//
//    protected com.netgrif.application.engine.petrinet.domain.events.CaseEvent addCaseEvent(com.netgrif.application.engine.importer.model.CaseEvent imported) {
//        com.netgrif.application.engine.petrinet.domain.events.CaseEvent event = new com.netgrif.application.engine.petrinet.domain.events.CaseEvent();
//        event.setMessage(toI18NString(imported.getMessage()));
//        event.setImportId(imported.getId());
//        event.setType(com.netgrif.application.engine.importer.model.CaseEventType.valueOf(imported.getType().value().toUpperCase()));
//        event.setPostActions(parsePostActions(null, imported));
//        event.setPreActions(parsePreActions(null, imported));
//
//        return event;
//    }

//    protected List<Action> parsePostActions(String transitionId, com.netgrif.application.engine.importer.model.BaseEvent imported) {
//        return parsePhaseActions(com.netgrif.application.engine.importer.model.EventPhaseType.POST, transitionId, imported);
//    }
//
//    protected List<Action> parsePreActions(String transitionId, com.netgrif.application.engine.importer.model.BaseEvent imported) {
//        return parsePhaseActions(com.netgrif.application.engine.importer.model.EventPhaseType.PRE, transitionId, imported);
//    }
//
//    protected List<Action> parsePhaseActions(com.netgrif.application.engine.importer.model.EventPhaseType phase, String transitionId, com.netgrif.application.engine.importer.model.BaseEvent imported) {
//        return imported.getActions().stream()
//                .filter(actions -> actions.getPhase().equals(phase))
//                .flatMap(actions -> actions.getAction().parallelStream()
//                        .map(action -> parseAction(transitionId, action)))
//                .collect(Collectors.toList());
//    }

//    protected List<Action> parsePhaseActions(String fieldId, com.netgrif.application.engine.importer.model.EventPhaseType phase, com.netgrif.application.engine.importer.model.DataEventType trigger, String transitionId, com.netgrif.application.engine.importer.model.DataEvent dataEvent) {
//        List<Action> actionList = dataEvent.getActions().stream()
//                .filter(actions -> actions.getPhase().equals(phase))
//                .flatMap(actions -> actions.getAction().stream()
//                        .map(action -> {
//                            action.setTrigger(trigger.name());
//                            return parseAction(fieldId, transitionId, action);
//                        }))
//                .collect(Collectors.toList());
//        actionList.addAll(dataEvent.getActions().stream()
//                .filter(actions -> actions.getPhase().equals(phase))
//                .flatMap(actions -> actions.getActionRef().stream().map(this::fromActionRef))
//                .collect(Collectors.toList()));
//        return actionList;
//    }

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
//    protected void addDataWithDefaultGroup(Transition transition, DataRef dataRef) throws MissingIconKeyException {
//        DataGroup dataGroup = new DataGroup();
//        dataGroup.setImportId(transition.getImportId() + "_" + dataRef.getId() + "_" + System.currentTimeMillis());
//        if (transition.getLayout() != null && transition.getLayout().getCols() != null) {
//            dataGroup.setLayout(new DataGroupLayout(null, transition.getLayout().getCols(), null, null, null));
//        }
//        dataGroup.setAlignment(DataGroupAlignment.START);
//        dataGroup.setStretch(true);
//        dataGroup.addData(getField(dataRef.getId()).getStringId());
//        transition.addDataGroup(dataGroup);
//
//        addDataLogic(transition, dataRef);
//        addDataLayout(transition, dataRef);
//        addDataComponent(transition, dataRef);
//    }
//
//    protected void addDataGroup(Transition transition, com.netgrif.application.engine.importer.model.DataGroup importDataGroup, int index) throws MissingIconKeyException {
//        DataGroup dataGroup = new DataGroup();
//
//        if (importDataGroup.getId() != null && importDataGroup.getId().length() > 0) {
//            dataGroup.setImportId(importDataGroup.getId());
//        } else {
//            dataGroup.setImportId(transition.getImportId() + "_dg_" + index);
//        }
//
//        dataGroup.setLayout(new DataGroupLayout(importDataGroup));
//
//        dataGroup.setTitle(toI18NString(importDataGroup.getTitle()));
//        dataGroup.setAlignment(importDataGroup.getAlignment() != null ? importDataGroup.getAlignment() : null);
//        dataGroup.setStretch(importDataGroup.isStretch());
//        importDataGroup.getDataRef().forEach(dataRef -> dataGroup.addData(getField(dataRef.getId()).getStringId()));
//        transition.addDataGroup(dataGroup);
//        DataGroupLayout dataGroupLayout = dataGroup.getLayout() != null && dataGroup.getLayout().getType() != null ? dataGroup.getLayout() : null;
//
//        for (DataRef dataRef : importDataGroup.getDataRef()) {
//            if (dataGroupLayout != null && dataGroupLayout.getType().equals(LayoutType.GRID) && dataRef.getLayout() == null) {
//                throw new IllegalArgumentException("Data ref [" + dataRef.getId() + "] of data group [" + dataGroup.getStringId() + "] in transition [" + transition.getStringId() + "] doesn't have a layout.");
//            }
//            addDataLogic(transition, dataRef);
//            addDataLayout(transition, dataRef);
//            addDataComponent(transition, dataRef);
//        }
//    }
//
//    protected void addToTransaction(Transition transition, TransactionRef transactionRef) {
//        Transaction transaction = getTransaction(transactionRef.getId());
//        if (transaction == null) {
//            throw new IllegalArgumentException("Referenced transaction [" + transactionRef.getId() + "] in transition [" + transition.getTitle() + "] doesn't exist.");
//        }
//        transaction.addTransition(transition);
//    }

//    protected void addRoleLogic(Transition transition, com.netgrif.application.engine.importer.model.RoleRef roleRef) {
//        com.netgrif.application.engine.importer.model.RoleRefLogic logic = roleRef.getLogic();
//        String roleId = getRole(roleRef.getId()).getStringId();
//
//        if (logic == null || roleId == null) {
//            return;
//        }
//      TODO: NAE-1969
//        logicValidator.checkConflictingAttributes(logic, logic.isAssigned(), logic.isAssign(), "assigned", "assign");
//        logicValidator.checkDeprecatedAttributes(logic);
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
//
//        logicValidator.checkConflictingAttributes(logic, logic.isAssigned(), logic.isAssign(), "assigned", "assign");
//        logicValidator.checkDeprecatedAttributes(logic);
//
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
//    protected void addDataLayout(Transition transition, DataRef dataRef) {
//        Layout layout = dataRef.getLayout();
//        try {
//            Field<?> field = getField(dataRef.getId());
//            String fieldId = field.getStringId();
//            if (layout == null || fieldId == null) {
//                return;
//            }
//
//            String template = DEFAULT_FIELD_TEMPLATE;
//            if (layout.getTemplate() != null) {
//                template = layout.getTemplate().toString();
//            }
//
//            String appearance = DEFAULT_FIELD_APPEARANCE;
//            if (layout.getAppearance() != null) {
//                appearance = layout.getAppearance().toString();
//            }
//
//            String alignment = DEFAULT_FIELD_ALIGNMENT;
//            if (layout.getAlignment() != null) {
//                alignment = layout.getAlignment().value();
//            }
//
//            FieldLayout fieldLayout = new FieldLayout(layout.getX(), layout.getY(), layout.getRows(), layout.getCols(), layout.getOffset(), template, appearance, alignment);
//            transition.setDataRefLayout(field, fieldLayout);
//        } catch (NullPointerException e) {
//            throw new IllegalArgumentException("Wrong dataRef id [" + dataRef.getId() + "] on transition [" + transition.getTitle() + "]", e);
//        }
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
//    protected Map<com.netgrif.application.engine.importer.model.DataEventType, DataEvent> buildEvents(String fieldId, List<com.netgrif.application.engine.importer.model.DataEvent> events, String transitionId) {
//        Map<com.netgrif.application.engine.importer.model.DataEventType, DataEvent> parsedEvents = new HashMap<>();
//
//        List<com.netgrif.application.engine.importer.model.DataEvent> filteredEvents = events.stream()
//                .filter(event -> com.netgrif.application.engine.importer.model.DataEventType.GET.toString().equalsIgnoreCase(event.getType().toString()))
//                .collect(Collectors.toList());
//        if (!filteredEvents.isEmpty()) {
//            parsedEvents.put(com.netgrif.application.engine.importer.model.DataEventType.GET, parseDataEvent(fieldId, filteredEvents, transitionId));
//        }
//
//        filteredEvents = events.stream().filter(event -> com.netgrif.application.engine.importer.model.DataEventType.SET.toString().equalsIgnoreCase(event.getType().toString()))
//                .collect(Collectors.toList());
//        if (!filteredEvents.isEmpty()) {
//            parsedEvents.put(com.netgrif.application.engine.importer.model.DataEventType.SET, parseDataEvent(fieldId, filteredEvents, transitionId));
//        }
//
//        return parsedEvents;
//    }
//
//    protected com.netgrif.application.engine.petrinet.domain.events.DataEvent parseDataEvent(String fieldId, List<com.netgrif.application.engine.importer.model.DataEvent> events, String transitionId) {
//        com.netgrif.application.engine.petrinet.domain.events.DataEvent dataEvent = new com.netgrif.application.engine.petrinet.domain.events.DataEvent();
//        events.forEach(event -> {
//            dataEvent.setType(event.getType() == com.netgrif.application.engine.importer.model.DataEventType.GET ? com.netgrif.application.engine.importer.model.DataEventType.GET : com.netgrif.application.engine.importer.model.DataEventType.SET);
//            if (dataEvent.getId() == null) {
//                dataEvent.setId(event.getId());
//            }
//            if (dataEvent.getMessage() == null && event.getMessage() != null) {
//                dataEvent.setMessage(toI18NString(event.getMessage()));
//            }
//            event.getActions().forEach(action -> {
//                com.netgrif.application.engine.importer.model.EventPhaseType phaseType = action.getPhase();
//                if (action.getPhase() == null) {
//                    phaseType = event.getType().toString().equalsIgnoreCase(com.netgrif.application.engine.importer.model.DataEventType.GET.toString()) ? com.netgrif.application.engine.importer.model.EventPhaseType.PRE : com.netgrif.application.engine.importer.model.EventPhaseType.POST;
//                }
//                List<Action> parsedPhaseActions = parsePhaseActions(fieldId, phaseType, dataEvent.getType(), transitionId, event);
//                if (phaseType == com.netgrif.application.engine.importer.model.EventPhaseType.PRE) {
//                    dataEvent.getPreActions().addAll(parsedPhaseActions);
//                } else {
//                    dataEvent.getPostActions().addAll(parsedPhaseActions);
//                }
//            });
//        });
//        return dataEvent;
//    }
//
//    protected List<Action> buildActions(List<com.netgrif.application.engine.importer.model.Action> imported, String fieldId, String transitionId) {
//        return imported.stream()
//                .map(action -> parseAction(fieldId, transitionId, action))
//                .collect(Collectors.toList());
//    }

//    protected Action parseAction(String transitionId, com.netgrif.application.engine.importer.model.Action action) {
//        if (action.getValue().contains("f.this")) {
//            throw new IllegalArgumentException("Event action can not reference field using 'this'");
//        }
//        return parseAction(null, transitionId, action);
//    }
//
//    protected Action parseAction(String fieldId, String transitionId, com.netgrif.application.engine.importer.model.Action importedAction) {
//        if (fieldId != null && importedAction.getTrigger() == null) {
//            throw new IllegalArgumentException("Data field action [" + importedAction.getValue() + "] doesn't have trigger");
//        }
//        try {
//            Action action = createAction(importedAction);
//            parseIds(fieldId, transitionId, importedAction, action);
//            actions.put(action.getImportId(), action);
//            return action;
//        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException("Error parsing ids of action [" + importedAction.getValue() + "]", e);
//        }
//    }
//
//    //    // TODO: release/8.0.0 add atribute "type" to set actions
//    protected Action createAction(com.netgrif.application.engine.importer.model.Action importedAction) {
//        Action action = new Action();
//        if (importedAction.getTrigger() != null) {
//            action.setTrigger(com.netgrif.application.engine.importer.model.DataEventType.fromValue(importedAction.getTrigger().toLowerCase()));
//        }
//        action.setImportId(buildActionId(importedAction.getId()));
//        return action;
//    }
//
//    protected String buildActionId(String importedActionId) {
//        String sanitizedImportedId;
//        if (importedActionId != null) {
//            if (actions.containsKey(this.net.getIdentifier() + "-" + importedActionId)) {
//                throw new IllegalArgumentException("Duplicate action id, action with id [" + importedActionId + "] already exists in petri net with identifier [" + this.net.getIdentifier() + "]");
//            }
//            sanitizedImportedId = importedActionId;
//        } else {
//            sanitizedImportedId = new ObjectId().toString();
//        }
//        return this.net.getIdentifier() + "-" + sanitizedImportedId;
//    }
//
//    protected void parseIds(String fieldId, String transitionId, com.netgrif.application.engine.importer.model.Action importedAction, Action action) {
//        String definition = importedAction.getValue();
//        action.setDefinition(definition);
//
//        if (containsParams(definition)) {
//            parseParamsAndObjectIds(action, fieldId, transitionId);
//        }
//        actionValidator.validateAction(action.getDefinition());
//    }
//
//    protected void parseParamsAndObjectIds(Action action, String fieldId, String transitionId) {
//        String[] actionParts = action.getDefinition().split(";", 2);
//        action.setDefinition(actionParts[1]);
//        parseObjectIds(action, fieldId, transitionId, actionParts[0]);
//    }
//
//    protected boolean containsParams(String definition) {
//        return definition.matches("[\\W\\w\\s]*[\\w]*:[\\s]*[ft].[\\w]+;[\\w\\W\\s]*");
//    }
//
//    protected void parseObjectIds(Action action, String fieldId, String transitionId, String definition) {
//        try {
//            Map<String, String> ids = parseParams(definition);
//
//            ids.entrySet().forEach(entry -> replaceImportId(action, fieldId, transitionId, entry));
//        } catch (NullPointerException e) {
//            throw new IllegalArgumentException("Failed to parse action: " + action, e);
//        }
//    }
//
//    protected void replaceImportId(Action action, String fieldId, String transitionId, Map.Entry<String, String> entry) {
//        String[] parts = entry.getValue().split("[.]");
//        if (parts.length != 2) {
//            throw new IllegalArgumentException("Can not parse id of " + entry.getValue());
//        }
//        String key = parts[0];
//        String importId = parts[1];
//        String paramName = entry.getKey().trim();
//
//        if (importId.startsWith("this")) {
//            if (Objects.equals(key.trim(), FIELD_KEYWORD)) {
//                action.addFieldId(paramName, fieldId);
//                return;
//            }
//            if (Objects.equals(key.trim(), TRANSITION_KEYWORD)) {
//                action.addTransitionId(paramName, transitionId);
//                return;
//            }
//        }
//        if (Objects.equals(key.trim(), FIELD_KEYWORD)) {
//            action.addFieldId(paramName, getFieldId(importId));
//            return;
//        }
//        if (Objects.equals(key.trim(), TRANSITION_KEYWORD)) {
//            action.addTransitionId(paramName, importId);
//            return;
//        }
//        throw new IllegalArgumentException("Object " + key + "." + importId + " not supported");
//    }
//
//    protected Map<String, String> parseParams(String definition) {
//        List<String> params = Arrays.asList(definition.split(","));
//        return params.stream()
//                .map(param -> param.split(":"))
//                .collect(Collectors.toMap(o -> o[0], o -> o[1]));
//    }
//
//    protected String getFieldId(String importId) {
//        try {
//            return getField(importId).getStringId();
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Object f." + importId + " does not exists");
//        }
//    }
//
//    protected void createTrigger(Transition transition, com.netgrif.application.engine.importer.model.Trigger importTrigger) {
//        com.netgrif.application.engine.workflow.domain.triggers.Trigger trigger = triggerFactory.buildTrigger(importTrigger);
//
//        transition.addTrigger(trigger);
//    }
//
//    protected void createPlace(com.netgrif.application.engine.importer.model.Place importPlace) {
//        Place place = new Place();
//        place.setImportId(importPlace.getId());
//        // TODO: release/8.0.0 static place
////        if (importPlace.isStatic() == null) {
////            place.setIsStatic(importPlace.isIsStatic());
////        } else {
////            place.setIsStatic(importPlace.isStatic());
////        }
//        place.setTokens(importPlace.getTokens());
//        place.setPosition(importPlace.getX(), importPlace.getY());
//        place.setTitle(toI18NString(importPlace.getTitle()));
//
//        net.addPlace(place);
//        places.put(importPlace.getId(), place);
//    }
//
//    protected void createRole(com.netgrif.application.engine.importer.model.Role importRole) {
//        if (importRole.getId().equals(ProcessRole.DEFAULT_ROLE)) {
//            throw new IllegalArgumentException("Role ID '" + ProcessRole.DEFAULT_ROLE + "' is a reserved identifier, roles with this ID cannot be defined!");
//        }
//
//        if (importRole.getId().equals(ProcessRole.ANONYMOUS_ROLE)) {
//            throw new IllegalArgumentException("Role ID '" + ProcessRole.ANONYMOUS_ROLE + "' is a reserved identifier, roles with this ID cannot be defined!");
//        }
//
//        ProcessRole role = new ProcessRole();
//        role.setImportId(importRole.getId());
//        role.setName(toI18NString(importRole.getTitle()));
//        role.setEvents(createEventsMap(importRole.getEvent()));
//
//        role.setNetId(net.getStringId());
//        net.addRole(role);
//    }
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

//    public I18nString toI18NString(com.netgrif.application.engine.importer.model.I18NStringType imported) {
//        if (imported == null) {
//            return new I18nString("");
//        }
//        String key = imported.getValue();
//        if (imported.getId() != null) {
//            key = imported.getId();
//        }
//        if (imported instanceof com.netgrif.application.engine.importer.model.Option) {
//            key = ((com.netgrif.application.engine.importer.model.Option) imported).getKey();
//        }
//        I18nString string = i18n.getOrDefault(imported.getId(), new I18nString(key, imported.getValue()));
//        if (string.getDefaultValue() == null) {
//            string.setDefaultValue(imported.getValue());
//        }
//        return string;
//    }
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
//    protected com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy toAssignPolicy(com.netgrif.application.engine.importer.model.AssignPolicy policy) {
//        if (policy == null || policy.value() == null) {
//            return com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy.MANUAL;
//        }
//
//        return com.netgrif.application.engine.petrinet.domain.policies.AssignPolicy.valueOf(policy.value().toUpperCase());
//    }
//
//    protected com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy toFinishPolicy(com.netgrif.application.engine.importer.model.FinishPolicy policy) {
//        if (policy == null || policy.value() == null) {
//            return com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy.MANUAL;
//        }
//
//        return com.netgrif.application.engine.petrinet.domain.policies.FinishPolicy.valueOf(policy.value().toUpperCase());
//    }
//
//    public ProcessRole getRole(String id) {
//        ProcessRole role = roles.get(id);
//        if (role == null) {
//            throw new IllegalArgumentException("Role " + id + " not found");
//        }
//        return role;
//    }
//
//    public Field<?> getField(String id) {
//        Field<?> field = fields.get(id);
//        if (field == null) {
//            throw new IllegalArgumentException("Field " + id + " not found");
//        }
//        return field;
//    }
//
//    public I18nString getI18n(String id) {
//        return i18n.get(id);
//    }
//
//    protected static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
//        try (FileOutputStream outputStream = new FileOutputStream(file)) {
//            int read;
//            byte[] bytes = new byte[1024];
//            while ((read = inputStream.read(bytes)) != -1) {
//                outputStream.write(bytes, 0, read);
//            }
//        }
//    }
//
//    protected void setMetaData() throws MissingPetriNetMetaDataException {
//        List<String> missingMetaData = new ArrayList<>();
//
//        if (process.getId() != null) {
//            net.setImportId(process.getId());
//            net.setIdentifier(process.getId());
//        } else {
//            missingMetaData.add("<id>");
//        }
//        if (process.getVersion() != null) {
//            net.setVersion(Version.of(process.getVersion()));
//        } else {
//            missingMetaData.add("<version>");
//        }
//        // TODO: NAE-1969 extension from NAE-1973
//        if (process.getTitle() != null) {
//            net.setTitle(toI18NString(process.getTitle()));
//        } else {
//            missingMetaData.add("<title>");
//        }
//        net.setIcon(process.getIcon());
//        if (!missingMetaData.isEmpty()) {
//            throw new MissingPetriNetMetaDataException(missingMetaData);
//        }
//    }
//
//    protected void createProperties(com.netgrif.application.engine.importer.model.Properties propertiesXml, Map<String, String> properties) {
//        propertiesXml.getProperty().forEach(property -> {
//            properties.put(property.getKey(), property.getValue());
//        });
//    }
}
