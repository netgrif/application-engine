package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class ActorListField extends Field<ActorListFieldValue> {

    private Set<String> roles;

    public ActorListField() {
        this(new HashSet<>());
    }

    public ActorListField(String[] roles) {
        this(roles == null ? new HashSet<>() : new HashSet<>(Arrays.asList(roles)));
    }

    public ActorListField(Set<String> roles) {
        super();
        this.roles = roles == null ? new HashSet<>() : roles;
    }

    @Override
    public FieldType getType() {
        return FieldType.ACTORLIST;
    }

    @Override
    public Field<?> clone() {
        ActorListField clone = new ActorListField();
        super.clone(clone);
        clone.setRoles(this.roles == null ? null : new HashSet<>(this.roles));
        return clone;
    }
}
