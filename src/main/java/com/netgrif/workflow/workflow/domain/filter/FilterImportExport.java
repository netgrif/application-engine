package com.netgrif.workflow.workflow.domain.filter;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.netgrif.workflow.petrinet.domain.I18nString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Class that represents one exported filter.
 * This class holds all information about one filter, so filter can be fully build from this class.
 * This class is represented by <filter> tag in exported xml file.
 */
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class FilterImportExport {
    @EqualsAndHashCode.Exclude
    protected String caseId;
    @EqualsAndHashCode.Exclude
    protected String parentCaseId;
    protected String parentViewId;
    protected I18nString filterName;
    protected String filterValue;
    protected String visibility;
    protected String type;
    protected String icon;
    @JacksonXmlElementWrapper(localName = "allowedNets")
    @JacksonXmlProperty(localName = "allowedNet")
    protected List<String> allowedNets;
    @JacksonXmlProperty(localName = "filterMetadata")
    protected FilterMetadataExport filterMetadataExport;

    public void setFilterMetadataExport(Map<String, Object> value) {
        this.filterMetadataExport = new FilterMetadataExport(value);
    }
}
