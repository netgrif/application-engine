package com.netgrif.application.engine.objects.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public abstract class DataField implements Serializable {

    @Serial
    private static final long serialVersionUID = 2035013102812591274L;

    public String[] fulltextValue;

    DataField(DataField dataField) {
        this.fulltextValue = dataField.fulltextValue == null ? null : Arrays.copyOf(dataField.fulltextValue, dataField.fulltextValue.length);
    }

    DataField(String fulltextValue) {
        this.fulltextValue = new String[1];
        this.fulltextValue[0] = fulltextValue;
    }

    public Object getValue() {
        return (fulltextValue != null && fulltextValue.length > 0) ? fulltextValue[0] : null;
    }
}
