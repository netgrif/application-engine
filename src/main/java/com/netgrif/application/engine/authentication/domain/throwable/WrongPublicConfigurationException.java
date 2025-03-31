package com.netgrif.application.engine.authentication.domain.throwable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class WrongPublicConfigurationException extends Exception {

    public WrongPublicConfigurationException(String message) {
        super(message);
    }
}
