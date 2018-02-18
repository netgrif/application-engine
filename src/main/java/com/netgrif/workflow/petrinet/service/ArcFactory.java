package com.netgrif.workflow.petrinet.service;

import com.netgrif.workflow.petrinet.domain.Arc;
import com.netgrif.workflow.petrinet.domain.InhibitorArc;
import com.netgrif.workflow.petrinet.domain.ReadArc;
import com.netgrif.workflow.petrinet.domain.ResetArc;
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
            default:
                throw new IllegalArgumentException(arc.getType() + " is not a valid Arc type");
        }
    }
}