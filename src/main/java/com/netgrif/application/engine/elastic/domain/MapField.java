package com.netgrif.application.engine.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MapField extends TextField {

    @Field(type = Keyword)
    public List<String> keyValue = new ArrayList<>();

    public MapField(String key, List<String> values) {
        super(values);
        this.keyValue.add(key);
    }

    public MapField(Map<String, List<String>> valuePairs) {
        super();
        valuePairs.forEach((key, value) -> {
            this.keyValue.add(key);
            this.textValue.addAll(value);
            this.fulltextValue.addAll(value);
        });
    }
}
