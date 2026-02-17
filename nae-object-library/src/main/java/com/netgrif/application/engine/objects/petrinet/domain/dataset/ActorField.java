package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class ActorField extends Field<ActorFieldValue> {

    private Set<String> roles;

    public ActorField() {
        this(new HashSet<>());
    }

    public ActorField(String[] roles) {
        this(new HashSet<>(Arrays.asList(roles)));
    }

    public ActorField(Set<String> roles) {
        super();
        this.roles = roles == null ? new HashSet<>() : roles;
    }

    @Override
    public FieldType getType() {
        return FieldType.ACTOR;
    }

    @Override
    public Field<?> clone() {
        ActorField clone = new ActorField();
        super.clone(clone);
        clone.setRoles(this.roles);
        return clone;
    }
}
