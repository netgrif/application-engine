package com.netgrif.workflow.filters

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * Class that wraps and holds list of filters, which are meant to be exported/imported.
 * This is the root class of xml file, that is created after exporting of filters.
 * The root tag of the xml file is: <filters>
 * <filters> tag is followed by list of <filter> tags.
 * Whole schema for the xml file is on the path: resources/petriNets/filter_export_schema.xsd
 */

@JacksonXmlRootElement(localName = "filters")
class FilterImportExportList {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "filter")
    List<FilterImportExport> filters

    FilterImportExportList() {
        this.filters = new ArrayList<>()
    }
}