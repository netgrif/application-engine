package com.netgrif.workflow.workflow.domain.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class holds values of some search predicates (mainly searching for author).
 * Values can be integer of author id or other search predicate as a text (<<me>>).
 */
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class PredicateValue {

    protected String text;
    @JacksonXmlElementWrapper(localName = "stringValues")
    @JacksonXmlProperty(localName = "stringValue")
    protected List<String> stringValues;
    @JacksonXmlElementWrapper(localName = "doubleValues")
    @JacksonXmlProperty(localName = "doubleValue")
    protected List<Double> doubleValues;

    public PredicateValue(Map<String, Object> value) {
        value.forEach((k, v) -> {
            switch (k) {
                case "text":
                    text = (String) v;
                    break;
                case "value":
                    List<?> list = (List<?>) v;
                    if (list.get(0) instanceof String) {
                        stringValues = new ArrayList<>();
                        for (Object val : list) {
                            stringValues.add((String) val);
                        }
                    } else if (list.get(0) instanceof Integer || list.get(0) instanceof Double || list.get(0) instanceof Float) {
                        doubleValues = new ArrayList<>();
                        for (Object val : list) {
                            if (val instanceof Double)
                                doubleValues.add((Double) val);
                            if (val instanceof Integer)
                                doubleValues.add(new Double((Integer) val));
                            if (val instanceof Float)
                                doubleValues.add(new Double((Float) val));
                        }
                    }
                    break;
                case "stringValues":
                    stringValues = new ArrayList<>();
                    List<?> stringList = (List<?>) v;
                    for (Object val : stringList) {
                        stringValues.add((String) val);
                    }
                    break;
                case "doubleValues":
                    doubleValues = new ArrayList<>();
                    List<?> doubleList = (List<?>) v;
                    for (Object val : doubleList) {
                        if (val instanceof Double)
                            doubleValues.add((Double) val);
                        if (val instanceof String)
                            doubleValues.add(Double.parseDouble((String) val));
                    }
                    break;
            }
        });
    }

    @JsonIgnore
    public Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("text", text);
        if (doubleValues != null) {
            mapObject.put("value", doubleValues);
        } else if (stringValues != null) {
            mapObject.put("value", stringValues);
        }
        return mapObject;
    }
}
