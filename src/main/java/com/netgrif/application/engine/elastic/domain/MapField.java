package com.netgrif.application.engine.elastic.domain;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.*;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MapField extends TextField {

    @Field(type = Keyword)
    public List<String> keyValue = new ArrayList<>();

    public List<String> options = new ArrayList<>();

    public MapField(String key, List<String> values, Map<String, I18nString> options) {
        super(values);
        this.keyValue.add(key);
        this.options.addAll(options.keySet());
    }

    public MapField(Map<String, List<String>> valuePairs, Map<String, I18nString> options) {
        super();
        valuePairs.forEach((key, value) -> {
            this.keyValue.add(key);
            this.textValue.addAll(value);
            this.fulltextValue.addAll(value);
        });
        this.options.addAll(options.keySet());
    }
}
