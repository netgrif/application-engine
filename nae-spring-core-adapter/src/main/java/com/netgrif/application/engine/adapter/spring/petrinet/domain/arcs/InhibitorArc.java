package com.netgrif.application.engine.adapter.spring.petrinet.domain.arcs;

import com.netgrif.application.engine.objects.petrinet.domain.Node;
import org.springframework.data.annotation.Transient;

public class InhibitorArc extends com.netgrif.application.engine.objects.petrinet.domain.arcs.InhibitorArc {

    public InhibitorArc() {
        super();
    }

    public InhibitorArc(com.netgrif.application.engine.objects.petrinet.domain.arcs.InhibitorArc arc) {
        super(arc);
    }

    public InhibitorArc(InhibitorArc arc) {
        super(arc);
    }

    @Override
    @Transient
    public Node getDestination() {
        return super.getDestination();
    }

    @Override
    @Transient
    public Node getSource() {
        return super.getSource();
    }
}
