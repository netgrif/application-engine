package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.*;

@Data
@AllArgsConstructor
public class ToDataSetOutcome {
    private Map<String, Map<String, Object>> dataSet;
    /**
     * todo javadoc
     * */
    private ToDataSetOutcome associatedOutcome;

    public ToDataSetOutcome() {
        this.dataSet = new HashMap<>();
    }

    public ToDataSetOutcome(Map<String, Map<String, Object>> dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * todo javadoc
     * */
    public void putDataSetEntry(String fieldId, FieldType fieldType, @Nullable Object fieldValue) {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("type", fieldType.getName());
        fieldMap.put("value", fieldValue);
        this.dataSet.put(fieldId, fieldMap);
    }

    /**
     * todo javadoc
     * */
    public void putDataSetEntryOptions(String fieldId, FieldType fieldType, @Nullable Map<String, I18nString> options) {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("type", fieldType.getName());
        if (fieldType.equals(FieldType.MULTICHOICE_MAP)) {
            fieldMap.put("value", Set.of());
        }
        if (options == null) {
            options = new HashMap<>();
        }
        fieldMap.put("options", options);
        this.dataSet.put(fieldId, fieldMap);
    }
}
