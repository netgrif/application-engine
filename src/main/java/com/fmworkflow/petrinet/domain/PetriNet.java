package com.fmworkflow.petrinet.domain;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Document
public class PetriNet {
    @Id
    private ObjectId _id;
    private String title;
    private DateTime creationDate;
    @Field("places")
    private Set<Place> places;
    // TODO: 4. 2. 2017 use @Transient Map<String, Place> with @PersistenceConstructor
    @Field("transitions")
    private Set<Transition> transitions;
    // TODO: 4. 2. 2017 use @Transient Map<String, Transition> with @PersistenceConstructor
    @Field("arcs")
    private Set<Arc> arcs;

    public PetriNet() {
        creationDate = DateTime.now();
        this.places = new HashSet<>();
        this.transitions = new HashSet<>();
        this.arcs = new HashSet<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<Place> getPlaces() {
        return places;
    }

    public void setPlaces(Set<Place> places) {
        this.places = places;
    }

    public void addPlace(Place place) {
        this.places.add(place);
    }

    public Set<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Set<Transition> transitions) {
        this.transitions = transitions;
    }

    public void addTransition(Transition transition) {
        this.transitions.add(transition);
    }

    public Set<Arc> getArcs() {
        return arcs;
    }

    public void setArcs(Set<Arc> arcs) {
        this.arcs = arcs;
    }

    public void addArc(Arc arc) {
        this.arcs.add(arc);
    }

    public Node getNode(ObjectId id) {
        Optional<Place> p = places.stream().filter(place->place.getObjectId().equals(id)).findFirst();
        if (p.isPresent())
            return p.get();
        Optional<Transition> t = transitions.stream().filter(transition -> transition.getObjectId().equals(id)).findFirst();
        return t.orElse(null);
    }

    public void initializeArcs() {
        for (Arc arc : this.arcs) {
            arc.setSource(getNode(arc.getSourceId()));
            arc.setDestination(getNode(arc.getDestinationId()));
        }
    }

    @Override
    public String toString() {
        return title;
    }
}
