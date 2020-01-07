package com.netgrif.workflow.elastic.domain.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DateField extends DataField {

    private long timestamp;

    public DateField(String value, long timestamp) {
        super(value);
        this.timestamp = timestamp;
    }
}