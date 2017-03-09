package com.fmworkflow.petrinet.domain;

import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.domain.roles.ProcessRole;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Document
public class PetriNet {
    @Id
    private ObjectId _id;
    private String title;
    private String initials;
    private LocalDateTime creationDate;
    @org.springframework.data.mongodb.core.mapping.Field("places")
    private Map<String, Place> places;
    @org.springframework.data.mongodb.core.mapping.Field("transitions")
    private Map<String, Transition> transitions;
    @org.springframework.data.mongodb.core.mapping.Field("arcs")
    private Map<String, List<Arc>> arcs;
    @org.springframework.data.mongodb.core.mapping.Field("dataset")
    private Map<String, Field> dataSet;
    @org.springframework.data.mongodb.core.mapping.Field("roles")
    @DBRef
    private Map<String, ProcessRole> roles;
    @Transient
    private boolean initialized;

    public PetriNet() {
        initialized = false;
        creationDate = LocalDateTime.now();
        this.places = new HashMap<>();
        this.transitions = new HashMap<>();
        this.arcs = new HashMap<>();
        this.dataSet = new HashMap<>();
        this.roles = new HashMap<>();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public Map<String, Place> getPlaces() {
        return places;
    }

    public void setPlaces(Map<String, Place> places) {
        this.places = places;
    }

    public void addPlace(Place place) {
        this.places.put(place.getObjectId().toString(), place);
    }

    public Map<String, Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<String, Transition> transitions) {
        this.transitions = transitions;
    }

    public void addTransition(Transition transition) {
        this.transitions.put(transition.getObjectId().toString(), transition);
    }

    public Map<String, List<Arc>> getArcs() {
        return arcs;
    }

    public Map<String, ProcessRole> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, ProcessRole> roles) {
        this.roles = roles;
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

    public void setArcs(Map<String, List<Arc>> arcs) {
        this.arcs = arcs;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, Field> getDataSet() {
        return dataSet;
    }

    public void setDataSet(Map<String, Field> dataSet) {
        this.dataSet = dataSet;
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

    public Map<Place, Integer> getInputPlaces(Transition transition) {
        return getIOPlaces(transition, arc -> arc.getDestination() == transition);
    }

    public Map<Place, Integer> getOutputPlaces(Transition transition) {
        return getIOPlaces(transition, arc -> arc.getSource() == transition);
    }

    private Map<Place, Integer> getIOPlaces(Transition transition, Predicate<Arc> predicate) {
        List<Arc> transitionsArcs = getArcsOfTransition(transition);
        if (transitionsArcs == null)
            return new HashMap<>();
        return transitionsArcs.stream()
                .filter(predicate)
                .collect(Collectors.toMap(Arc::getPlace, Arc::getMultiplicity));
    }

    @Override
    public String toString() {
        return title;
    }
}