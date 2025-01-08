package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserListField extends FieldWithAllowedRoles<UserListFieldValue> {

    public UserListField() {
        super();
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
