package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.dataset.*;

public final class FieldFactory {
    public static Field getField(FieldType type, String[] values) throws IllegalArgumentException {
        switch (type) {
            case TEXT:
                return new TextField();
            case BOOLEAN:
                return new BooleanField();
            case DATE:
                return new DateField();
            case FILE:
                return new FileField();
            case ENUMERATION:
                return new EnumerationField(values);
            case MULTICHOICE:
                return new MultichoiceField(values);
            default:
                throw new IllegalArgumentException(type + " is not a valid Field type");
        }
    }

    public static Field getField(String typeString, String[] values) throws IllegalArgumentException {
        return getField(FieldType.fromString(typeString), values);
    }
}
