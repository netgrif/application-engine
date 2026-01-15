package com.netgrif.application.engine.objects.elastic.domain;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class StringCollectionField extends TextField {

    public String[] collectionValue;

    public StringCollectionField(StringCollectionField field) {
        super(field);
        this.collectionValue = field.collectionValue == null ? null : Arrays.copyOf(field.collectionValue, field.collectionValue.length);
    }

    public StringCollectionField(String[] values) {
        super(values);
        this.collectionValue = values;
    }
}
