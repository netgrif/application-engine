package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import org.springframework.stereotype.Component;

@Component
public class UserListFieldBuilder extends FieldBuilder<UserListField> {
    @Override
    public UserListField build(Data data, Importer importer) {
        UserListField field = new UserListField();
        setDefaultValues(field, data, inits -> {
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.USER_LIST;
    }
}
