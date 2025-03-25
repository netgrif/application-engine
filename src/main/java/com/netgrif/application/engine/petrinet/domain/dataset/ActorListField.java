package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActorListField extends FieldWithAllowedRoles<ActorListFieldValue> {

    public ActorListField() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.ACTOR_LIST;
    }

    @Override
    public ActorListField clone() {
        ActorListField clone = new ActorListField();
        super.clone(clone);
        return clone;
    }
}
