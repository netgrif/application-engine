package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class ActorListFieldValue {

    private LinkedHashSet<ActorFieldValue> actorValues;

    public ActorListFieldValue(Collection<ActorFieldValue> actorValues) {
        this.actorValues = new LinkedHashSet<>(actorValues);
    }

    public ActorListFieldValue(ActorFieldValue actorValue) {
        this(Set.of(actorValue));
    }

    @Override
    public String toString() {
        if (actorValues == null) {
            return "";
        }
        return actorValues.toString();
    }
}
