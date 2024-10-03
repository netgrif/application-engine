package com.netgrif.application.engine.petrinet.domain.dataset;


import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserField extends FieldWithAllowedRoles<UserFieldValue> {

    public UserField() {
        this(new HashSet<>());
    }

    public UserField(Set<String> values) {
        super(values);
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.USER;
    }

    @Override
    public UserField clone() {
        UserField clone = new UserField();
        super.clone(clone);
        return clone;
    }
}