package com.netgrif.application.engine.importer.service.throwable;

public class MissingIconKeyException extends RuntimeException {

    public MissingIconKeyException(String fieldId) {
        super("Icon key can not be null and must exists in choices of field. Field: " + fieldId);
    }
}
