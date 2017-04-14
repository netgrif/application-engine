package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.Arc;
import com.fmworkflow.petrinet.domain.InhibitorArc;
import com.fmworkflow.petrinet.domain.ResetArc;

public final class ArcFactory {
    public static Arc getArc(Type type) throws IllegalArgumentException {
        switch (type) {
            case REGULAR:
                return new Arc();
            case RESET:
                return new ResetArc();
            case INHIBITOR:
                return new InhibitorArc();
            default:
                throw new IllegalArgumentException(type+" is not a valid Arc type");
        }
    }

    public static Arc getArc(String type) throws IllegalArgumentException {
        return getArc(Type.fromString(type));
    }

    public enum Type {
        REGULAR("regular"),
        INHIBITOR("inhibitor"),
        RESET("reset");

        String name;

        Type(String name) {
            this.name = name;
        }

        public static Type fromString(String name) {
            return Type.valueOf(name.toUpperCase());
        }
    }
}
