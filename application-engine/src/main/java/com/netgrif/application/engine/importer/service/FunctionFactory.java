package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.objects.importer.model.Scope;
import com.netgrif.application.engine.objects.petrinet.domain.Function;
import com.netgrif.application.engine.objects.petrinet.domain.FunctionScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class FunctionFactory {

    private final IFunctionValidator functionValidator;

    public Function getFunction(com.netgrif.application.engine.objects.importer.model.Function function) {
        functionValidator.checkDeprecatedAttributes(function);
        Function function1 = new Function();

        function1.setDefinition(function.getValue());
        function1.setName(function.getName());
        function1.setScope(FunctionScope.valueOf(function.getScope().name()));

        return function1;
    }

}
