package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class FieldWithAllowedRoles<T> extends Field<T> {

    private Set<String> roles;

    public FieldWithAllowedRoles() {
        super();
    }

    public FieldWithAllowedRoles(Set<String> roles) {
        this();
        this.setRoles(roles);
    }

    public void clone(FieldWithAllowedRoles<T> clone) {
        super.clone(clone);
        if (roles != null) {
            clone.roles = new HashSet<>(roles);
        }
    }
}
