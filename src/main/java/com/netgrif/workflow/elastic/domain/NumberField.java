package com.netgrif.workflow.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NumberField extends DataField {

    @Field(type = FieldType.Double)
    public Double numberValue;

    public NumberField(Double value) {
        super(value.toString());
        this.numberValue = value;
    }
}