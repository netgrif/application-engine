package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Data;

public interface IDataValidator extends IModelValidator {
    void checkDeprecatedAttributes(Data data);
}
