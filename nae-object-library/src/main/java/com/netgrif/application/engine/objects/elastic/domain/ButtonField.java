package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ButtonField extends DataField {

    protected Integer buttonValue;

    public ButtonField(ButtonField field) {
        super(field);
        this.buttonValue = field.buttonValue;
    }

    public ButtonField(Integer value) {
        super(value == null ? null : value.toString());
        this.buttonValue = value;
    }

    public void setButtonValue(Integer value) {
        this.fulltextValue.clear();
        this.fulltextValue.add(value == null ? null : value.toString());
        this.buttonValue = value;
    }

    @Override
    public Object getValue() {
        return this.buttonValue;
    }
}
