package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public abstract class FieldWithAllowedNets<T> extends Field<T> {

    private final List<String> allowedNets;

    public FieldWithAllowedNets() {
        super();
        allowedNets = new ArrayList<>();
    }

    public FieldWithAllowedNets(List<String> allowedNets) {
        this();
        this.setAllowedNets(allowedNets);
    }

    @Override
    public void clone(Field<T> clone) {
        super.clone(clone);
        ((FieldWithAllowedNets<?>) clone).setAllowedNets(new ArrayList<>(this.allowedNets));
    }

    public void setAllowedNets(Collection<String> allowedNets) {
        if (allowedNets.equals(this.allowedNets)) {
            return;
        }
        this.allowedNets.clear();
        this.allowedNets.addAll(allowedNets);
    }
}
