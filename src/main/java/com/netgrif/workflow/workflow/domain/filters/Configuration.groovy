package com.netgrif.workflow.workflow.domain.filters

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class Configuration {
    String operator
    String datafield

    Configuration(Map<String, Object> value) {
        value.forEach({ k, v ->
            switch(k) {
                case "operator":
                    operator = (String) v
                    break
                case "datafield":
                    datafield = (String) v
                    break
            }
        })
    }
}
