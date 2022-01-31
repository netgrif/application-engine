package com.netgrif.application.engine.workflow.domain.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Class represents configuration part on predicate.
 * Class holds operator, which provides operation for value comparison.
 * When creating filter for datafield value, datafield attribute is also used,
 * to represent which datafield from which process is used in comparison.
 */
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class Configuration {
    protected String operator;
    protected String datafield;

    public Configuration(Map<String, Object> value) {
        value.forEach((k, v) -> {
            switch (k) {
                case "operator":
                    operator = (String) v;
                    break;
                case "datafield":
                    datafield = (String) v;
                    break;
            }
        });
    }

    @JsonIgnore
    public Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("operator", operator);
        if (datafield != null) {
            mapObject.put("datafield", datafield);
        }
        return mapObject;
    }
}
