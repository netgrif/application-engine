package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class MapField extends TextField {

    public String[] keyValue;

    public MapField(Map.Entry<String, Collection<String>> valuePair) {
        super(valuePair.getValue().toArray(new String[0]));
        this.keyValue = new String[1];
        this.keyValue[0] = valuePair.getKey();
    }

    public MapField(List<Map.Entry<String, Collection<String>>> valuePairs) {
        super(new String[0]);
        this.keyValue = new String[valuePairs.size()];
        List<String> values = new ArrayList<>();
        for (int i = 0; i < valuePairs.size(); i++) {
            keyValue[i] = valuePairs.get(i).getKey();
            values.addAll(valuePairs.get(i).getValue());
        }
        this.textValue = values.toArray(new String[0]);
        this.fulltextValue = values.toArray(new String[0]);
    }
}
