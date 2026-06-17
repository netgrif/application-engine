package com.netgrif.application.engine.auth.service;

public class InvalidUserTokenException extends Exception {

    public InvalidUserTokenException(String token) {
        super("Token " + token + " has invalid format");
    }
}
