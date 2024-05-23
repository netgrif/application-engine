package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
public class UserListFieldBuilder extends FieldBuilder<UserListField> {

    @Override
    public UserListField build(Data data, Importer importer) {
        UserListField field = new UserListField();
        initialize(field);
        setDefaultValues(field, data, inits -> {
//            TODO: release/8.0.0
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.USER_LIST;
    }
}
