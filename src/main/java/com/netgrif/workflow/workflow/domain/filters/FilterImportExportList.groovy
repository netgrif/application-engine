package com.netgrif.workflow.workflow.domain.filters

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import lombok.AllArgsConstructor
import lombok.Data

@Data
@AllArgsConstructor
@JacksonXmlRootElement(localName = "filterList")
class FilterImportExportList {

    List<FilterImportExport> filters

    FilterImportExportList() {
        this.filters = new ArrayList<>()
    }
}