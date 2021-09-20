package com.netgrif.workflow.filters

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class PredicateValue {

    String text
    @JacksonXmlElementWrapper(localName = "stringValues")
    @JacksonXmlProperty(localName = "stringValue")
    List<String> stringValues
    @JacksonXmlElementWrapper(localName = "integerValues")
    @JacksonXmlProperty(localName = "integerValue")
    List<Integer> integerValues

    PredicateValue(Map<String, Object> value) {
        value.forEach({ k, v ->
            switch (k) {
                case "text":
                    text = (String) v
                    break
                case "value":
                    v = (List) v
                    if (v.get(0) instanceof String) {
                        stringValues = new ArrayList<>()
                        for (def val : v) {
                            stringValues.add((String) val)
                        }
                    } else if (v.get(0) instanceof Integer) {
                        integerValues = new ArrayList<>()
                        for (def val : v) {
                            integerValues.add((Integer) val)
                        }
                    }
                    break
                case "stringValues":
                    stringValues = new ArrayList<>()
                    for (def val : v) {
                        stringValues.add(val as String)
                    }
                    break
                case "integerValues":
                    integerValues = new ArrayList<>()
                    for (def val : v) {
                        integerValues.add(val as Integer)
                    }
                    break
            }
        })
    }

    @JsonIgnore
    Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>()
        mapObject.put("text", text)
        if (integerValues != null) {
            mapObject.put("value", integerValues)
        } else if (stringValues != null) {
            mapObject.put("value", stringValues)
        }
        return mapObject
    }
}
