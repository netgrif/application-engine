package com.netgrif.application.engine.petrinet.domain.arcs;

import com.netgrif.application.engine.petrinet.domain.Node;
import com.netgrif.application.engine.petrinet.domain.Place;
import com.netgrif.application.engine.petrinet.domain.Transition;

/**
 * Special arcs that can only lead from Place to Transition (thus PT).
 */
public abstract class PTArc extends Arc {

    /**
     * Sets source of this arc.
     *
     * @param source Node object of class Place
     * @throws IllegalArgumentException if <i>source</i> is of class Transition
     */
    @Override
    public void setSource(Node source) {
        if (source instanceof Transition)
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " can not lead from a Transition");
        super.setSource(source);
    }

    /**
     * Sets destination of this arc.
     *
     * @param destination Node object of class Transition
     * @throws IllegalArgumentException if <i>destination</i> is of class Place
     */
    @Override
    public void setDestination(Node destination) {
        if (destination instanceof Place)
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " can not lead to a Place");
        super.setDestination(destination);
    }
}