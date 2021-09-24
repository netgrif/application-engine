package com.netgrif.workflow.filters

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class PredicateArray {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "predicate")
    List<Predicate> predicates

    PredicateArray(List<Object> value) {
        predicates = new ArrayList<>()
        for (def val : value) {
            predicates.add(new Predicate(val as Map<String, Object>))
        }
    }

    PredicateArray(Map<String, Object> value) {
        predicates = new ArrayList<>()
        value.forEach({k, v ->
            for (def val : v) {
                predicates.add(new Predicate(val as Map<String, Object>))
            }
        })
    }

    @JsonIgnore
    List<Object> getMapObject() {
        List<Object> mapObject = new ArrayList<>()
        for (def val : predicates) {
            mapObject.add(val.getMapObject())
        }
        return mapObject
    }
}
