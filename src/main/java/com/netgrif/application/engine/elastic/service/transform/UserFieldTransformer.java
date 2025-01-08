package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.dataset.UserField;
import com.netgrif.application.engine.workflow.domain.dataset.UserFieldValue;
import org.springframework.stereotype.Component;

@Component
public class UserFieldTransformer extends ElasticDataFieldTransformer<UserField, com.netgrif.application.engine.elastic.domain.UserField> {

    @Override
    public com.netgrif.application.engine.elastic.domain.UserField transform(UserField caseField, UserField petriNetField) {
        UserFieldValue value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new com.netgrif.application.engine.elastic.domain.UserField(
                new com.netgrif.application.engine.elastic.domain.UserField.UserMappingData(
                        value.getId(),
                        value.getEmail(),
                        value.getFullName()
                )
        );
    }

    @Override
    public DataType getType() {
        return DataType.USER;
    }
}
