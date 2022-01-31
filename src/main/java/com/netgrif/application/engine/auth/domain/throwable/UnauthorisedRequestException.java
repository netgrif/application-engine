package com.netgrif.application.engine.auth.domain.throwable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnauthorisedRequestException extends Exception {

    public UnauthorisedRequestException(String message) {
        super(message);
    }
}