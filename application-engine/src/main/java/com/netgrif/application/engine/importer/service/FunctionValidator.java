package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.objects.importer.model.Function;
import com.netgrif.application.engine.objects.importer.model.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FunctionValidator implements IFunctionValidator {

    private static final String NAMESPACE = "namespace";

    /**
    * Validates and updates deprecated function attributes.
    * <p>
    * This method mutates the input function by replacing deprecated scope values.
    * Specifically, it replaces the deprecated NAMESPACE scope with GLOBAL scope.
    *
    * @param function the function to validate and update (mutated in-place)
    */
    @Override
    public void checkDeprecatedAttributes(Function function) {
        if (function.getScope() != null && function.getScope().value().equals(NAMESPACE)) {
            log.warn("Function scope [NAMESPACE] is deprecated. Replacing with [GLOBAL].");
            function.setScope(Scope.GLOBAL);
        }
    }
}
