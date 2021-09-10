package com.netgrif.workflow.workflow.domain.filters

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.netgrif.workflow.petrinet.domain.I18nString
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class FilterImportExport {
    String title
    I18nString filterName
    String filterValue
    String visibility
    String type
    String icon
    List<String> allowedNets
    FilterMetadataExport filterMetadataExport

    void setFilterMetadataExport(Map<String, Object> value) {
        this.filterMetadataExport = new FilterMetadataExport(value)
    }
}
