package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Arrays;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StringCollectionField extends com.netgrif.application.engine.objects.elastic.domain.StringCollectionField {

    @Deprecated
    public StringCollectionField(String[] values) {
        super(Arrays.asList(values));
    }

    public StringCollectionField(StringCollectionField field) {
        super(field);
    }

    public StringCollectionField(List<String> values) {
        super(values);
    }

    @Override
    @Field(type = Text)
    public List<String> getFulltextValue() {
        return super.getFulltextValue();
    }

    @Override
    @Field(type = Keyword)
    public List<String> getCollectionValue() {
        return super.getCollectionValue();
    }

}
