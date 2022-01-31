package com.netgrif.application.engine.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Boolean;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BooleanField extends DataField {

    @Field(type = Boolean)
    public Boolean booleanValue;

    public BooleanField(Boolean value) {
        super(value.toString());
        this.booleanValue = value;
    }
}