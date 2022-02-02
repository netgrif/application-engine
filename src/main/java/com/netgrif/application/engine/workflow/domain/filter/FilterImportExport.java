package com.netgrif.application.engine.workflow.domain.filter;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterImportExport that = (FilterImportExport) o;

        if (!Objects.equals(parentViewId, that.parentViewId)) return false;
        if (!Objects.equals(filterName, that.filterName)) return false;
        if (!Objects.equals(filterValue, that.filterValue)) return false;
        if (!Objects.equals(visibility, that.visibility)) return false;
        if (!Objects.equals(type, that.type)) return false;
        if (!Objects.equals(allowedNets, that.allowedNets)) return false;
        return Objects.equals(filterMetadataExport, that.filterMetadataExport);
    }

    @Override
    public int hashCode() {
        int result = parentViewId != null ? parentViewId.hashCode() : 0;
        result = 31 * result + (filterName != null ? filterName.hashCode() : 0);
        result = 31 * result + (filterValue != null ? filterValue.hashCode() : 0);
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        result = 31 * result + (allowedNets != null ? allowedNets.hashCode() : 0);
        result = 31 * result + (filterMetadataExport != null ? filterMetadataExport.hashCode() : 0);
        return result;
    }
}
