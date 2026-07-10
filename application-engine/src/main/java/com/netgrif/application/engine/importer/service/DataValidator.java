package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.objects.importer.model.Data;
import com.netgrif.application.engine.objects.importer.model.DataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataValidator implements IDataValidator {

    @Override
    public void checkDeprecatedAttributes(Data data) {
        validateAttribute(data.getView(), "view", data.getId());
        validateAttribute(data.getValid() != null && !data.getValid().isEmpty() ? data.getValid() : null, "valid", data.getId());
        validateAttribute(data.getFormat(), "format", data.getId());
        validateAttribute(data.getValues() != null && !data.getValues().isEmpty() ? data.getValues() : null, "values", data.getId());
        validateAttribute(data.getType() == DataType.USER ? Boolean.TRUE : null, "type: " + DataType.USER.name(), data.getId());
        validateAttribute(data.getType() == DataType.USER_LIST ? Boolean.TRUE : null, "type: " + DataType.USER_LIST.name(), data.getId());
    }

    protected void validateAttribute(Object attr, String attrName, String fieldName) {
        if (attr != null) {
            log.warn("Data attribute [{}] on field [{}] is deprecated.", attrName, fieldName);
        }
    }
}
