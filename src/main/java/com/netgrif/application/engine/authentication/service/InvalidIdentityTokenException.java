package com.netgrif.application.engine.authentication.service;

public class InvalidIdentityTokenException extends Exception {

    public InvalidIdentityTokenException(String token) {
        super(String.format("Token %s has invalid format", token));
    }
}
