package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.elastic.domain.ActorMappingData;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Arrays;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class ActorField extends com.netgrif.application.engine.objects.elastic.domain.ActorField {

    public ActorField(ActorField field) {
        super(field);
    }

    public ActorField(ActorMappingData value) {
        super(value);
    }

    @Deprecated
    public ActorField(ActorMappingData[] values) {
        this(Arrays.asList(values));
    }

    public ActorField(List<ActorMappingData> values) {
        super(values);
    }

    @Override
    @Field(type = Text)
    public List<String> getFulltextValue() {
        return super.getFulltextValue();
    }

    @Override
    @Field(type = Text)
    public List<String> getUsernameValue() {
        return super.getUsernameValue();
    }

    @Override
    @Field(type = Text)
    public List<String> getFullNameValue() {
        return super.getFullNameValue();
    }

    @Override
    @Field(type = Text)
    public List<String> getActorIdValue() {
        return super.getActorIdValue();
    }

    @Override
    @Field(type = Text)
    public List<String> getActorRealmIdValue() {
        return super.getActorRealmIdValue();
    }
}


