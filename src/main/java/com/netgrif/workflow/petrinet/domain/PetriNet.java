package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.petrinet.domain.arcs.Arc;
import com.netgrif.workflow.petrinet.domain.arcs.VariableArc;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.petrinet.domain.events.CaseEvent;
import com.netgrif.workflow.petrinet.domain.events.CaseEventType;
import com.netgrif.workflow.petrinet.domain.events.ProcessEvent;
import com.netgrif.workflow.petrinet.domain.events.ProcessEventType;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.workflow.petrinet.domain.version.Version;
import com.netgrif.workflow.workflow.domain.DataField;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Document
public class PetriNet extends PetriNetObject {

    @Getter
    @Setter
    private String identifier; //combination of identifier and version must be unique ... maybe use @CompoundIndex?

    @Getter
    private I18nString title;

    @Getter
    @Setter
    private I18nString defaultCaseName;

    @Getter
    @Setter
    private String initials;

    @Getter
    @Setter
    private String icon;

    // TODO: 18. 3. 2017 replace with Spring auditing
    @Getter
    @Setter
    private LocalDateTime creationDate;

    @Getter
    @Setter
    private Version version;

    @Getter
    @Setter
    private Author author;

    @org.springframework.data.mongodb.core.mapping.Field("places")
    @Getter
    @Setter
    private Map<String, Place> places;

    @org.springframework.data.mongodb.core.mapping.Field("transitions")
    @Getter
    @Setter
    private Map<String, Transition> transitions;

    @org.springframework.data.mongodb.core.mapping.Field("arcs")
    @Getter
    @Setter
    private Map<String, List<Arc>> arcs;//todo: import id

    @org.springframework.data.mongodb.core.mapping.Field("dataset")
    @Getter
    @Setter
    private Map<String, Field> dataSet;

    @org.springframework.data.mongodb.core.mapping.Field("roles")
    @DBRef
    @Getter
    @Setter
    private Map<String, ProcessRole> roles;

    @org.springframework.data.mongodb.core.mapping.Field("transactions")
    @Getter
    @Setter
    private Map<String, Transaction> transactions;//todo: import id

    @Getter
    @Setter
    private Map<ProcessEventType, ProcessEvent> processEvents;

    @Getter
    @Setter
    private Map<CaseEventType, CaseEvent> caseEvents;

    @Getter @Setter
    private Map<String, Set<ProcessRolePermission>> processRoles;

    @Transient
    private boolean initialized;

    @Getter
    @Setter
    private String importXmlPath;

    public PetriNet() {
        this._id = new ObjectId();
        this.identifier = "Default";
        this.initials = "";
        this.title = new I18nString("");
        this.importId = "";
        this.version = new Version();
        defaultCaseName = new I18nString("");
        initialized = false;
        creationDate = LocalDateTime.now();
        places = new HashMap<>();
        transitions = new HashMap<>();
        arcs = new HashMap<>();
        dataSet = new LinkedHashMap<>();
        roles = new HashMap<>();
        transactions = new LinkedHashMap<>();
        processEvents = new LinkedHashMap<>();
        caseEvents = new LinkedHashMap<>();
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

    public void addProcessRole(String roleId, Set<ProcessRolePermission> permissions) {
        if (processRoles.containsKey(roleId) && processRoles.get(roleId) != null) {
            processRoles.get(roleId).addAll(permissions);
        } else {
            processRoles.put(roleId, permissions);
        }
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

    public void addDataSetField(Field field) {
        this.dataSet.put(field.getStringId(), field);
    }

    public boolean isNotInitialized() {
        return !initialized;
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

    public Optional<Field> getField(String id) {
        return Optional.ofNullable(dataSet.get(id));
    }

    public Place getPlace(String id) {
        return places.get(id);
    }

    public Transition getTransition(String id) {
        return transitions.get(id);
    }

    public void initializeArcs() {
        arcs.values().forEach(list -> list.forEach(arc -> {
            arc.setSource(getNode(arc.getSourceId()));
            arc.setDestination(getNode(arc.getDestinationId()));
        }));
        initialized = true;
    }

    public void initializeTokens(Map<String, Integer> activePlaces) {
        places.values().forEach(place -> place.setTokens(activePlaces.getOrDefault(place.getStringId(), 0)));
    }

    public void initializeVarArcs(Map<String, DataField> dataSet) {
        arcs.values()
                .stream()
                .flatMap(List::stream)
                .filter(arc -> arc instanceof VariableArc)
                .forEach(arc -> {
                    VariableArc varc = (VariableArc) arc;
                    String fieldId = varc.getFieldId();
                    DataField field = dataSet.get(fieldId);
                    varc.setField(field);
                });
    }

    public Map<String, Integer> getActivePlaces() {
        Map<String, Integer> activePlaces = new HashMap<>();
        for (Place place : places.values()) {
            if (place.getTokens() > 0) {
                activePlaces.put(place.getStringId(), place.getTokens());
            }
        }
        return activePlaces;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.put(transaction.getStringId(), transaction);
    }

    public Transaction getTransactionByTransition(Transition transition) {
        return transactions.values().stream()
                .filter(transaction ->
                        transaction.getTransitions().contains(transition.getStringId())
                ).findAny().orElse(null);
    }

    public List<Field> getImmediateFields() {
        return this.dataSet.values().stream().filter(Field::isImmediate).collect(Collectors.toList());
    }

    public boolean isDisplayableInAnyTransition(String fieldId) {
        return transitions.values().stream().parallel().anyMatch(trans -> trans.isDisplayable(fieldId));
    }

    public void incrementVersion(VersionType type) {
        this.version.increment(type);
    }

    @Override
    public String toString() {
        return title.toString();
    }

    public void setTitle(I18nString title) {
        this.title = title;
    }

    public void setTitle(String title) {
        setTitle(new I18nString(title));
    }

    public String getTranslatedDefaultCaseName(Locale locale) {
        if (defaultCaseName == null) {
            return "";
        }
        return defaultCaseName.getTranslation(locale);
    }

    public String getTranslatedTitle(Locale locale) {
        if (title == null) {
            return "";
        }
        return title.getTranslation(locale);
    }

    public enum VersionType {
        MAJOR,
        MINOR,
        PATCH
    }

    public List<Action> getPreCreateActions() {
        return getPreCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPostCreateActions() {
        return getPostCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPreDeleteActions() {
        return getPreCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPostDeleteActions() {
        return getPostCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPreUploadActions() {
        return getPreProcessActions(ProcessEventType.UPLOAD);
    }

    public List<Action> getPostUploadActions() {
        return getPostProcessActions(ProcessEventType.UPLOAD);
    }

    private List<Action> getPreCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type))
            return caseEvents.get(type).getPreActions();
        return new LinkedList<>();
    }

    private List<Action> getPostCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type))
            return caseEvents.get(type).getPostActions();
        return new LinkedList<>();
    }

    private List<Action> getPreProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type))
            return processEvents.get(type).getPreActions();
        return new LinkedList<>();
    }

    private List<Action> getPostProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type))
            return processEvents.get(type).getPostActions();
        return new LinkedList<>();
    }

    @Override
    public String getStringId() {
        return _id.toString();
    }

    public PetriNet clone() {
        PetriNet clone = new PetriNet();
        clone.setIdentifier(this.identifier);
        clone.setInitials(this.initials);
        clone.setTitle(this.title);
        clone.setDefaultCaseName(this.defaultCaseName);
        clone.setIcon(this.icon);
        clone.setCreationDate(this.creationDate);
        clone.setVersion(this.version);
        clone.setAuthor(this.author);
        clone.setTransitions(this.transitions);
        clone.setRoles(this.roles);
        clone.setTransactions(this.transactions);
        clone.setImportXmlPath(this.importXmlPath);
        clone.setImportId(this.importId);
        clone.setObjectId(this._id);
        clone.setDataSet(this.dataSet.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone()))
        );
        clone.setPlaces(this.places.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone()))
        );
        clone.setArcs(this.arcs.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                        .map(arc -> arc.clone())
                        .collect(Collectors.toList())))
        );
        clone.initializeArcs();
        return clone;
    }
}