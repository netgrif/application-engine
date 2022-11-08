package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.UserField;
import org.springframework.stereotype.Component;

@Component
public class UserFieldBuilder extends FieldBuilder<UserField> {

    @Override
    public UserField build(Data data, Importer importer) {
        // TODO: NAE-1645 values or options?
        String[] roles = data.getValues().stream()
                .map(value -> importer.getRoles().get(value.getValue()).getStringId())
                .toArray(String[]::new);
        UserField field = new UserField(roles);
        setDefaultValues(field, data, inits -> {
            field.setDefaultValue(null);
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.USER;
    }
}
