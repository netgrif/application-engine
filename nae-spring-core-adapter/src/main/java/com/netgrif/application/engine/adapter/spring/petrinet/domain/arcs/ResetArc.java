package com.netgrif.application.engine.adapter.spring.petrinet.domain.arcs;

import com.netgrif.application.engine.objects.petrinet.domain.Node;
import org.springframework.data.annotation.Transient;

public class ResetArc extends com.netgrif.application.engine.objects.petrinet.domain.arcs.ResetArc {

    public ResetArc() {
        super();
    }

    public ResetArc(com.netgrif.application.engine.objects.petrinet.domain.arcs.ResetArc arc) {
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
