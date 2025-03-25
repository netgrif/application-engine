package com.netgrif.application.engine.petrinet.domain.dataset;


import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActorField extends FieldWithAllowedRoles<ActorFieldValue> {

    public ActorField() {
        this(new HashSet<>());
    }

    public ActorField(Set<String> values) {
        super(values);
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.ACTOR;
    }

    @Override
    public ActorField clone() {
        ActorField clone = new ActorField();
        super.clone(clone);
        return clone;
    }
}