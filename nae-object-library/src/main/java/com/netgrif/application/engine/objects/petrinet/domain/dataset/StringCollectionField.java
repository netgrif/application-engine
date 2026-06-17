package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import java.util.ArrayList;
import java.util.List;

public class StringCollectionField extends Field<List<String>> {

    public StringCollectionField() {
        super();
        this.setDefaultValue(new ArrayList<String>());
    }

    @Override
    public FieldType getType() {
        return FieldType.STRING_COLLECTION;
    }

    @Override
    public void clearValue() {
        this.setValue(new ArrayList<String>());
    }

    @Override
    public Field<?> clone() {
        StringCollectionField clone = new StringCollectionField();
        super.clone(clone);
        return clone;
    }

    public void setDefaultValue(List<String> defaultValue) {
        super.setDefaultValue(defaultValue);
    }
}
