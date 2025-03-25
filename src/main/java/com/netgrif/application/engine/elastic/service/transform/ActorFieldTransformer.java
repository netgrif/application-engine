package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.ActorField;
import com.netgrif.application.engine.petrinet.domain.dataset.ActorFieldValue;
import org.springframework.stereotype.Component;

@Component
public class ActorFieldTransformer extends ElasticDataFieldTransformer<ActorField, com.netgrif.application.engine.elastic.domain.ActorField> {

    @Override
    public com.netgrif.application.engine.elastic.domain.ActorField transform(ActorField caseField, ActorField petriNetField) {
        ActorFieldValue value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new com.netgrif.application.engine.elastic.domain.ActorField(
                new com.netgrif.application.engine.elastic.domain.ActorField.ActorMappingData(
                        value.getId(),
                        value.getEmail(),
                        value.getFullName()
                )
        );
    }

    @Override
    public DataType getType() {
        return DataType.USER;
    }
}
