package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.netgrif.workflow.petrinet.domain.I18nString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "filter")
public class FilterImportExport {

    protected String title;
    protected I18nString filterName;
    protected String filterValue;
    protected Map<String, Object> filterMetadata;
    protected String visibility;
    protected String type;
    protected String icon;
    protected List<String> allowedNets;
}
