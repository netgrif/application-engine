package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.Arc;

public final class ArcFactory {
    public static Arc getArc(Arc.Type type) throws IllegalArgumentException {
        switch (type) {
            case REGULAR:
                return new Arc();
            default:
                throw new IllegalArgumentException(type+" is not a valid Arc type");
        }
    }

    public static Arc getArc(String type) throws IllegalArgumentException {
        return getArc(Arc.Type.fromString(type));
    }
}
