package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;

import java.util.ArrayList;
import java.util.List;

public class CaseField extends FieldWithAllowedNets<List<String>> {

    public CaseField() {
        this(new ArrayList<>());
    }

    public CaseField(List<String> allowedNets) {
        super(allowedNets);
        super.defaultValue = new ArrayList<>();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.CASE_REF;
    }

    @Override
    public void clearValue() {
        this.setValue(new ArrayList<>());
    }

    @Override
    public CaseField clone() {
        CaseField clone = new CaseField();
        super.clone(clone);
        return clone;
    }
}