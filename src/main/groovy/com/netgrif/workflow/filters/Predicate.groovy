package com.netgrif.workflow.filters

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class Predicate {
    String category
    Configuration configuration
    @JacksonXmlElementWrapper(localName = "stringValues")
    @JacksonXmlProperty(localName = "stringValue")
    List<String> stringValues
    @JacksonXmlElementWrapper(localName = "integerValues")
    @JacksonXmlProperty(localName = "integerValue")
    List<Integer> integerValues
    @JacksonXmlElementWrapper(localName = "booleanValues")
    @JacksonXmlProperty(localName = "booleanValue")
    List<Boolean> booleanValues
    @JacksonXmlElementWrapper(localName = "mapValues")
    @JacksonXmlProperty(localName = "mapValue")
    List<PredicateValue> mapValues
    @JacksonXmlElementWrapper(localName = "longValues")
    @JacksonXmlProperty(localName = "longValue")
    List<Long> longValues

    Predicate(Map<String, Object> value) {
        value.forEach({k, v ->
            switch (k) {
                case "category":
                    category = (String) v
                    break
                case "configuration":
                    configuration = new Configuration(v as Map<String, Object>)
                    break
                case "values":
                    v = (List) v
                    if (v.get(0) instanceof String) {
                        stringValues = new ArrayList<>()
                        for (def val : v) {
                            stringValues.add((String) val)
                        }
                    } else if (v.get(0) instanceof Boolean) {
                        booleanValues = new ArrayList<>()
                        for (def val : v) {
                            booleanValues.add((Boolean) val)
                        }
                    } else if (v.get(0) instanceof Integer) {
                        integerValues = new ArrayList<>()
                        for (def val : v) {
                            integerValues.add((Integer) val)
                        }
                    } else if (v.get(0) instanceof Long) {
                        longValues = new ArrayList<>()
                        for (def val : v) {
                            longValues.add((Long) val)
                        }
                    } else {
                        mapValues = new ArrayList<>()
                        for (def val : v) {
                            mapValues.add(new PredicateValue(val as Map<String, Object>))
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
                case "longValues":
                    longValues = new ArrayList<>()
                    for (def val : v) {
                        longValues.add(val as Long)
                    }
                    break
                case "booleanValues":
                    booleanValues = new ArrayList<>()
                    for (def val : v) {
                        booleanValues.add(Boolean.valueOf(val as String))
                    }
                    break
                case "mapValues":
                    mapValues = new ArrayList<>()
                    for (def val : v) {
                        mapValues.add(new PredicateValue(val as Map<String, Object>))
                    }
                    break
            }
        })
    }

    @JsonIgnore
    Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>()
        mapObject.put("category", category)
        mapObject.put("configuration", configuration.getMapObject())
        if (mapValues != null) {
            List<Object> tmpList = new ArrayList<>()
            for (def val : mapValues) {
                tmpList.add(val.getMapObject())
            }
            mapObject.put("values", tmpList)
        } else {
            if (stringValues != null) {
                mapObject.put("values", stringValues)
            } else if (integerValues != null) {
                mapObject.put("values", integerValues)
            } else if (booleanValues != null) {
                mapObject.put("values", booleanValues)
            } else if (longValues != null) {
                mapObject.put("values", longValues)
            }
        }
        return mapObject
    }
}
