package com.netgrif.application.engine.workflow.domain.throwable;

public class IllegalArcNodeException extends RuntimeException {

    public IllegalArcNodeException(String message) {
        super(message);
    }

    public static IllegalArcNodeException fromTransition(String arcType) {
        return new IllegalArcNodeException(arcType + " can not lead from a Transition");
    }

    public static IllegalArcNodeException toPlace(String arcType) {
        return new IllegalArcNodeException(arcType + " can not lead to a Place");
    }
}
