package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ActorListField extends ActorField {

    public ActorListField(ActorListField field) {
        super(field);
    }

    public ActorListField(List<ActorMappingData> actorMappingDataList) {
        super(actorMappingDataList);
    }
}
