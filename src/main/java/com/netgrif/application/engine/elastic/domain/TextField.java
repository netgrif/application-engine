package com.netgrif.application.engine.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextField extends DataField {

    @Field(type = Text)
    public List<String> textValue = new ArrayList<>();

    public TextField(String value) {
        super(value);
        this.textValue.add(value);
    }

    public TextField(List<String> values) {
        super(values);
        this.textValue = values;
    }
}