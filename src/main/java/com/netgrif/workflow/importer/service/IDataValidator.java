package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Data;

public interface IDataValidator {
    void checkDeprecatedAttributes(Data data);
}
