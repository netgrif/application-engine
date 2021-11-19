package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Logic;

public interface ILogicValidator extends IModelValidator {
    void checkDeprecatedAttributes(Logic logic);

    void checkBeatingAttributes(Logic logic, Object attr1, Object attr2, String attr1Name, String attr2Name);
}
