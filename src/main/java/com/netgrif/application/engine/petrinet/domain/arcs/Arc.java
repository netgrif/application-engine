package com.netgrif.application.engine.petrinet.domain.arcs;

import com.netgrif.application.engine.petrinet.domain.*;
import com.netgrif.application.engine.petrinet.domain.arcs.reference.Reference;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

public class Arc extends PetriNetObject {

    @Transient
    protected Node source;

    @Getter
    @Setter
    protected String sourceId;

    @Transient
    protected Node destination;

    @Getter
    @Setter
    protected String destinationId;

    @Getter
    @Setter
    protected Integer multiplicity;

    @Getter
    @Setter
    protected Reference reference;

    @Getter
    @Setter
    protected List<Position> breakpoints;

    public Arc() {
        this.setObjectId(new ObjectId());
        this.breakpoints = new ArrayList<>();
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
        this.sourceId = source.getImportId();
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
        this.destinationId = destination.getImportId();
    }

    @Override
    public String toString() {
        return source.getTitle() + " -(" + multiplicity + ")> " + destination.getTitle();
    }

    public boolean isExecutable() {
        if (source instanceof Transition)
            return true;
        if (this.reference != null){
            this.multiplicity = this.reference.getMultiplicity();
        }
        return ((Place) source).getTokens() >= multiplicity;
    }

    public void execute() {
        if (reference != null) {
            multiplicity = reference.getMultiplicity();
        }
        if (source instanceof Transition) {
            ((Place) destination).addTokens(multiplicity);
        } else {
            ((Place) source).removeTokens(multiplicity);
        }
    }

    public void rollbackExecution(Integer tokensConsumed) {
        if (tokensConsumed == null && this.reference != null) {
            throw new IllegalArgumentException("Cannot rollback variable arc, because it was never executed");
        }
        if (this.reference == null) {
            tokensConsumed = multiplicity;
        }
        ((Place) source).addTokens(tokensConsumed);
    }

    @SuppressWarnings("Duplicates")
    public Arc clone() {
        Arc clone = new Arc();
        clone.setSourceId(this.sourceId);
        clone.setDestinationId(this.destinationId);
        clone.setMultiplicity(this.multiplicity);
        clone.setBreakpoints(this.breakpoints);
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        clone.setReference(this.reference == null ? null : this.reference.clone());
        return clone;
    }
}