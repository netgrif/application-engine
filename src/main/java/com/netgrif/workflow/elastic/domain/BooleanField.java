package com.netgrif.workflow.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BooleanField extends DataField {

    @Field(type = FieldType.Boolean)
    public Boolean booleanValue;

    public BooleanField(Boolean value) {
        super(value.toString());
        this.booleanValue = value;
    }
}