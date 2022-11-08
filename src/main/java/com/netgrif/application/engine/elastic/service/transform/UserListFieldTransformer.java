package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.domain.UserField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserListFieldTransformer extends ElasticDataFieldTransformer<UserListField, UserField> {

    private final IUserService userService;

    public UserListFieldTransformer(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public UserField transform(UserListField caseField, UserListField petriNetField) {
        List<String> value = caseField.getValue().getValue();
        if (value == null || value.isEmpty()) {
            return null;
        }
        List<IUser> users = this.userService.findAllByIds(new HashSet<>(value), true);
        List<UserField.UserMappingData> userData = users.stream()
                .map(user -> new UserField.UserMappingData(
                        user.getStringId(),
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
