package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FilterField extends FieldWithAllowedNets<String> {

    /**
     * Serialized information necessary for the restoration of the advanced search frontend GUI.
     * <p>
     * Backend shouldn't need to interact with this attribute
     */
    private Map<String, Object> filterMetadata;

    public FilterField() {
        super();
        filterMetadata = new HashMap<>();
    }

    public FilterField(List<String> allowedNets) {
        super(allowedNets);
        filterMetadata = new HashMap<>();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.FILTER;
    }

    @Override
    public FilterField clone() {
        FilterField clone = new FilterField();
        super.clone(clone);
        clone.filterMetadata = this.filterMetadata;
        return clone;
    }
}
