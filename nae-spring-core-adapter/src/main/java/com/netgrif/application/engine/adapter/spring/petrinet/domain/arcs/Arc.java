package com.netgrif.application.engine.adapter.spring.petrinet.domain.arcs;

import com.netgrif.application.engine.objects.petrinet.domain.Node;
import org.springframework.data.annotation.Transient;

public class Arc extends com.netgrif.application.engine.objects.petrinet.domain.arcs.Arc {

    public Arc() {
        super();
    }

    public Arc(com.netgrif.application.engine.objects.petrinet.domain.arcs.Arc arc) {
        super(arc);
    }

    public Arc(Arc arc) {
        super(arc);
    }

    public Arc(Node source, Node destination, int multiplicity) {
        super(source, destination, multiplicity);
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
