package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Double;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class NumberField extends com.netgrif.application.engine.objects.elastic.domain.NumberField {

    public NumberField(NumberField field) {
        super(field);
    }

    public NumberField(Double value) {
        super(value);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Override
    @Field(type = Double)
    public Double getNumberValue() {
        return super.getNumberValue();
    }
}
