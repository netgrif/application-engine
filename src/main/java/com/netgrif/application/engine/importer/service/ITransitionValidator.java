package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Transition;
import com.netgrif.application.engine.importer.service.throwable.BeatingAttributesException;

public interface ITransitionValidator extends IModelValidator {

    void checkDeprecatedAttributes(Transition transition);

    /**
     * Self-beating attributes are two attributes, that should not be present inside their parent at the same time.
     * E.g.: if user defines <userRef> in transition, the tag <usersRef> will be disabled, however, additional
     * <userRef> tags can be still added.
     *
     * @param transition the model of Transition
     * @param attr1      first element to be compared
     * @param attr2      second element to be compared
     * @param attr1Name  the name of first element
     * @param attr2Name  the name of second element
     * @throws BeatingAttributesException is thrown when there are two attributes with different type but same goal
     */
    void checkConflictingAttributes(Transition transition, Object attr1, Object attr2, String attr1Name, String attr2Name) throws BeatingAttributesException;
}
