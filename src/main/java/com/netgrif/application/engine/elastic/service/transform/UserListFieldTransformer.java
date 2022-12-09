package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.UserField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserListFieldTransformer extends ElasticDataFieldTransformer<UserListField, UserField> {

    public UserListFieldTransformer() {
    }

    @Override
    public UserField transform(UserListField caseField, UserListField petriNetField) {
        UserListFieldValue value = caseField.getValue().getValue();
        if (value == null || value.getUserValues() == null || value.getUserValues().isEmpty()) {
            return null;
        }
        List<UserField.UserMappingData> userData = value.getUserValues().stream()
                .map(user -> new UserField.UserMappingData(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName()
                ))
                .collect(Collectors.toList());
        return new UserField(userData);
    }

    @Override
    public DataType getType() {
        return DataType.USER_LIST;
    }
}
