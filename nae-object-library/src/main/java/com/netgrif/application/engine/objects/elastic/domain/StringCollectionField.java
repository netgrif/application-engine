package com.netgrif.application.engine.objects.elastic.domain;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class StringCollectionField extends DataField {

    protected List<String> collectionValue;

    public StringCollectionField(StringCollectionField field) {
        super(field);
        this.collectionValue = field.collectionValue == null ? null : new ArrayList<>(field.collectionValue);
    }


    public StringCollectionField(List<String> values) {
        super(values);
        this.collectionValue = values;
    }
}
