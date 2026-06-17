package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Boolean;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class BooleanField extends com.netgrif.application.engine.objects.elastic.domain.BooleanField {

    @Field(type = Boolean)
    public Boolean booleanValue;

    public BooleanField(BooleanField field) {
        super(field);
        this.booleanValue = field.booleanValue;
    }

    public BooleanField(Boolean value) {
        super(value);
        this.booleanValue = value;
    }

    @Override
    @Field(type = Text)
    public List<String> getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Boolean)
    public Boolean getBooleanValue() {
        return super.getBooleanValue();
    }
}
