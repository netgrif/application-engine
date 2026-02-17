package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public abstract class DataField implements Serializable {

    @Serial
    private static final long serialVersionUID = 2035013102812591274L;

    protected List<String> fulltextValue = new ArrayList<>();

    DataField(DataField dataField) {
        this(dataField.fulltextValue == null ? new ArrayList<>() : new ArrayList<>(dataField.fulltextValue));
    }

    DataField(String fulltextValue) {
        this(fulltextValue == null ? new ArrayList<>() : List.of(fulltextValue));
    }

    DataField(List<String> fulltextValue) {
        if (fulltextValue != null) {
            this.fulltextValue.addAll(fulltextValue);
        }
    }

    public Object getValue() {
        return (!fulltextValue.isEmpty()) ? fulltextValue.getFirst() : null;
    }
}
