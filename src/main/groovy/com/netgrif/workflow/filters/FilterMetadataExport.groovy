package com.netgrif.workflow.filters

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

class FilterMetadataExport {
    String filterType
    boolean defaultSearchCategories
    boolean inheritAllowedNets
    @JacksonXmlElementWrapper(localName = "searchCategories")
    @JacksonXmlProperty(localName = "searchCategory")
    List<String> searchCategories
    @JacksonXmlElementWrapper(localName = "predicateMetadata")
    @JacksonXmlProperty(localName = "predicateMetadataItem")
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
                        if (val instanceof List) {
                            predicateMetadata.add(new PredicateArray(val as List<Object>))
                        } else {
                            predicateMetadata.add(new PredicateArray(val as Map<String, Object>))
                        }
                    }
                    break
            }
        })
    }

    @JsonIgnore
    Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>()
        List<Object> listPredicateMetadata = new ArrayList<>()
        for (def val : predicateMetadata) {
            listPredicateMetadata.add(val.getMapObject())
        }
        mapObject.put("predicateMetadata", listPredicateMetadata)
        mapObject.put("searchCategories", searchCategories != null ? searchCategories : new ArrayList<String>())
        return mapObject
    }
}
