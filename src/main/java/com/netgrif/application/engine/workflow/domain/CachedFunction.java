package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.petrinet.domain.Function;
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
        Closure code = (Closure) shell.evaluate(function.getDefinition());
        if (code == null) {
            throw new IllegalArgumentException("Non compilable function");
        }
        return CachedFunction.builder()
                .code(code)
                .function(function)
                .build();
    }
}
