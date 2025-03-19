package com.netgrif.application.engine.event.evaluators;

import lombok.Getter;

import java.util.EventObject;
import java.util.function.Function;

public class Evaluator<E extends EventObject, R> {
    @Getter
    private final String type;
    private final Function<E, R> evaluationFunction;

    public Evaluator(String type, Function<E, R> evaluationFunction) {
        this.type = type;
        this.evaluationFunction = evaluationFunction;
    }

    public R apply(E applicationEvent) {
        return evaluationFunction.apply(applicationEvent);
    }
}
