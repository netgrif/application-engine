package com.fmworkflow.petrinet.domain;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

public class Arc extends PetriNetObject {
    @Transient
    private Node source;
    private ObjectId sourceId;
    @Transient
    private Node destination;
    private ObjectId destinationId;
    private int multiplicity;

    public Arc() {
        this.setObjectId(new ObjectId());
    }

    public Arc(Node source, Node destination, int multiplicity) {
        this();
        this.setSource(source);
        this.setDestination(destination);
        this.multiplicity = multiplicity;
    }

    public Place getPlace() {
        return (source instanceof Place)?((Place) source):((Place) destination);
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
        this.sourceId = source.getObjectId();
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
        this.destinationId = destination.getObjectId();
    }

    public int getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(int multiplicity) {
        this.multiplicity = multiplicity;
    }

    public ObjectId getSourceId() {
        return sourceId;
    }

    public void setSourceId(ObjectId sourceId) {
        this.sourceId = sourceId;
    }

    public ObjectId getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(ObjectId destinationId) {
        this.destinationId = destinationId;
    }

    @Override
    public String toString() {
        return source.getTitle() + " -(" + multiplicity + ")> " + destination.getTitle();
    }

    public enum Type {
        regular,
        inhibitor,
        reset
    }
}
