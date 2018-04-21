package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.petrinet.domain.arcs.Arc;
import com.netgrif.workflow.petrinet.domain.arcs.InhibitorArc;
import com.netgrif.workflow.petrinet.domain.arcs.ReadArc;
import com.netgrif.workflow.petrinet.domain.arcs.ResetArc;
import com.netgrif.workflow.petrinet.domain.arcs.VariableArc;
import org.springframework.stereotype.Component;

@Component
public final class ArcFactory {

    public Arc getArc(com.netgrif.workflow.importer.model.Arc arc) throws IllegalArgumentException {
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
                return new VariableArc(arc.getMultiplicity());
            default:
                throw new IllegalArgumentException(arc.getType() + " is not a valid Arc type");
        }
    }
}