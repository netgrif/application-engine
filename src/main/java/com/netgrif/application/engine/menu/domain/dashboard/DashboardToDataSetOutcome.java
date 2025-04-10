package com.netgrif.application.engine.menu.domain.dashboard;

import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DashboardToDataSetOutcome extends ToDataSetOutcome {
    public void putDataSetEntryWithOptions(String fieldId, FieldType fieldType, @Nullable Map<String, I18nString> options, @Nullable Object fieldValue){
        Map<String, Object> fieldMap = new LinkedHashMap<>();

        fieldMap.put("type", fieldType.getName());
        if (fieldType.equals(FieldType.MULTICHOICE_MAP)) {
            fieldMap.put("value", Set.of());
        }
        if (options == null) {
            options = new HashMap<>();
        }
        fieldMap.put("options", options);
        fieldMap.put("value", fieldValue);
        this.getDataSet().put(fieldId, fieldMap);
    }
}
