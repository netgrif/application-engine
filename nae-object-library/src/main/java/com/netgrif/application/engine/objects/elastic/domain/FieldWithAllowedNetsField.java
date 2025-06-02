package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FieldWithAllowedNetsField extends DataField {

    public String[] allowedNets;

    public FieldWithAllowedNetsField(String fullTextValue, String[] allowedNets) {
        super(fullTextValue);
        this.allowedNets = allowedNets;
    }

    public FieldWithAllowedNetsField(String[] fullTextValue, String[] allowedNets) {
        super(fullTextValue);
        this.allowedNets = allowedNets;
    }
}
