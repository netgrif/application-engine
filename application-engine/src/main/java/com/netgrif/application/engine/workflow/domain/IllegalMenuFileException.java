package com.netgrif.application.engine.workflow.domain;

/**
 * Exception class that throws error after incorrect menu xml file import.\
 */

public class IllegalMenuFileException extends Exception {
    public IllegalMenuFileException(Exception e) {
        super("Incorrect format of imported menu file!", e);
    }
}