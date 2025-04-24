package com.netgrif.application.engine.objects.petrinet.domain.dataset;

public class BooleanField extends Field<Boolean> {
    public BooleanField() {
        super();
        setDefaultValue(false);
    }

    @Override
    public FieldType getType() {
        return FieldType.BOOLEAN;
    }

    public Boolean or(final BooleanField field) {
        return this.getValue() || field.getValue();
    }

    public Boolean and(final BooleanField field) {
        return this.getValue() && field.getValue();
    }

    @Override
    public Field<?> clone() {
        BooleanField clone = new BooleanField();
        super.clone(clone);
        return clone;
    }
}
