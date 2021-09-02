package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.netgrif.workflow.petrinet.domain.I18nString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@JacksonXmlRootElement(localName = "filter")
public class FilterImportExport {
    protected String title;
    protected I18nString filterName;
    protected String filterValue;
//    protected Map<String, Object> filterMetadata;
    protected String visibility;
    protected String type;
    protected String icon;
    @JacksonXmlProperty(localName = "allowedNets")
    protected List<String> allowedNets;
    protected FilterMetadataExport filterMetadataExport;

    public void setFilterMetadataExport(Map<String, Object> value) {
        this.filterMetadataExport = new FilterMetadataExport(value);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
//@JacksonXmlRootElement(localName = "filterMetadata")
class FilterMetadataExport {
    protected String filterType;
    protected boolean defaultSearchCategories;
    protected boolean inheritAllowedNets;
    @JacksonXmlProperty(localName = "searchCategories")
//    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<String> searchCategories;
    @JacksonXmlProperty(localName = "predicateMetadataList")
//    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<PredicateArray> predicateMetadata;

    FilterMetadataExport(Map<String, Object> value) {
        value.forEach((k, v) -> {
            switch (k) {
                case "filterType":
                    filterType = (String) v;
                    break;
                case "defaultSearchCategories":
                    defaultSearchCategories = (boolean) v;
                    break;
                case "inheritAllowedNets":
                    inheritAllowedNets = (boolean) v;
                    break;
                case "searchCategories":
                    searchCategories = (List<String>) v;
                    break;
                case "predicateMetadata":
                    predicateMetadata = new ArrayList<>();
                    predicateMetadata.addAll(((List<PredicateArray>) v));
                    break;
            }
        });
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
//@JacksonXmlRootElement(localName = "predicateMetadata")
class PredicateArray {
    @JacksonXmlProperty(localName = "predicateList")
    protected List<Predicate> predicates;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
//@JacksonXmlRootElement(localName = "predicate")
class Predicate {
    protected String category;
    protected Configuration configuration;
    @JacksonXmlProperty(localName = "predicateValueList")
    protected List<PredicateValue> values;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
//@JacksonXmlRootElement(localName = "configuration")
class Configuration {
    protected String operator;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
//@JacksonXmlRootElement(localName = "predicateValue")
class PredicateValue {
    protected String text;
    @JacksonXmlProperty(localName = "predicateValueItemList")
    protected List<PredicateValueItem> value;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
//@JacksonXmlRootElement(localName = "predicateValueItem")
class PredicateValueItem {
    protected String val;
}