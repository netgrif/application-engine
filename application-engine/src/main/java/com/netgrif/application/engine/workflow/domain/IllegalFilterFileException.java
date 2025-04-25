package com.netgrif.application.engine.workflow.domain;

/**
 * Exception class that throws error after incorrect filter xml file import.\
 */

public class IllegalFilterFileException extends Exception {

    public IllegalFilterFileException(Exception e) {
        super("Incorrect format of imported filters file!", e);
    }
}
