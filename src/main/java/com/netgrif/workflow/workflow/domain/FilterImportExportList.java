package com.netgrif.workflow.workflow.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@JacksonXmlRootElement(localName = "filterList")
public class FilterImportExportList {

    @JsonProperty("filter")
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<FilterImportExport> filters;

    public FilterImportExportList() {
        this.filters = new ArrayList<>();
    }
}
