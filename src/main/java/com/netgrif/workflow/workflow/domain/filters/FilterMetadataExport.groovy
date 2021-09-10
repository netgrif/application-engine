package com.netgrif.workflow.workflow.domain.filters

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class FilterMetadataExport {
    String filterType
    boolean defaultSearchCategories
    boolean inheritAllowedNets
    List<String> searchCategories
    List<PredicateArray> predicateMetadata

    FilterMetadataExport(Map<String, Object> value) {
        value.forEach({ k, v ->
            switch (k) {
                case "filterType":
                    filterType = (String) v
                    break
                case "defaultSearchCategories":
                    defaultSearchCategories = String.valueOf(v) == "true"
                    break
                case "inheritAllowedNets":
                    inheritAllowedNets = String.valueOf(v) == "true"
                    break
                case "searchCategories":
                    searchCategories = (List<String>) v
                    break
                case "predicateMetadata":
                    predicateMetadata = new ArrayList<>()
                    for (def val : v) {
                        predicateMetadata.add(new PredicateArray(val as List<Object>))
                    }
                    break;
            }
        });
    }
}
