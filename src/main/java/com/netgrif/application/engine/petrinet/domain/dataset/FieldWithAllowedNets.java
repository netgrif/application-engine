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
        allowedNets = new ArrayList<>();
    }

    public FieldWithAllowedNets(List<String> allowedNets) {
        this();
        this.setAllowedNets(allowedNets);
    }

    public void clone(FieldWithAllowedNets<T> clone) {
        super.clone(clone);
        clone.allowedNets = new ArrayList<>(this.allowedNets);
    }

    public List<String> getAllowedNets() {
        return allowedNets;
    }

    public void setAllowedNets(Collection<String> allowedNets) {
        if (allowedNets == this.allowedNets) {
            return;
        }
        this.allowedNets.clear();
        this.allowedNets.addAll(allowedNets);
    }
}