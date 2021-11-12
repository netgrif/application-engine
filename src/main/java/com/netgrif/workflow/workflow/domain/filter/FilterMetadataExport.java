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
 * This class represents complex structure of filter field metadata object.
 * This class is represented by <filterMetadata> tag in xml document.
 * While exporting, class is created from complex structure (map) which consists of:
 * keys as strings
 * values as objects
 * This structure needs to be recreated when importing filter by method getMapObject().
 */
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class FilterMetadataExport {
    protected String filterType;
    protected boolean defaultSearchCategories;
    protected boolean inheritAllowedNets;
    @JacksonXmlElementWrapper(localName = "searchCategories")
    @JacksonXmlProperty(localName = "searchCategory")
    protected List<String> searchCategories;
    @JacksonXmlElementWrapper(localName = "predicateMetadata")
    @JacksonXmlProperty(localName = "predicateMetadataItem")
    protected List<PredicateArray> predicateMetadata;

    public FilterMetadataExport(Map<String, Object> value) {
        value.forEach((k, v) -> {
            switch (k) {
                case "filterType":
                    filterType = (String) v;
                    break;
                case "defaultSearchCategories":
                    defaultSearchCategories = String.valueOf(v).equals("true");
                    break;
                case "inheritAllowedNets":
                    inheritAllowedNets = String.valueOf(v).equals("true");
                    break;
                case "searchCategories":
                    searchCategories = (List<String>) v;
                    break;
                case "predicateMetadata":
                    predicateMetadata = new ArrayList<>();
                    List<?> list = (List<?>) v;
                    for (Object val : list) {
                        if (val instanceof List) {
                            predicateMetadata.add(new PredicateArray((List<Object>) val));
                        } else {
                            predicateMetadata.add(new PredicateArray((Map<String, Object>) val));
                        }
                    }
                    break;
            }
        });
    }

    @JsonIgnore
    public Map<String, Object> getMapObject() {
        Map<String, Object> mapObject = new HashMap<>();
        List<Object> listPredicateMetadata = new ArrayList<>();
        for (PredicateArray val : predicateMetadata) {
            listPredicateMetadata.add(val.getMapObject());
        }
        mapObject.put("predicateMetadata", listPredicateMetadata);
        mapObject.put("searchCategories", searchCategories != null ? searchCategories : new ArrayList<String>());
        return mapObject;
    }
}
