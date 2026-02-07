package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Arrays;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextField extends com.netgrif.application.engine.objects.elastic.domain.TextField {

    public TextField(TextField field) {
        super(field);
    }

    public TextField(String value) {
        super(value);
    }

    @Deprecated
    public TextField(String[] values) {
        this(Arrays.asList(values));
    }

    public TextField(List<String> values) {
        super(values);
    }

    @Override
    @Field(type = Text)
    public List<String> getFulltextValue() {
        return super.getFulltextValue();
    }

    @Override
    @Field(type = Text)
    public List<String> getTextValue() {
        return super.getTextValue();
    }
}
