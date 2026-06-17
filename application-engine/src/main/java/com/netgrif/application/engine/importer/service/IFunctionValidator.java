package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.objects.importer.model.Function;

public interface IFunctionValidator extends IModelValidator {
    /**
     * Validates and updates deprecated function attributes.
     * <p>
     * This method mutates the input function by replacing deprecated scope values.
     * Specifically, it replaces the deprecated NAMESPACE scope with GLOBAL scope.
     *
     * @param function the function to validate and update (mutated in-place)
     */
    void checkDeprecatedAttributes(Function function);
}
