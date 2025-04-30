package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class UserListField extends Field<UserListFieldValue> {

    private Set<String> roles;

    public UserListField() {
        super();
        this.roles = new HashSet<>();
    }

    public UserListField(String[] values) {
        this();
        if (values != null) {
            this.roles.addAll(Arrays.asList(values));
        }
    }

    @Override
    public FieldType getType() {
        return FieldType.USERLIST;
    }

    @Override
    public Field<?> clone() {
        UserListField clone = new UserListField();
        super.clone(clone);
        clone.setRoles(this.roles);
        return clone;
    }
}
