package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
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
    private I18nString title;

    @Getter @Setter
    private String initials;

    @Getter @Setter
    private String icon;

    // TODO: 18. 3. 2017 replace with Spring auditing
    @Getter @Setter
    private LocalDateTime creationDate;

    @Getter @Setter
    private Author author;

    @org.springframework.data.mongodb.core.mapping.Field("places")
    @Getter @Setter
    private Map<String, Place> places;

    @org.springframework.data.mongodb.core.mapping.Field("transitions")
    @Getter @Setter
    private Map<String, Transition> transitions;

    @org.springframework.data.mongodb.core.mapping.Field("arcs")
    @Getter @Setter
    private Map<String, List<Arc>> arcs;

    @org.springframework.data.mongodb.core.mapping.Field("dataset")
    @Getter @Setter
    private Map<String, Field> dataSet;

    @org.springframework.data.mongodb.core.mapping.Field("roles")
    @DBRef
    @Getter @Setter
    private Map<String, ProcessRole> roles;

    @org.springframework.data.mongodb.core.mapping.Field("transactions")
    @Getter @Setter
    private Map<String, Transaction> transactions;

    @Transient
    private boolean initialized;

    @Getter @Setter
    private String importXmlPath;

    public PetriNet() {
        initialized = false;
        creationDate = LocalDateTime.now();
        places = new HashMap<>();
        transitions = new HashMap<>();
        arcs = new HashMap<>();
        dataSet = new LinkedHashMap<>();
        roles = new HashMap<>();
        transactions = new LinkedHashMap<>();
    }

    public PetriNet(String title, String initials) {
        this();
        setTitle(title);
        this.initials = initials;
    }

    public void addPlace(Place place) {
        this.places.put(place.getObjectId().toString(), place);
    }

    public void addTransition(Transition transition) {
        this.transitions.put(transition.getObjectId().toString(), transition);
    }

    public void addRole(ProcessRole role) {
        this.roles.put(role.getStringId(), role);
    }

    public List<Arc> getArcsOfTransition(Transition transition) {
        return getArcsOfTransition(transition.getObjectId().toString());
    }

    public List<Arc> getArcsOfTransition(String transitionId) {
        return arcs.get(transitionId);
    }

    public void addDataSetField(Field field) {
        this.dataSet.put(field.getStringId(), field);
    }

    public boolean isNotInitialized() {
        return !initialized;
    }

    public void addArc(Arc arc) {
        String transitionId = arc.getTransition().getObjectId().toString();
        if (arcs.containsKey(transitionId))
            arcs.get(transitionId).add(arc);
        else {
            List<Arc> arcList = new LinkedList<>();
            arcList.add(arc);
            arcs.put(transitionId, arcList);
        }
    }

    public Node getNode(ObjectId id) {
        String stringId = id.toString();
        if (places.containsKey(stringId))
            return getPlace(stringId);
        if (transitions.containsKey(stringId))
            return getTransition(stringId);
        return null;
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

    public Map<String, Integer> getActivePlaces() {
        Map<String, Integer> activePlaces = new HashMap<>();
        for (Place place : places.values()) {
            if (place.getTokens() > 0) {
                activePlaces.put(place.getObjectId().toString(), place.getTokens());
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

    public List<Field> getImmediateFields(){
        return this.dataSet.values().stream().filter(Field::isImmediate).collect(Collectors.toList());
    }

    public boolean isDisplayableInAnyTransition(String fieldId){
        return transitions.values().stream().parallel().anyMatch(trans -> trans.isDisplayable(fieldId));
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
}