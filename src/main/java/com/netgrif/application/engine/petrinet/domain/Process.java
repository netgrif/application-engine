package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.domain.arcs.Arc;
import com.netgrif.application.engine.workflow.domain.arcs.ArcCollection;
import com.netgrif.application.engine.workflow.domain.arcs.PTArc;
import com.netgrif.application.engine.workflow.domain.arcs.TPArc;
import com.netgrif.application.engine.workflow.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.workflow.domain.events.CaseEvent;
import com.netgrif.application.engine.workflow.domain.events.ProcessEvent;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.CasePermission;
import com.netgrif.application.engine.workflow.domain.version.Version;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Document
@CompoundIndex(name = "cmp-idx-one", def = "{'identifier': 1, 'version.major': -1, 'version.minor': -1, 'version.patch': -1}")
public class Process extends ProcessObject {

    @Indexed
    private String identifier;
    private Version version;
    private List<PetriNetIdentifier> parentIdentifiers;
    private I18nString title;
    private String icon;
    private I18nExpression defaultCaseName;
    // TODO: release/8.0.0 - default + anonymous role, roleref
    private UniqueKeyMap<String, Map<CasePermission, Boolean>> permissions;
    private Map<ProcessEventType, ProcessEvent> processEvents;
    private Map<CaseEventType, CaseEvent> caseEvents;
    @DBRef
    private UniqueKeyMap<String, ProcessRole> roles;
    private List<Function> functions;
    private UniqueKeyMap<String, Field<?>> dataSet;
    private UniqueKeyMap<String, Transition> transitions;
    private UniqueKeyMap<String, Place> places;
    private UniqueKeyMap<String, ArcCollection> arcs;//todo: import id
    private UniqueKeyMap<String, String> properties;

    // TODO: 18. 3. 2017 replace with Spring auditing
    private LocalDateTime creationDate;
    private String authorId;
    private String importXmlPath;
    private String uriNodeId;

    public Process() {
        this.id = new ObjectId();
        this.identifier = "Default";
        this.title = new I18nString("");
        this.importId = "";
        this.version = new Version();
        defaultCaseName = new I18nExpression("");
        parentIdentifiers = new ArrayList<>();
        creationDate = LocalDateTime.now();
        places = new UniqueKeyMap<>();
        transitions = new UniqueKeyMap<>();
        arcs = new UniqueKeyMap<>();
        dataSet = new UniqueKeyMap<>();
        roles = new UniqueKeyMap<>();
        processEvents = new HashMap<>();
        caseEvents = new HashMap<>();
        permissions = new UniqueKeyMap<>();
        functions = new LinkedList<>();
        properties = new UniqueKeyMap<>();
    }

    public void addParentIdentifier(PetriNetIdentifier identifier) {
        parentIdentifiers.add(identifier);
    }

    public void addPlace(Place place) {
        this.places.put(place.getStringId(), place);
    }

    public void addTransition(Transition transition) {
        this.transitions.put(transition.getStringId(), transition);
    }

    public void addRole(ProcessRole role) {
        this.roles.put(role.getStringId(), role);
    }

    public void addPermission(String actorId, Map<CasePermission, Boolean> permissions) {
        if (this.permissions.containsKey(actorId) && this.permissions.get(actorId) != null) {
            this.permissions.get(actorId).putAll(permissions);
        } else {
            this.permissions.put(actorId, permissions);
        }
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public void addProcessEvent(ProcessEvent processEvent) {
        processEvents.put(processEvent.getType(), processEvent);
    }

    public void addCaseEvent(CaseEvent caseEvent) {
        caseEvents.put(caseEvent.getType(), caseEvent);
    }

    public List<PTArc> getInputArcsOf(String transitionId) {
        if (arcs.containsKey(transitionId)) {
            return arcs.get(transitionId).getInput();
        }
        return new LinkedList<>();
    }

    public List<TPArc> getOutputArcsOf(String transitionId) {
        if (arcs.containsKey(transitionId)) {
            return arcs.get(transitionId).getOutput();
        }
        return new LinkedList<>();
    }


    public void addDataSetField(Field<?> field) {
        this.dataSet.put(field.getStringId(), field);
    }

    public void addArc(Arc<?, ?> arc) {
        String transitionId = arc.getTransition().getStringId();
        ArcCollection arcCollection = arcs.get(transitionId);
        if (arcCollection == null) {
            arcCollection = new ArcCollection();
            arcs.put(transitionId, arcCollection);
        }
        if (arc instanceof PTArc) {
            arcCollection.addInput((PTArc) arc);
        } else {
            arcCollection.addOutput((TPArc) arc);
        }
    }

    public Node getNode(String importId) {
        if (places.containsKey(importId)) {
            return getPlace(importId);
        }
        if (transitions.containsKey(importId)) {
            return getTransition(importId);
        }
        return null;
    }

    public Optional<Field<?>> getField(String id) {
        return Optional.ofNullable(dataSet.get(id));
    }

    public Place getPlace(String id) {
        return places.get(id);
    }

    public Transition getTransition(String id) {
        // TODO: release/8.0.0 change
        if ("fake".equals(id)) {
            return new Transition();
        }
        return transitions.get(id);
    }

    public void initializeArcs() {
        arcs.values().forEach(list -> {
            list.getOutput().forEach(arc -> {
                arc.setSource(getTransition(arc.getSourceId()));
                arc.setDestination(getPlace(arc.getDestinationId()));
            });
            list.getInput().forEach(arc -> {
                arc.setSource(getPlace(arc.getSourceId()));
                arc.setDestination(getTransition(arc.getDestinationId()));
            });
        });
    }

    public void initializeTokens(Map<String, Integer> activePlaces) {
        places.values().forEach(place -> place.setTokens(activePlaces.getOrDefault(place.getStringId(), 0)));
    }

    public Map<String, Integer> getActivePlaces() {
        return places.values().stream()
                .filter(Place::hasAnyTokens)
                .collect(Collectors.toMap(ProcessObject::getStringId, Place::getTokens));
    }

    public void setActivePlaces(Map<String, Integer> activePlaces) {
        places.forEach((id, place) -> {
            Integer marking = activePlaces.getOrDefault(id, 0);
            place.setTokens(marking);
        });
    }

    public List<Field<?>> getImmediateFields() {
        return this.dataSet.values().stream().filter(Field::isImmediate).collect(Collectors.toList());
    }

    public void incrementVersion(VersionType type) {
        this.version.increment(type);
    }

    @Override
    public String toString() {
        return title.toString();
    }

    public String getTranslatedDefaultCaseName(Locale locale) {
        if (defaultCaseName == null) {
            return "";
        }
        return defaultCaseName.getTranslation(locale);
    }

    public List<Function> getFunctions(Scope scope) {
        return functions.stream().filter(function -> function.getScope().equals(scope)).collect(Collectors.toList());
    }

    public List<Action> getPreCreateActions() {
        return getPreCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPostCreateActions() {
        return getPostCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPreDeleteActions() {
        return getPreCaseActions(CaseEventType.DELETE);
    }

    public List<Action> getPostDeleteActions() {
        return getPostCaseActions(CaseEventType.DELETE);
    }

    public List<Action> getPreUploadActions() {
        return getPreProcessActions(ProcessEventType.UPLOAD);
    }

    public List<Action> getPostUploadActions() {
        return getPostProcessActions(ProcessEventType.UPLOAD);
    }

    private List<Action> getPreCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type)) {
            return caseEvents.get(type).getPreActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPostCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type)) {
            return caseEvents.get(type).getPostActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPreProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type)) {
            return processEvents.get(type).getPreActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPostProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type)) {
            return processEvents.get(type).getPostActions();
        }
        return new LinkedList<>();
    }

    public ProcessRole getRole(String id) {
        return roles.get(id);
    }

    @Override
    public String getStringId() {
        return id.toString();
    }

    public Process clone() {
        Process clone = new Process();
        clone.setId(this.id);
        clone.setIdentifier(this.identifier);
        clone.setParentIdentifiers(this.parentIdentifiers.stream().map(PetriNetIdentifier::clone).collect(Collectors.toList()));
        clone.setUriNodeId(this.uriNodeId);
        clone.setTitle(this.title.clone());
        clone.setDefaultCaseName(this.defaultCaseName == null ? null : this.defaultCaseName.clone());
        clone.setIcon(this.icon);
        clone.setCreationDate(this.creationDate);
        clone.setVersion(this.version == null ? null : this.version.clone());
        clone.setTransitions(this.transitions == null ? null : this.transitions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (v1, v2) -> v1, UniqueKeyMap::new)));
        clone.setRoles(this.roles == null ? null : this.roles.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (v1, v2) -> v1, UniqueKeyMap::new)));
        clone.setImportXmlPath(this.importXmlPath);
        clone.setImportId(this.importId);
        clone.setDataSet(this.dataSet.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x, y) -> y, UniqueKeyMap::new))
        );
        clone.setPlaces(this.places.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x, y) -> y, UniqueKeyMap::new))
        );
        clone.setArcs(this.arcs.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x, y) -> y, UniqueKeyMap::new))
        );
        clone.initializeArcs();
        clone.setCaseEvents(this.caseEvents == null ? null : this.caseEvents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setProcessEvents(this.processEvents == null ? null : this.processEvents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        // TODO: release/8.0.0
//        clone.setPermissions(this.permissions == null ? null : this.permissions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new UniqueKeyMap<>(e.getValue()))));
        this.getFunctions().forEach(clone::addFunction);
        clone.setProperties(new UniqueKeyMap<>(this.properties));
        return clone;
    }
}