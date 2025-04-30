package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class UserField extends Field<UserFieldValue> {
    private Set<String> roles;

    public UserField() {
        super();
        this.roles = new HashSet<>();
    }

    public UserField(String[] values) {
        this();
        if (values != null) {
            this.roles.addAll(Arrays.asList(values));
        }
    }

    @Override
    public FieldType getType() {
        return FieldType.USER;
    }

    @Override
    public Field clone() {
        UserField clone = new UserField();
        super.clone(clone);
        clone.setRoles(this.roles);
        return clone;
    }
}
