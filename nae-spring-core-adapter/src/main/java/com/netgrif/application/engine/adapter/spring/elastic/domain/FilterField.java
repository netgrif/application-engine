package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Map;

import static org.springframework.data.elasticsearch.annotations.FieldType.*;

@NoArgsConstructor
public class FilterField extends com.netgrif.application.engine.objects.elastic.domain.FilterField {

    public FilterField(FilterField field) {
        super(field);
    }

    public FilterField(String value, String[] allowedNets, Map<String, Object> filterMetadata) {
        super(value, allowedNets, filterMetadata);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Text)
    public String[] getAllowedNets() {
        return super.allowedNets;
    }

    @Field(type = Flattened)
    public Map<String, Object> getFilterMetadata() {
        return super.filterMetadata;
    }
}
