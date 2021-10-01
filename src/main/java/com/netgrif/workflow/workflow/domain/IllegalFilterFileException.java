package com.netgrif.workflow.workflow.domain;

/**
 * Exception class that throws error after incorrect filter xml file import.
 */

public class IllegalFilterFileException extends Exception {

    public IllegalFilterFileException() {
        super("Incorrect format of imported filters file!");
    }
}
