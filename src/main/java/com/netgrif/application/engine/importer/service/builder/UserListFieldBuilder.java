package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.UserListField;
import com.netgrif.application.engine.workflow.domain.dataset.UserListFieldValue;
import org.springframework.stereotype.Component;

@Component
public class UserListFieldBuilder extends FieldWithAllowedRolesBuilder<UserListField, UserListFieldValue> {

    @Override
    public UserListField build(Data data, Importer importer) {
        UserListField field = new UserListField();
        initialize(field);
        setRoles(field, data);
        setDefaultValue(field, data);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.USER_LIST;
    }
}
