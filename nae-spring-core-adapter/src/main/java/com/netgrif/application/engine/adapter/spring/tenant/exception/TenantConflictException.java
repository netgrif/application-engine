package com.netgrif.application.engine.adapter.spring.tenant.exception;

public class TenantConflictException extends RuntimeException {
    public TenantConflictException(String message) {
        super(message);
    }
}
