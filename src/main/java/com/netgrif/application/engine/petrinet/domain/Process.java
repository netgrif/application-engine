package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.CaseEvent;
import com.netgrif.application.engine.petrinet.domain.events.ProcessEvent;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Document
public class Process extends ProcessObject {

    private String identifier;
    private Version version;
    // TODO: NAE-1969 extends - merge NAE-1973
    private I18nString title;
    private String icon;
    private I18nExpression defaultCaseName;
    // TODO: release/8.0.0 - default + anonymous role, roleref
    private Map<String, Map<ProcessRolePermission, Boolean>> permissions;
    private Map<ProcessEventType, ProcessEvent> processEvents;
    private Map<CaseEventType, CaseEvent> caseEvents;
    @DBRef
    private LinkedHashMap<String, ProcessRole> roles;
    private List<Function> functions;
    private LinkedHashMap<String, Field<?>> dataSet;
    private LinkedHashMap<String, Transition> transitions;
    private LinkedHashMap<String, Place> places;
    // TODO: release/8.0.0 save sorted by execution priority
    private LinkedHashMap<String, List<Arc>> arcs;//todo: import id
    private Map<String, String> properties;

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
        creationDate = LocalDateTime.now();
        places = new LinkedHashMap<>();
        transitions = new LinkedHashMap<>();
        arcs = new LinkedHashMap<>();
        dataSet = new LinkedHashMap<>();
        roles = new LinkedHashMap<>();
        processEvents = new LinkedHashMap<>();
        caseEvents = new LinkedHashMap<>();
        permissions = new HashMap<>();
        functions = new LinkedList<>();
        properties = new HashMap<>();
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

    public void addPermission(String actorId, Map<ProcessRolePermission, Boolean> permissions) {
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

    public List<Arc> getArcsOfTransition(Transition transition) {
        return getArcsOfTransition(transition.getStringId());
    }

    public List<Arc> getArcsOfTransition(String transitionId) {
        if (arcs.containsKey(transitionId)) {
            return arcs.get(transitionId);
        }
        return new LinkedList<>();
    }

    public void addDataSetField(Field<?> field) {
        this.dataSet.put(field.getStringId(), field);
    }

    public void addArc(Arc arc) {
        String transitionId = arc.getTransition().getStringId();
        if (arcs.containsKey(transitionId)) {
            arcs.get(transitionId).add(arc);
        } else {
            List<Arc> arcList = new LinkedList<>();
            arcList.add(arc);
            arcs.put(transitionId, arcList);
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
        arcs.values().forEach(list -> list.forEach(arc -> {
            arc.setSource(getNode(arc.getSourceId()));
            arc.setDestination(getNode(arc.getDestinationId()));
        }));
    }

    public void initializeTokens(Map<String, Integer> activePlaces) {
        places.values().forEach(place -> place.setTokens(activePlaces.getOrDefault(place.getStringId(), 0)));
    }

    public Map<String, Integer> getActivePlaces() {
        return places.values().stream()
                .filter(Place::hasAnyTokens)
                .collect(Collectors.toMap(ProcessObject::getStringId, Place::getTokens));
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

    public List<Function> getFunctions(FunctionScope scope) {
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
        clone.setUriNodeId(this.uriNodeId);
        clone.setTitle(this.title.clone());
        clone.setDefaultCaseName(this.defaultCaseName == null ? null : this.defaultCaseName.clone());
        clone.setIcon(this.icon);
        clone.setCreationDate(this.creationDate);
        clone.setVersion(this.version == null ? null : this.version.clone());
        clone.setTransitions(this.transitions == null ? null : this.transitions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (v1, v2) -> v1, LinkedHashMap::new)));
        clone.setRoles(this.roles == null ? null : this.roles.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (v1, v2) -> v1, LinkedHashMap::new)));
        clone.setImportXmlPath(this.importXmlPath);
        clone.setImportId(this.importId);
        clone.setDataSet(this.dataSet.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x, y) -> y, LinkedHashMap::new))
        );
        clone.setPlaces(this.places.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x, y) -> y, LinkedHashMap::new))
        );
        clone.setArcs(this.arcs.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                        .map(Arc::clone)
                        .collect(Collectors.toList()), (x, y) -> y, LinkedHashMap::new))
        );
        clone.initializeArcs();
        clone.setCaseEvents(this.caseEvents == null ? null : this.caseEvents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setProcessEvents(this.processEvents == null ? null : this.processEvents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setPermissions(this.permissions == null ? null : this.permissions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
        this.getFunctions().forEach(clone::addFunction);
        clone.setProperties(new HashMap<>(this.properties));
        return clone;
    }
}