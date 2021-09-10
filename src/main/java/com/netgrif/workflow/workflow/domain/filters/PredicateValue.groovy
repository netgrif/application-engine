package com.netgrif.workflow.workflow.domain.filters

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class PredicateValue {

    String text
    List<String> stringValues
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
            }
        })
    }
}
