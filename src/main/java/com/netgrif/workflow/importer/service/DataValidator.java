package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
public class DataValidator implements IDataValidator {

    @Override
    public void checkDeprecatedAttributes(Data data) {
        validateAttribute(data.getView(), "view");
        validateAttribute(data.getValid(), "valid");
        validateAttribute(data.getFormat(), "format");
        validateAttribute(data.getValues(), "values");
    }
}
