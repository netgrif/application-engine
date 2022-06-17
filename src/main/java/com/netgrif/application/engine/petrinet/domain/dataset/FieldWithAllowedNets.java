package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public abstract class FieldWithAllowedNets<T> extends Field<T> {

    private List<String> allowedNets;

    FieldWithAllowedNets() {
        super();
        allowedNets = new ArrayList<>();
    }

    FieldWithAllowedNets(List<String> allowedNets) {
        this();
        this.setAllowedNets(allowedNets);
    }

    void clone(FieldWithAllowedNets<T> clone) {
        super.clone(clone);
        clone.allowedNets = new ArrayList<>(this.allowedNets);
    }

    List<String> getAllowedNets() {
        return allowedNets;
    }

    void setAllowedNets(Collection<String> allowedNets) {
        if (allowedNets == this.allowedNets) {
            return;
        }
        this.allowedNets.clear();
        this.allowedNets.addAll(allowedNets);
    }
}