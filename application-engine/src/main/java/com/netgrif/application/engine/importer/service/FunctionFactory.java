package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.objects.importer.model.Scope;
import com.netgrif.application.engine.objects.petrinet.domain.Function;
import com.netgrif.application.engine.objects.petrinet.domain.FunctionScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class FunctionFactory {

    private static final String NAMESPACE = "namespace";

    public Function getFunction(com.netgrif.application.engine.objects.importer.model.Function function) {
        checkDeprecatedAttributes(function);
        Function function1 = new Function();

        function1.setDefinition(function.getValue());
        function1.setName(function.getName());
        function1.setScope(FunctionScope.valueOf(function.getScope().name()));

        return function1;
    }

    private void checkDeprecatedAttributes(com.netgrif.application.engine.objects.importer.model.Function function) {
        if (function.getScope() != null && function.getScope().name().equals(NAMESPACE)) {
            log.warn("Function scope [NAMESPACE] is deprecated. Replacing with [GLOBAL].]");
            function.setScope(Scope.GLOBAL);
        }
    }
}
