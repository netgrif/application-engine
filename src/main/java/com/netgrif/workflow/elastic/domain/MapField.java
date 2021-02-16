package com.netgrif.workflow.elastic.domain;

import com.netgrif.workflow.petrinet.domain.I18nString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MapField extends TextField {

    @Field(type = FieldType.Keyword)
    public String[] keyValue;

    public MapField(Map.Entry<String, I18nString> valuePair) {
        super(valuePair.getValue().toString());
        this.keyValue = new String[1];
        this.keyValue[0] = valuePair.getKey();
    }

    public MapField(List<Map.Entry<String, I18nString>> valuePairs) {
        super(new String[valuePairs.size()]);
        this.keyValue = new String[valuePairs.size()];
        for (int i = 0; i < valuePairs.size(); i++) {
            keyValue[i] = valuePairs.get(i).getKey();
            super.textValue[i] = valuePairs.get(i).getValue().toString();
            super.fulltextValue[i] = valuePairs.get(i).getValue().toString();
        }
    }
}
