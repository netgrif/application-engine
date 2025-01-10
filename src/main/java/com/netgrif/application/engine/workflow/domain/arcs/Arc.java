package com.netgrif.application.engine.workflow.domain.arcs;

import com.netgrif.application.engine.importer.model.Scope;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.workflow.domain.*;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class Arc<S extends Node, D extends Node> extends CaseElement {

    @Transient
    protected S source;
    protected String sourceId;
    @Transient
    protected D destination;
    protected String destinationId;
    protected Multiplicity multiplicityExpression;
    protected List<Position> breakpoints;
    protected UniqueKeyMap<String, String> properties;
    private Scope scope;

    public Arc() {
        this.setObjectId(new ObjectId());
        this.breakpoints = new ArrayList<>();
        this.properties = new UniqueKeyMap<>();
    }

    public Arc(S source, D destination, int multiplicity) {
        this();
        this.setSource(source);
        this.setDestination(destination);
        this.multiplicityExpression = new Multiplicity(multiplicity);
    }

    public Arc(S source, D destination, String multiplicity) {
        this();
        this.setSource(source);
        this.setDestination(destination);
        this.multiplicityExpression = new Multiplicity(multiplicity);
    }

    public abstract void execute();

    public Place getPlace() {
        return (source instanceof Place) ? ((Place) source) : ((Place) destination);
    }

    public Transition getTransition() {
        return (source instanceof Transition) ? ((Transition) source) : ((Transition) destination);
    }

    public void setSource(S source) {
        this.source = source;
        this.sourceId = source.getImportId();
    }

    public void setDestination(D destination) {
        this.destination = destination;
        this.destinationId = destination.getImportId();
    }

    public int getMultiplicity() {
        return this.multiplicityExpression.getMultiplicity();
    }

    public void setMultiplicity(int weight) {
        this.multiplicityExpression.setMultiplicity(weight);
    }

    @Override
    public String toString() {
        return source.getTitle() + " -(" + multiplicityExpression + ")> " + destination.getTitle();
    }
}