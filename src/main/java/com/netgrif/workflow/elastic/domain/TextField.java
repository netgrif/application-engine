package com.netgrif.workflow.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextField extends DataField {

    public TextField(String value) {
        super(value);
    }
}