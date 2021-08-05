package com.netgrif.workflow.workflow.domain;

import com.netgrif.workflow.petrinet.domain.Function;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CachedFunction {

    private final Function function;
    private final Closure code;

    public static CachedFunction build(GroovyShell shell, Function function) {
        return CachedFunction.builder()
                .code((Closure) shell.evaluate(function.getDefinition()))
                .function(function)
                .build();
    }
}
