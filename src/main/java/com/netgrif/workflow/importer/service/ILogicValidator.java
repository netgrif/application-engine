package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Logic;

public interface ILogicValidator extends IModelValidator {
    void checkDeprecatedAttributes(Logic logic);
}
