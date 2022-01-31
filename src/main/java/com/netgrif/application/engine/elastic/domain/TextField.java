package com.netgrif.application.engine.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextField extends DataField {

    @Field(type = Text)
    public String[] textValue;

    public TextField(String value) {
        super(value);
        this.textValue = new String[1];
        this.textValue[0] = value;
    }

    public TextField(String[] values) {
        super(values);
        this.textValue = values;
    }
}