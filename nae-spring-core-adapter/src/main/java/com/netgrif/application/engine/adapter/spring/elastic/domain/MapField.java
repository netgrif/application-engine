package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.springframework.data.elasticsearch.annotations.FieldType.*;

@NoArgsConstructor
public class MapField extends com.netgrif.application.engine.objects.elastic.domain.MapField {

    public MapField(Map.Entry<String, Collection<String>> valuePair) {
        super(valuePair);
    }

    public MapField(List<Map.Entry<String, Collection<String>>> valuePairs) {
        super(valuePairs);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Keyword)
    public String[] getKeyValue() {
        return super.getKeyValue();
    }
}
