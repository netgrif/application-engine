package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public abstract class FieldWithAllowedNets<T> extends Field<T> {

    private List<String> allowedNets;

    public FieldWithAllowedNets() {
        super();
    }

    public FieldWithAllowedNets(List<String> allowedNets) {
        this();
        this.setAllowedNets(allowedNets);
    }

    public void clone(FieldWithAllowedNets<T> clone) {
        super.clone(clone);
        if (allowedNets != null) {
            clone.allowedNets = new ArrayList<>(this.allowedNets);
        }
    }
}