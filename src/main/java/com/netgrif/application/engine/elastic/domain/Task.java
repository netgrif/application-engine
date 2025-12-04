package com.netgrif.application.engine.elastic.domain;

import com.netgrif.application.engine.workflow.domain.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class Task {

    @Field(type = Text)
    public String stringId;

    @Field(type = Text)
    public State state;

    @Field(type = Text)
    public String userId;

}