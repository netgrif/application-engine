package com.netgrif.workflow.mail.throwables;

public class NoEmailTypeDefinedException extends RuntimeException {

    public NoEmailTypeDefinedException(String errorMessage){
        super(errorMessage);
    }
}
