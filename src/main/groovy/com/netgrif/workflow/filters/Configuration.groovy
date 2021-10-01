package com.netgrif.workflow.filters

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Class represents configuration part on predicate.
 * Class holds operator, which provides operation for value comparison.
 * When creating filter for datafield value, datafield attribute is also used,
 * to represent which datafield from which process is used in comparison.
 */

class Configuration {
    String operator
    String datafield

    Configuration(Map<String, Object> value) {
        value.forEach({ k, v ->
            switch(k) {
                case "operator":
                    operator = (String) v
                    break
                case "datafield":
                    datafield = (String) v
                    break
            }
        })
    }

    @JsonIgnore
    Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>()
        mapObject.put("operator", operator)
        if (datafield != null) {
            mapObject.put("datafield", datafield)
        }
        return mapObject
    }
}
