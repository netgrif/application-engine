package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserListField extends FieldWithAllowedRoles<UserListFieldValue> {

    private String roleId;

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
        clone.setRoleId(roleId);
        super.clone(clone);
        return clone;
    }
}
