package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.petrinet.domain.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class FunctionFactory {

    public Function getFunction(com.netgrif.workflow.importer.model.Function function) {
        Function function1 = new Function();

        function1.setDefinition(function.getValue());
        function1.setName(function.getName());

        return function1;
    }
}
