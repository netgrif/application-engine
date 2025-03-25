package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.ActorField;
import com.netgrif.application.engine.petrinet.domain.dataset.ActorFieldValue;
import org.springframework.stereotype.Component;

@Component
public class ActorFieldBuilder extends FieldWithAllowedRolesBuilder<ActorField, ActorFieldValue> {

    @Override
    public ActorField build(Data data, Importer importer) {
        ActorField field = new ActorField();
        initialize(field);
        setRoles(field, data);
        setDefaultValue(field, data);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.ACTOR;
    }
}
