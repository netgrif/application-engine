package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FilterField extends FieldWithAllowedNetsField {


    public FilterField(FilterField field) {
        super(field);
    }

    public FilterField(String fullTextValue, List<String> allowedNets, Map<String, Object> filterMetadata) {
        super(fullTextValue, allowedNets);
    }

    @Override
    public Object getValue() {
        return super.getValue();
    }
}

