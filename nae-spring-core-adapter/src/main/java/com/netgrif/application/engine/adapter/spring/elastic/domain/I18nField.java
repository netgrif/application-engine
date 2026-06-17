package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.Flattened;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class I18nField extends com.netgrif.application.engine.objects.elastic.domain.I18nField {

    public I18nField(I18nField field) {
        super(field);
    }

    public I18nField(Set<String> keys, Set<String> values, Map<String, String> translations) {
        super(keys, values, translations);
    }

    @Override
    @Field(type = Text)
    public List<String> getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Text)
    public Set<String> getKeyValue() {
        return super.getKeyValue();
    }

    @Field(type = Flattened)
    public Map<String,String> getTranslations() {
        return super.getTranslations();
    }
}
