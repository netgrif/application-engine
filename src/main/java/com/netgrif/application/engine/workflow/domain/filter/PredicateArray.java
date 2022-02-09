package com.netgrif.application.engine.workflow.domain.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.util.*;

/**
 * This class wraps and holds list of predicates.
 * In the xml structure class is represented by <predicateMetadataItem> tag.
 * Same as the FilterMetadataExport class, this one needs to be converted into
 * map object while importing filter too.
 */
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class PredicateArray {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "predicate")
    protected List<Predicate> predicates;

    public PredicateArray(List<Object> value) {
        predicates = new ArrayList<>();
        for (Object val : value) {
            predicates.add(new Predicate((Map<String, Object>) val));
        }
    }

    public PredicateArray(Map<String, Object> value) {
        predicates = new ArrayList<>();
        value.forEach((k, v) -> {
            for (Object val : ((Collection<?>) v)) {
                predicates.add(new Predicate((Map<String, Object>) val));
            }
        });
    }

    @JsonIgnore
    public List<Object> getMapObject() {
        List<Object> mapObject = new ArrayList<>();
        for (Predicate val : predicates) {
            mapObject.add(val.getMapObject());
        }
        return mapObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PredicateArray that = (PredicateArray) o;

        return predicates.size() == that.predicates.size(); // TODO implements better comparison of two PredicateArrays
    }

    @Override
    public int hashCode() {
        return predicates != null ? predicates.hashCode() : 0;
    }
}
