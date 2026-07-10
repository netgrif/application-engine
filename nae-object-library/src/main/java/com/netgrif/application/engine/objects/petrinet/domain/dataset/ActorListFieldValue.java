package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ActorListFieldValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 5228212326431238485L;
    private Set<ActorFieldValue> actorValues;

    public ActorListFieldValue() {
        this(new LinkedHashSet<>());
    }

    public ActorListFieldValue(Collection<ActorFieldValue> actorValues) {
        this(new LinkedHashSet<>(actorValues));
    }

    public ActorListFieldValue(LinkedHashSet<ActorFieldValue> actorValues) {
        this.actorValues = actorValues;
    }

    public LinkedHashSet<ActorFieldValue> getActorValues() {
        return (LinkedHashSet<ActorFieldValue>) actorValues;
    }

    public void setActorValues(Collection<ActorFieldValue> actorValues) {
        this.actorValues = new LinkedHashSet<>(actorValues);
    }

    @Override
    public String toString() {
        return actorValues.toString();
    }
}
