package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Argument implements Serializable {

    private static final long serialVersionUID = -8701225585091953864L;

    protected String value;
    protected Boolean isDynamic;

    @Override
    public Argument clone() {
        Argument cloned =  new Argument();
        cloned.setValue(value);
        cloned.setIsDynamic(isDynamic);
        return cloned;
    }
}
