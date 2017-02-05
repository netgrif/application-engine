package com.fmworkflow.petrinet.domain;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Document
public class PetriNet {
    @Id
    private ObjectId _id;
    private String title;
    private DateTime creationDate;
    @Field("places")
    private Map<String, Place> places;
    @Field("transitions")
    private Map<String, Transition> transitions;
    @Field("arcs")
    private Map<String, List<Arc>> arcs;
    @Transient
    private Set<Arc> arcsSkeleton;

    public PetriNet() {
        creationDate = DateTime.now();
        this.places = new HashMap<>();
        this.transitions = new HashMap<>();
        this.arcs = new HashMap<>();
        this.arcsSkeleton = new HashSet<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void setArcs(Map<String, List<Arc>> arcs) {
        this.arcs = arcs;
    }

    public void addArcSkelet(Arc arc) {
        arcsSkeleton.add(arc);
    }

    public void addArc(Arc arc) {
        String placeId = arc.getPlace().getObjectId().toString();
        if (arcs.containsKey(placeId))
            arcs.get(placeId).add(arc);
        else {
            List<Arc> arcList = new LinkedList<>();
            arcList.add(arc);
            arcs.put(placeId, arcList);
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
        for (Arc arc : this.arcsSkeleton) {
            arc.setSource(getNode(arc.getSourceId()));
            arc.setDestination(getNode(arc.getDestinationId()));
            addArc(arc);
        }
    }

    @Override
    public String toString() {
        return title;
    }
}
