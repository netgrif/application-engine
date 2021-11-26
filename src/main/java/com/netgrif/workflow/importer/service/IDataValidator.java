package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Data;

public interface IDataValidator extends IModelValidator {
    void checkDeprecatedAttributes(Data data);
}
