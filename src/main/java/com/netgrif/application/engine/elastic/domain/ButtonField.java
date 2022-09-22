package com.netgrif.application.engine.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Integer;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ButtonField extends DataField {

    @Field(type = Integer)
    public Integer buttonValue;

    public ButtonField(Integer value) {
        super(value.toString());
        this.buttonValue = value;
    }
}