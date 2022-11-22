package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.Option;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.UserField;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserFieldBuilder extends FieldBuilder<UserField> {

    @Override
    public UserField build(Data data, Importer importer) {
        UserField field = new UserField();
        if (data.getOptions() != null && data.getOptions().getOption() != null) {
            Set<String> roles = data.getOptions().getOption().stream()
                    .map(Option::getKey)
                    .collect(Collectors.toSet());
            field.setRoles(roles);
        } else if (data.getValues() != null) {
            Set<String> roles = data.getValues().stream()
                    .map(value -> importer.getRoles().get(value.getValue()).getStringId())
                    .collect(Collectors.toSet());
            field.setRoles(roles);
        }
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.USER;
    }
}
