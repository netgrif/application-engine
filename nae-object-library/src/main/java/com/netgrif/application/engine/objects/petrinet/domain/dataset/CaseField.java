package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import java.util.ArrayList;
import java.util.List;

public class CaseField extends FieldWithAllowedNets<List<String>> {

    public CaseField() {
        this(new ArrayList<String>());
    }

    public CaseField(List<String> allowedNets) {
        super(allowedNets);
        super.setDefaultValue(new ArrayList<>());
    }

    @Override
    public FieldType getType() {
        return FieldType.CASE_REF;
    }

    @Override
    public void clearValue() {
        this.setValue(new ArrayList<>());
    }

    @Override
    public Field<?> clone() {
        CaseField clone = new CaseField();
        super.clone(clone);
        return clone;
    }

}
