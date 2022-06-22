package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;

import java.util.ArrayList;
import java.util.List;

public class UserListField extends Field<List<String>> {

    public UserListField() {
        super();
        this.defaultValue = new ArrayList<>();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.USER_LIST;
    }

    @Override
    public UserListField clone() {
        UserListField clone = new UserListField();
        super.clone(clone);
        return clone;
    }
}
