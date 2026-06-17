package com.netgrif.application.engine.objects.workflow.domain.menu.dashboard;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.objects.workflow.domain.menu.ToDataSetOutcome;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;


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
