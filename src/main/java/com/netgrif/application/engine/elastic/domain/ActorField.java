package com.netgrif.application.engine.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActorField extends DataField {

    @Field(type = Text)
    private List<String> emailValue = new ArrayList<>();

    @Field(type = Text)
    private List<String> fullNameValue = new ArrayList<>();

    @Field(type = Text)
    private List<String> actorIdValue = new ArrayList<>();

    public ActorField(ActorMappingData value) {
        super();
        this.addValue(value);
    }

    public ActorField(List<ActorMappingData> values) {
        super();
        values.forEach(this::addValue);
    }

    protected void addValue(ActorMappingData value) {
        this.emailValue.add(value.email);
        this.fullNameValue.add(value.fullName);
        this.actorIdValue.add(value.actorId);
        super.fulltextValue.add(String.format("%s %s", value.fullName, value.email));
    }

    @AllArgsConstructor
    public static class ActorMappingData {
        public String actorId;
        public String email;
        public String fullName;
    }
}


