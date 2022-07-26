package com.netgrif.application.engine.importer.service.validation;

import com.netgrif.application.engine.importer.model.Data;

public interface IDataValidator extends IModelValidator {
    void checkDeprecatedAttributes(Data data);
}
