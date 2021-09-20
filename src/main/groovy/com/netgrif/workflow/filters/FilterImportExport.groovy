package com.netgrif.workflow.filters

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.netgrif.workflow.petrinet.domain.I18nString

class FilterImportExport {
    String title
    I18nString filterName
    String filterValue
    String visibility
    String type
    String icon
    @JacksonXmlElementWrapper(localName = "allowedNets")
    @JacksonXmlProperty(localName = "allowedNet")
    List<String> allowedNets
    FilterMetadataExport filterMetadataExport

    void setFilterMetadataExport(Map<String, Object> value) {
        this.filterMetadataExport = new FilterMetadataExport(value)
    }
}
