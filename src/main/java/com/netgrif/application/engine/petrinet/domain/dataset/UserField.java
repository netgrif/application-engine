package com.netgrif.application.engine.petrinet.domain.dataset;


import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserField extends Field<UserFieldValue> {

    private Set<String> roles;

    public UserField() {
        this(new HashSet<>());
    }

    public UserField(Set<String> values) {
        super();
        if (values != null) {
            this.roles = values;
        }
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.USER;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public UserField clone() {
        UserField clone = new UserField();
        super.clone(clone);
        clone.roles = this.roles;
        return clone;
    }
}