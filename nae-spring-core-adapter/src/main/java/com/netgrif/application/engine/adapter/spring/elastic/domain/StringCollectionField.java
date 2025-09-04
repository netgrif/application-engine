package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;


import static org.springframework.data.elasticsearch.annotations.FieldType.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StringCollectionField  extends com.netgrif.application.engine.objects.elastic.domain.StringCollectionField {

    @Field(type = Nested)
    public String[] collectionValue;

    public StringCollectionField(String[] values) {
        super(values);
        this.collectionValue = values;
    }

    @Override
    @Field(type = Nested)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

}
