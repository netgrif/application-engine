package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ActorListField extends ActorField {

    public ActorListField(ActorListField field) {
        super(field);
        this.usernameValue = field.usernameValue == null ? null : new ArrayList<>(field.usernameValue);
        this.fullNameValue = field.fullNameValue == null ? null : new ArrayList<>(field.fullNameValue);
        this.actorIdValue = field.actorIdValue == null ? null : new ArrayList<>(field.actorIdValue);
        this.actorRealmIdValue = field.actorRealmIdValue == null ? null : new ArrayList<>(field.actorRealmIdValue);
    }

    public ActorListField(List<ActorMappingData> actorMappingDataList) {
        super(actorMappingDataList);
    }
}
