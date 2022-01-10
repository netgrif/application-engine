package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.petrinet.domain.Function;
import com.netgrif.workflow.petrinet.domain.FunctionScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class FunctionFactory {

    public Function getFunction(com.netgrif.workflow.importer.model.Function function) {
        Function function1 = new Function();

        function1.setDefinition(function.getValue());
        function1.setName(function.getName());
        function1.setScope(FunctionScope.valueOf(function.getScope().name()));

        return function1;
    }
}
