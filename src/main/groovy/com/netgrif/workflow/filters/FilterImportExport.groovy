package com.netgrif.workflow.filters

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.netgrif.workflow.petrinet.domain.I18nString

/**
 * Class that represents one exported filter.
 * This class holds all information about one filter, so filter can be fully build from this class.
 * This class is represented by <filter> tag in exported xml file.
 */

class FilterImportExport {
    I18nString filterName
    String filterValue
    String visibility
    String type
    String icon
    @JacksonXmlElementWrapper(localName = "allowedNets")
    @JacksonXmlProperty(localName = "allowedNet")
    List<String> allowedNets
    @JacksonXmlProperty(localName = "filterMetadata")
    FilterMetadataExport filterMetadataExport

    void setFilterMetadataExport(Map<String, Object> value) {
        this.filterMetadataExport = new FilterMetadataExport(value)
    }
}
