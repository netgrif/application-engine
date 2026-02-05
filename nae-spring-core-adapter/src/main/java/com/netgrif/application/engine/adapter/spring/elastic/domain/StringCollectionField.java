package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StringCollectionField extends com.netgrif.application.engine.objects.elastic.domain.StringCollectionField {

    public StringCollectionField(StringCollectionField field) {
        super(field);
    }

    public StringCollectionField(String[] values) {
        super(values);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Override
    @Field(type = Keyword)
    public String[] getCollectionValue() {
        return super.getCollectionValue();
    }

}
