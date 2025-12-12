package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class TextField extends DataField {

    protected List<String> textValue;

    public TextField(String value) {
        this(value != null ? List.of(value) : List.of());
    }

    public TextField(List<String> values) {
        super(values);
        this.textValue = values;
    }
}
