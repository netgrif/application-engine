package com.netgrif.workflow.filters

import com.fasterxml.jackson.annotation.JsonIgnore

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
