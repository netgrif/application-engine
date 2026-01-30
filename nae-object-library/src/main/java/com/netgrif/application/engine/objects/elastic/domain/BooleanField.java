package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class BooleanField extends DataField {

    protected Boolean booleanValue;

    public BooleanField(BooleanField field) {
        super(field);
        this.booleanValue = field.booleanValue;
    }

    public BooleanField(Boolean value) {
        super(value.toString());
        this.booleanValue = value;
    }

    @Override
    public Object getValue() {
        return this.booleanValue;
    }
}
