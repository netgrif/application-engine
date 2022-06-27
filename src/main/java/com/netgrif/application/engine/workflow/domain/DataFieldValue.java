package com.netgrif.application.engine.workflow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataFieldValue {

    private Object value;

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
