package com.netgrif.application.engine.objects.common;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final ResourceNotFoundExceptionCode key;

    public ResourceNotFoundException(ResourceNotFoundExceptionCode key, String message) {
        super(message);
        this.key = key;
    }
}
