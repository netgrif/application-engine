package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FilterField extends FieldWithAllowedNetsField {

    public Map<String, Object> filterMetadata;

    public FilterField(FilterField field) {
        super(field);
        this.filterMetadata = field.filterMetadata == null ? null : new HashMap<>(field.filterMetadata);
    }

    public FilterField(String fullTextValue, String[] allowedNets, Map<String, Object> filterMetadata) {
        super(fullTextValue, allowedNets);
        this.filterMetadata = filterMetadata != null ? filterMetadata : new HashMap<>();
    }

    @Override
    public Object getValue() {
        return super.getValue();
    }
}

