package com.netgrif.workflow.filters

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "filterList")
class FilterImportExportList {

    @JacksonXmlElementWrapper(localName = "filters")
    @JacksonXmlProperty(localName = "filter")
    List<FilterImportExport> filters

    FilterImportExportList() {
        this.filters = new ArrayList<>()
    }
}