package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FieldWithAllowedNetsField extends DataField {

    public String[] allowedNets;

    public FieldWithAllowedNetsField(FieldWithAllowedNetsField field) {
        super(field);
        this.allowedNets = field.allowedNets == null ? null : Arrays.copyOf(field.allowedNets, field.allowedNets.length);
    }

    public FieldWithAllowedNetsField(String fullTextValue, String[] allowedNets) {
        super(fullTextValue);
        this.allowedNets = allowedNets;
    }

    public FieldWithAllowedNetsField(String[] fullTextValue, String[] allowedNets) {
        super(fullTextValue);
        this.allowedNets = allowedNets;
    }
}
