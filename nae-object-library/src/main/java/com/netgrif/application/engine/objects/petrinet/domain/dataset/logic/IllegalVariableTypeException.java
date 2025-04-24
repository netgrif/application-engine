package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

public class IllegalVariableTypeException extends RuntimeException {
    public IllegalVariableTypeException(String var1) {
        super("Variable type '" + var1 + "' does not exist");
    }
}
