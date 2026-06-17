package com.netgrif.application.engine.objects.petrinet.domain.dataset;

public class ButtonField extends Field<Integer> {

    public ButtonField() {
        super();
        setValue(0);
    }

    @Override
    public FieldType getType() {
        return FieldType.BUTTON;
    }

    @Override
    public Field<?> clone() {
        ButtonField clone = new ButtonField();
        super.clone(clone);
        return clone;
    }

}
