package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.UserField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import org.springframework.stereotype.Component;

@Component
public class UserFieldBuilder extends FieldWithAllowedRolesBuilder<UserField, UserFieldValue> {

    @Override
    public UserField build(Data data, Importer importer) {
        UserField field = new UserField();
        initialize(field);
        setRoles(field, data);
        setDefaultValue(field, data);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.USER;
    }
}
