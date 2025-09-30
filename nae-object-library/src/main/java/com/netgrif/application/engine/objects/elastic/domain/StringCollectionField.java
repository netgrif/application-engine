package com.netgrif.application.engine.objects.elastic.domain;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class StringCollectionField extends TextField {

    public String[] collectionValue;

    public StringCollectionField(String[] values) {
        super(values);
        this.collectionValue = values;
    }
}
