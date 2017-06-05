package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

public class Arc extends PetriNetObject {

    @Transient
    protected Node source;

    @Getter
    @Setter
    protected ObjectId sourceId;

    @Transient
    protected Node destination;

    @Getter
    @Setter
    protected ObjectId destinationId;

    @Getter
    @Setter
    protected int multiplicity;

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
        return (source instanceof Place) ? ((Place) source) : ((Place) destination);
    }

    public Transition getTransition() {
        return (source instanceof Transition) ? ((Transition) source) : ((Transition) destination);
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

    @Override
    public String toString() {
        return source.getTitle() + " -(" + multiplicity + ")> " + destination.getTitle();
    }

    public boolean isExecutable() {
        if (source instanceof Transition)
            return true;
        return ((Place) source).getTokens() >= multiplicity;
    }

    public void execute() {
        if (source instanceof Transition) {
            ((Place) destination).addTokens(multiplicity);
        } else {
            ((Place) source).removeTokens(multiplicity);
        }
    }

    public void rollbackExecution() {
        ((Place) source).addTokens(multiplicity);
    }
}