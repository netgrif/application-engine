package com.netgrif.application.engine.petrinet.domain.arcs;

import com.netgrif.application.engine.petrinet.domain.*;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Arc extends ProcessObject {

    @Transient
    protected Node source;
    protected String sourceId;
    @Transient
    protected Node destination;
    protected String destinationId;
    protected Multiplicity multiplicity;
    protected List<Position> breakpoints;
    protected Map<String, String> properties;

    public Arc() {
        this.setObjectId(new ObjectId());
        this.breakpoints = new ArrayList<>();
        this.properties = new HashMap<>();
    }

    public Arc(Node source, Node destination, int multiplicity) {
        this();
        this.setSource(source);
        this.setDestination(destination);
        this.multiplicity = new Multiplicity(multiplicity);
    }

    public Arc(Node source, Node destination, String multiplicity) {
        this();
        this.setSource(source);
        this.setDestination(destination);
        this.multiplicity = new Multiplicity(multiplicity);
    }

    public Place getPlace() {
        return (source instanceof Place) ? ((Place) source) : ((Place) destination);
    }

    public Transition getTransition() {
        return (source instanceof Transition) ? ((Transition) source) : ((Transition) destination);
    }

    public void setSource(Node source) {
        this.source = source;
        this.sourceId = source.getImportId();
    }

    public void setDestination(Node destination) {
        this.destination = destination;
        this.destinationId = destination.getImportId();
    }

    @Override
    public String toString() {
        return source.getTitle() + " -(" + multiplicity + ")> " + destination.getTitle();
    }

    @SuppressWarnings("Duplicates")
    public Arc clone() {
        Arc clone = new Arc();
        clone.setSourceId(this.sourceId);
        clone.setDestinationId(this.destinationId);
        clone.setBreakpoints(this.breakpoints);
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        clone.setMultiplicity(this.multiplicity.clone());
        return clone;
    }
}