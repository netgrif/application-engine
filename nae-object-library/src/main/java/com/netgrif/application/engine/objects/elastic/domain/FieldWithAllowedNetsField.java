package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FieldWithAllowedNetsField extends DataField {

    protected List<String> allowedNets;

    public FieldWithAllowedNetsField(FieldWithAllowedNetsField field) {
        super(field);
        this.allowedNets = field.allowedNets == null ? null : new ArrayList<>(field.allowedNets);
    }

    public FieldWithAllowedNetsField(String fullTextValue, List<String> allowedNets) {
        super(fullTextValue);
        this.allowedNets = allowedNets;
    }

    public FieldWithAllowedNetsField(List<String> fullTextValue, List<String> allowedNets) {
        super(fullTextValue);
        this.allowedNets = allowedNets;
    }
}
