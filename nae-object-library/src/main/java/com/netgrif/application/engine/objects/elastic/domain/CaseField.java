package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class CaseField extends FieldWithAllowedNetsField {

    protected List<String> caseValue;

    public CaseField(CaseField field) {
        super(field);
        this.caseValue = field.caseValue == null ? null : new ArrayList<>(field.caseValue);
    }

    public CaseField(List<String> caseValue, List<String> allowedNets) {
        super(caseValue, allowedNets);
        this.caseValue = caseValue;
    }

    @Override
    public Object getValue() {
        return new ArrayList<>(List.of(fulltextValue));
    }
}