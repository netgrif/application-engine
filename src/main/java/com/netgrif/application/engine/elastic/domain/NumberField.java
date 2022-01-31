package com.netgrif.application.engine.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Double;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NumberField extends DataField {

    @Field(type = Double)
    public Double numberValue;

    public NumberField(Double value) {
        super(value.toString());
        this.numberValue = value;
    }
}