package com.netgrif.workflow.workflow.domain.filters

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class Predicate {
    String category
    Configuration configuration
    List<String> stringValues
    List<Integer> integerValues
    List<Boolean> booleanValues
    List<PredicateValue> mapValues
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
                    }else {
                        mapValues = new ArrayList<>()
                        for (def val : v) {
                            mapValues.add(new PredicateValue(val as Map<String, Object>))
                        }
                    }
            }
        })
    }
}
