package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class I18nField extends com.netgrif.application.engine.objects.elastic.domain.I18nField {

    public I18nField(Set<String> keys, Set<String> values) {
        super(keys, values);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Text)
    public String[] getKeyValue() {
        return super.getKeyValue();
    }
}
