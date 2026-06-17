package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.springframework.data.elasticsearch.annotations.FieldType.*;

@NoArgsConstructor
public class MapField extends com.netgrif.application.engine.objects.elastic.domain.MapField {

    public MapField(MapField field) {
        super(field);
    }

    public MapField(Map.Entry<String, I18nString> valuePair) {
        super(valuePair);
    }

    public MapField(List<Map.Entry<String, I18nString>> valuePairs) {
        super(valuePairs);
    }

    @Override
    @Field(type = Text)
    public List<String> getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Keyword)
    public List<String> getKeyValue() {
        return super.getKeyValue();
    }

    @Field(type = Flattened, index = false)
    public Map<String, I18nString> getKeyValueTranslations() {
        return super.getKeyValueTranslations();
    }
}
