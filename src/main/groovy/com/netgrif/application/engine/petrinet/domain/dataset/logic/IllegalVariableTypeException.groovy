package com.netgrif.application.engine.petrinet.domain.dataset.logic


class IllegalVariableTypeException extends RuntimeException {
    IllegalVariableTypeException(String var1) {
        super("Variable type \'" + var1 + "\' does not exist")
    }
}
