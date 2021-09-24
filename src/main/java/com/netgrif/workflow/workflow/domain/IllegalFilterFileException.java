package com.netgrif.workflow.workflow.domain;

public class IllegalFilterFileException extends Exception {

    public IllegalFilterFileException() {
        super("Incorrect format of imported filters file!");
    }
}
