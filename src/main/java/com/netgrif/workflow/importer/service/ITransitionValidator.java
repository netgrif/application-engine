package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Transition;

public interface ITransitionValidator extends IModelValidator {

    void checkDeprecatedAttributes(Transition transition);

    void checkBeatingAttributes(Transition transition, Object attr1, Object attr2, String attr1Name, String attr2Name);
}
