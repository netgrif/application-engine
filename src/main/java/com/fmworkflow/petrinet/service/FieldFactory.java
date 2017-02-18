package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.domain.dataset.FieldType;
import com.fmworkflow.petrinet.domain.dataset.TextField;

public final class FieldFactory {
    public static Field getField(FieldType type) throws IllegalArgumentException {
        switch (type) {
            case TEXT:
                return new TextField();
            default:
                throw new IllegalArgumentException(type + " is not a valid Field type");
        }
    }

    public static Field getField(String typeString) throws IllegalArgumentException {
        return getField(FieldType.fromString(typeString));
    }
}
