package com.netgrif.workflow.workflow.domain.filters

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class PredicateArray {
    List<Predicate> predicates

    PredicateArray(List<Object> value) {
        predicates = new ArrayList<>()
        for (def val : value) {
            predicates.add(new Predicate(val as Map<String, Object>))
        }
    }
}
