package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class FieldWithAllowedRoles<T> extends Field<T> {

    private Set<String> allowedRoleImportIds;
    private Set<String> caseRoleIds;

    public FieldWithAllowedRoles() {
        super();
        this.caseRoleIds = new HashSet<>();
    }

    public FieldWithAllowedRoles(Set<String> allowedRoleImportIds) {
        this();
        this.setAllowedRoleImportIds(allowedRoleImportIds);
    }

    public void clone(FieldWithAllowedRoles<T> clone) {
        super.clone(clone);
        if (allowedRoleImportIds != null) {
            clone.allowedRoleImportIds = new HashSet<>(allowedRoleImportIds);
        }
        clone.setCaseRoleIds(new HashSet<>(caseRoleIds));
    }
}
