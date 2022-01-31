package com.netgrif.application.engine.workflow.domain.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Predicate class represents one search predicate (search term).
 * In the xml document, this class is represented with <predicate> tag.
 * Depending on search category and configuration, there could be 5 different
 * types of values.
 * Same as the PredicateArray class, this one needs to be converted into map object
 * when importing filter.
 */
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class Predicate extends DoubleValueHolder {
    protected String category;
    protected Configuration configuration;
    @JacksonXmlElementWrapper(localName = "stringValues")
    @JacksonXmlProperty(localName = "stringValue")
    protected List<String> stringValues;
    @JacksonXmlElementWrapper(localName = "doubleValues")
    @JacksonXmlProperty(localName = "doubleValue")
    protected List<Double> doubleValues;
    @JacksonXmlElementWrapper(localName = "booleanValues")
    @JacksonXmlProperty(localName = "booleanValue")
    protected List<Boolean> booleanValues;
    @JacksonXmlElementWrapper(localName = "mapValues")
    @JacksonXmlProperty(localName = "mapValue")
    protected List<PredicateValue> mapValues;
    @JacksonXmlElementWrapper(localName = "longValues")
    @JacksonXmlProperty(localName = "longValue")
    protected List<Long> longValues;

    public Predicate(Map<String, Object> value) {
        value.forEach((k, v) -> {
            switch (k) {
                case "category":
                    category = (String) v;
                    break;
                case "configuration":
                    configuration = new Configuration((Map<String, Object>) v);
                    break;
                case "values":
                    List<?> list = (List<?>) v;
                    if (list.get(0) instanceof String) {
                        stringValues = new ArrayList<>();
                        for (Object val : list) {
                            stringValues.add((String) val);
                        }
                    } else if (list.get(0) instanceof Boolean) {
                        booleanValues = new ArrayList<>();
                        for (Object val : list) {
                            booleanValues.add((Boolean) val);
                        }
                    } else if (list.get(0) instanceof Integer || list.get(0) instanceof Double || list.get(0) instanceof Float) {
                        doubleValues = new ArrayList<>();
                        for (Object val : list) {
                            doubleValues.add(convertObjectToDouble(val));
                        }
                    }  else if (list.get(0) instanceof Long) {
                        longValues = new ArrayList<>();
                        for (Object val : list) {
                            longValues.add((Long) val);
                        }
                    } else {
                        mapValues = new ArrayList<>();
                        for (Object val : list) {
                            mapValues.add(new PredicateValue((Map<String, Object>) val));
                        }
                    }
                    break;
                case "stringValues":
                    stringValues = new ArrayList<>();
                    List<String> stringList = (List<String>) v;
                    stringValues.addAll(stringList);
                    break;
                case "doubleValues":
                    doubleValues = new ArrayList<>();
                    for (Object val :  (List<?>) v) {
                        doubleValues.add(convertObjectToDouble(val));
                    }
                    break;
                case "longValues":
                    longValues = new ArrayList<>();
                    for (Object val :  (List<?>) v) {
                        if (val instanceof Long) {
                            longValues.add((Long) val);
                            continue;
                        } else if (val instanceof String) {
                            longValues.add(Long.parseLong((String) val));
                            continue;
                        }
                        throw new IllegalArgumentException("The provided Object (" + val.toString() + ") cannot be converted to Long");
                    }
                    break;
                case "booleanValues":
                    booleanValues = new ArrayList<>();
                    for (Object val :  (List<?>) v) {
                        if (val instanceof Boolean) {
                            booleanValues.add((Boolean) val);
                            continue;
                        } else if (val instanceof String) {
                            booleanValues.add(Boolean.parseBoolean((String) val));
                            continue;
                        }
                        throw new IllegalArgumentException("The provided Object (" + val.toString() + ") cannot be converted to Boolean");
                    }
                    break;
                case "mapValues":
                    mapValues = new ArrayList<>();
                    List<Map<String, Object>> mapList = (List<Map<String, Object>>) v;
                    for (Map<String, Object> val : mapList) {
                        mapValues.add(new PredicateValue(val));
                    }
                    break;
            }
        });
    }

    @JsonIgnore
    public Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("category", category);
        mapObject.put("configuration", configuration.getMapObject());
        if (mapValues != null) {
            List<Object> tmpList = new ArrayList<>();
            for (PredicateValue val : mapValues) {
                tmpList.add(val.getMapObject());
            }
            mapObject.put("values", tmpList);
        } else {
            if (stringValues != null) {
                mapObject.put("values", stringValues);
            } else if (doubleValues != null) {
                mapObject.put("values", doubleValues);
            } else if (booleanValues != null) {
                mapObject.put("values", booleanValues);
            } else if (longValues != null) {
                mapObject.put("values", longValues);
            }
        }
        return mapObject;
    }
}
