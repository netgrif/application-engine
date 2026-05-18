package com.netgrif.application.engine.menu.domain;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationTemplateOutcome {
    /**
     * Map of field data where the key is field ID and the value is field value.
     */
    public final Map<String, Object> mapping;

    public ConfigurationTemplateOutcome() {
        this.mapping = new HashMap<>();
    }

    public ConfigurationTemplateOutcome(ToDataSetOutcome toDataSetOutcome) {
        this();
        toDataSetOutcome.getDataSet()
                .forEach((fieldId, fieldMap) -> this.mapping.put(fieldId, fieldMap.get("value")));
    }

}
