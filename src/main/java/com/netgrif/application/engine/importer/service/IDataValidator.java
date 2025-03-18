package com.netgrif.application.engine.importer.service;

import com.netgrif.core.importer.model.Data;

public interface IDataValidator extends IModelValidator {
    void checkDeprecatedAttributes(Data data);
}
