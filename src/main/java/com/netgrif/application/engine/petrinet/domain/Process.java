package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.arcs.ArcCollection;
import com.netgrif.application.engine.petrinet.domain.arcs.PTArc;
import com.netgrif.application.engine.petrinet.domain.arcs.TPArc;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.CaseEvent;
import com.netgrif.application.engine.petrinet.domain.events.ProcessEvent;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.utils.UniqueKeyMapWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Document
@EqualsAndHashCode(callSuper = true)
@CompoundIndex(name = "cmp-idx-one", def = "{'identifier': 1, 'version.major': -1, 'version.minor': -1, 'version.patch': -1}")
public class Process extends ProcessObject {

    @Indexed
    private String identifier;
    private Version version;
    private List<PetriNetIdentifier> parentIdentifiers;
    private I18nString title;
    private String icon;
    private I18nExpression defaultCaseName;
    private AccessPermissions<CasePermission> processRolePermissions;
    private AccessPermissions<CasePermission> caseRolePermissions;
    private Map<ProcessEventType, ProcessEvent> processEvents;
    private Map<CaseEventType, CaseEvent> caseEvents;
    private List<Function> functions;
    private UniqueKeyMapWrapper<Field<?>> dataSet;
    private UniqueKeyMapWrapper<Transition> transitions;
    private UniqueKeyMapWrapper<Place> places;
    private UniqueKeyMapWrapper<ArcCollection> arcs;//todo: import id
    private UniqueKeyMapWrapper<String> properties;

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
        places = new UniqueKeyMapWrapper<>();
        transitions = new UniqueKeyMapWrapper<>();
        arcs = new UniqueKeyMapWrapper<>();
        dataSet = new UniqueKeyMapWrapper<>();
        processEvents = new HashMap<>();
        caseEvents = new HashMap<>();
        processRolePermissions = new AccessPermissions<>();
        caseRolePermissions = new AccessPermissions<>();
        functions = new LinkedList<>();
        properties = new UniqueKeyMapWrapper<>();
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

    public void addProcessRolePermission(String actorId, Map<CasePermission, Boolean> permissions) {
        this.processRolePermissions.addPermissions(actorId, permissions);
    }

    public void addCaseRolePermission(String userListId, Map<CasePermission, Boolean> permissions) {
        this.caseRolePermissions.addPermissions(userListId, permissions);
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
        return this.dataSet.values().stream()
                .filter((field) -> field.getImmediate() != null && field.getImmediate())
                .collect(Collectors.toList());
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
        clone.setTransitions(this.transitions == null ? null : new UniqueKeyMapWrapper<>(this.transitions));
        clone.setImportXmlPath(this.importXmlPath);
        clone.setImportId(this.importId);
        clone.setDataSet(new UniqueKeyMapWrapper<>(this.dataSet));
        clone.setPlaces(new UniqueKeyMapWrapper<>(this.places));
        clone.setArcs(new UniqueKeyMapWrapper<>(this.arcs));
        clone.initializeArcs();
        clone.setCaseEvents(this.caseEvents == null ? null : this.caseEvents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setProcessEvents(this.processEvents == null ? null : this.processEvents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setProcessRolePermissions(new AccessPermissions<>(this.processRolePermissions));
        clone.setCaseRolePermissions(new AccessPermissions<>(this.caseRolePermissions));
        this.getFunctions().forEach(clone::addFunction);
        clone.setProperties(new UniqueKeyMapWrapper<>(this.properties));
        return clone;
    }
}