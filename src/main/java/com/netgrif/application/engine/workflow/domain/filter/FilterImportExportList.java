package com.netgrif.application.engine.workflow.domain.filter;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that wraps and holds list of filters, which are meant to be exported/imported.
 * This is the root class of xml file, that is created after exporting of filters.
 * The root tag of the xml file is: <filters>
 * <filters> tag is followed by list of <filter> tags.
 * Whole schema for the xml file is on the path: resources/petriNets/filter_export_schema.xsd
 */
@EqualsAndHashCode
@Getter
@Setter
@JacksonXmlRootElement(localName = "filters")
public class FilterImportExportList {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "filter")
    protected List<FilterImportExport> filters;

    public FilterImportExportList() {
        this.filters = new ArrayList<>();
    }
}