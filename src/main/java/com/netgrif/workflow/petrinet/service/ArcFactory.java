package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.petrinet.domain.Arc;
import com.netgrif.workflow.petrinet.domain.InhibitorArc;
import com.netgrif.workflow.petrinet.domain.ReadArc;
import com.netgrif.workflow.petrinet.domain.ResetArc;
import org.springframework.stereotype.Component;

@Component
public final class ArcFactory {

    private Arc getArc(Type type) throws IllegalArgumentException {
        switch (type) {
            case REGULAR:
                return new Arc();
            case RESET:
                return new ResetArc();
            case INHIBITOR:
                return new InhibitorArc();
            case READ:
                return new ReadArc();
            default:
                throw new IllegalArgumentException(type+" is not a valid Arc type");
        }
    }

    public Arc getArc(String type) throws IllegalArgumentException {
        return getArc(Type.fromString(type));
    }

    public enum Type {
        REGULAR("regular"),
        INHIBITOR("inhibitor"),
        RESET("reset"),
        READ("read");

        String name;

        Type(String name) {
            this.name = name;
        }

        public static Type fromString(String name) {
            return Type.valueOf(name.toUpperCase());
        }
    }
}