package com.netgrif.application.engine.adapter.spring.petrinet.domain.arcs;

import com.netgrif.application.engine.objects.petrinet.domain.Node;
import org.springframework.data.annotation.Transient;

public class ReadArc extends com.netgrif.application.engine.objects.petrinet.domain.arcs.ReadArc {

    public ReadArc() {
        super();
    }

    public ReadArc(ReadArc readArc) {
        super(readArc);
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
