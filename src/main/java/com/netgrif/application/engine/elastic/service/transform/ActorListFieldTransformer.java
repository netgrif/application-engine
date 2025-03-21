package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.ActorField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.ActorListField;
import com.netgrif.application.engine.petrinet.domain.dataset.ActorListFieldValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActorListFieldTransformer extends ElasticDataFieldTransformer<ActorListField, ActorField> {

    public ActorListFieldTransformer() {
    }

    @Override
    public ActorField transform(ActorListField caseField, ActorListField petriNetField) {
        ActorListFieldValue value = caseField.getValue().getValue();
        if (value == null || value.getActorValues() == null || value.getActorValues().isEmpty()) {
            return null;
        }
        List<ActorField.ActorMappingData> userData = value.getActorValues().stream()
                .map(actor -> new ActorField.ActorMappingData(
                        actor.getId(),
                        actor.getEmail(),
                        actor.getFullName()
                ))
                .collect(Collectors.toList());
        return new ActorField(userData);
    }

    @Override
    public DataType getType() {
        return DataType.ACTOR_LIST;
    }
}
