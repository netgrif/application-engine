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

    public ToDataSetOutcome() {
        this.dataSet = new HashMap<>();
    }

    /**
     * Puts provided value into {@link #dataSet} according to dataSet rules.
     *
     * @param fieldId importId of the field
     * @param fieldType type of the field
     * @param fieldValue new value of the field
     * */
    public void putDataSetEntry(String fieldId, FieldType fieldType, @Nullable Object fieldValue) {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("type", fieldType.getName());
        fieldMap.put("value", fieldValue);
        this.dataSet.put(fieldId, fieldMap);
    }

    /**
     * Puts provided options into {@link #dataSet} according to dataSet rules.
     *
     * @param fieldId importId of the field
     * @param fieldType type of the field
     * @param options new options of the field
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
