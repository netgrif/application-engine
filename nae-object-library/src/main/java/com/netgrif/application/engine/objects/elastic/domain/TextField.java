package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class TextField extends DataField {

    public String[] textValue;

    public TextField(String value) {
        super(value);
        this.textValue = new String[1];
        this.textValue[0] = value;
    }

    public TextField(String[] values) {
        super(values);
        this.textValue = values;
    }
}
