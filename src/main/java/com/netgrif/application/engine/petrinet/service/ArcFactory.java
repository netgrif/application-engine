package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.arcs.InhibitorArc;
import com.netgrif.application.engine.petrinet.domain.arcs.ReadArc;
import com.netgrif.application.engine.petrinet.domain.arcs.ResetArc;
import com.netgrif.application.engine.petrinet.domain.arcs.reference.Reference;
import com.netgrif.application.engine.petrinet.domain.arcs.reference.Type;
import org.springframework.stereotype.Component;

@Component
public final class ArcFactory {

    public Arc getArc(com.netgrif.application.engine.importer.model.Arc arc) throws IllegalArgumentException {
        switch (arc.getType()) {
            case REGULAR:
                return new Arc();
            case RESET:
                return new ResetArc();
            case INHIBITOR:
                return new InhibitorArc();
            case READ:
                return new ReadArc();
            case VARIABLE:
                Arc varArc = new Arc();
                Reference ref = new Reference();
                ref.setReference(String.valueOf(arc.getMultiplicity()));
                varArc.setReference(ref);
                return varArc;
            default:
                throw new IllegalArgumentException(arc.getType() + " is not a valid Arc type");
        }
    }
}