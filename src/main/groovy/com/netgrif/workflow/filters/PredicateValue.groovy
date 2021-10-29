package com.netgrif.workflow.filters

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * Class holds values of some search predicates (mainly searching for author).
 * Values can be integer of author id or other search predicate as a text (<<me>>).
 */

class PredicateValue {

    String text
    @JacksonXmlElementWrapper(localName = "stringValues")
    @JacksonXmlProperty(localName = "stringValue")
    List<String> stringValues
    @JacksonXmlElementWrapper(localName = "doubleValues")
    @JacksonXmlProperty(localName = "doubleValue")
    List<Double> doubleValues

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
                    } else if (v.get(0) instanceof Integer || v.get(0) instanceof Double || v.get(0) instanceof Float) {
                        doubleValues = new ArrayList<>()
                        for (def val : v) {
                            doubleValues.add((Double) val)
                        }
                    }
                    break
                case "stringValues":
                    stringValues = new ArrayList<>()
                    for (def val : v) {
                        stringValues.add(val as String)
                    }
                    break
                case "doubleValues":
                    doubleValues = new ArrayList<>()
                    for (def val : v) {
                        doubleValues.add(val as Double)
                    }
                    break
            }
        })
    }

    @JsonIgnore
    Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>()
        mapObject.put("text", text)
        if (doubleValues != null) {
            mapObject.put("value", doubleValues)
        } else if (stringValues != null) {
            mapObject.put("value", stringValues)
        }
        return mapObject
    }
}
