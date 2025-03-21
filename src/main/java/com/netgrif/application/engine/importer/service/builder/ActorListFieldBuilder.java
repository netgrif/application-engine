package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.ActorListField;
import com.netgrif.application.engine.petrinet.domain.dataset.ActorListFieldValue;
import org.springframework.stereotype.Component;

@Component
public class ActorListFieldBuilder extends FieldWithAllowedRolesBuilder<ActorListField, ActorListFieldValue> {

    @Override
    public ActorListField build(Data data, Importer importer) {
        ActorListField field = new ActorListField();
        initialize(field);
        setRoles(field, data);
        setDefaultValue(field, data);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.ACTOR_LIST;
    }
}
