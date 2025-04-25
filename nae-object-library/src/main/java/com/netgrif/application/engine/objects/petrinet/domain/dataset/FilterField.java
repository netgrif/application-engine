package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class FilterField extends FieldWithAllowedNets<String> {

    private Map<String, Object> filterMetadata;

    public FilterField() {
        super();
        setAllowedNets(new ArrayList<>());
        filterMetadata = new HashMap<>();
    }

    public FilterField(List<String> allowedNets) {
        super(allowedNets);
        filterMetadata = new HashMap<>();
    }

    @Override
    public FieldType getType() {
        return FieldType.FILTER;
    }

    @Override
    public Field<?> clone() {
        FilterField clone = new FilterField();
        super.clone(clone);
        clone.setFilterMetadata(this.filterMetadata);
        return clone;
    }
}
