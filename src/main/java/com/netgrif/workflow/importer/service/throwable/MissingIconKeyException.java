package com.netgrif.workflow.importer.service.throwable;

public class MissingIconKeyException extends Exception {

    public MissingIconKeyException(String fieldId) {
        super("Icon key can not be null and must exists in choices of field. Field: " + fieldId);
    }
}
