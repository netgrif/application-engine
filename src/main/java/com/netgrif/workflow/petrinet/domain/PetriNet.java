package com.netgrif.workflow.petrinet.domain;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Document
public class PetriNet {

    @Id
    private ObjectId _id;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String initials;

    // TODO: 18. 3. 2017 replace with Spring auditing
    @Getter @Setter
    private LocalDateTime creationDate;

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

    public PetriNet() {
        initialized = false;
        creationDate = LocalDateTime.now();
        places = new HashMap<>();
        transitions = new HashMap<>();
        arcs = new HashMap<>();
        dataSet = new HashMap<>();
        roles = new HashMap<>();
        transactions = new HashMap<>();
    }

    public PetriNet(String title, String initials) {
        this();
        this.title = title;
        this.initials = initials;
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getStringId() {
        return _id.toString();
    }

    public void addPlace(Place place) {
        this.places.put(place.getObjectId().toString(), place);
    }

    public void addTransition(Transition transition) {
        this.transitions.put(transition.getObjectId().toString(), transition);
    }

    public void addRole(ProcessRole role) {
        this.roles.put(role.getObjectId(), role);
    }

    public List<Arc> getArcsOfTransition(Transition transition) {
        return getArcsOfTransition(transition.getObjectId().toString());
    }

    public List<Arc> getArcsOfTransition(String transitionId) {
        return arcs.get(transitionId);
    }

    public void addDataSetField(Field field) {
        this.dataSet.put(field.getObjectId(), field);
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

    @Override
    public String toString() {
        return title;
    }
}