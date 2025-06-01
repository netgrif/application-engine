package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class CaseField extends FieldWithAllowedNetsField {

    public CaseField(String[] fullTextValue, String[] allowedNets) {
        super(fullTextValue, allowedNets);
    }
}