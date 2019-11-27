package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.petrinet.domain.arcs.Arc;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
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
    private String version; //in format 1.1.1 - MAJOR.MINOR.PATCH

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

    @Transient
    private boolean initialized;

    @Getter
    @Setter
    private String importXmlPath;

    public PetriNet() {
        this._id = new ObjectId();
        this.identifier = "Default";
        this.version = "1.0.0";
        defaultCaseName = new I18nString("");
        initialized = false;
        creationDate = LocalDateTime.now();
        places = new HashMap<>();
        transitions = new HashMap<>();
        arcs = new HashMap<>();
        dataSet = new LinkedHashMap<>();
        roles = new HashMap<>();
        transactions = new LinkedHashMap<>();
    }

    public PetriNet(String identifier, String title, String initials) {
        this();
        this.identifier = identifier;
        setTitle(title);
        this.initials = initials;
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

    public void initializeArcs(Map<String, DataField> dataSet) {
        arcs.values()
                .stream()
                .flatMap(List::stream)
                .forEach(arc -> {
                    if (arc.getReference() != null) {
                        String fieldId = varc.getFieldId();
                        DataField field = dataSet.get(fieldId);
                        varc.setField(field);
                    }
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
        List<Integer> versionParts = Arrays.stream(this.version.split("\\."))
                .map(Integer::parseInt).collect(Collectors.toList());

        if (type == VersionType.MAJOR) {
            versionParts.set(0, versionParts.get(0) + 1);
            versionParts.set(1, 0);
            versionParts.set(2, 0);
        } else if (type == VersionType.MINOR) {
            versionParts.set(1, versionParts.get(1) + 1);
            versionParts.set(2, 0);
        } else if (type == VersionType.PATCH) {
            versionParts.set(2, versionParts.get(2) + 1);
        }

        this.version = versionParts.get(0) + "." + versionParts.get(1) + "." + versionParts.get(2);
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