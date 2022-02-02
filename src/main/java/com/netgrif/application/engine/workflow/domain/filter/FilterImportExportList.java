package com.netgrif.application.engine.workflow.domain.filter;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterImportExportList that = (FilterImportExportList) o;

        return Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        return filters != null ? filters.hashCode() : 0;
    }
}