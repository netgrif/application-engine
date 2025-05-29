package com.netgrif.application.engine.petrinet.domain.arcs;

import com.netgrif.application.engine.petrinet.domain.Place;
import com.netgrif.application.engine.petrinet.domain.Transition;

/**
 * Arcs that can only lead from Transition to Place (thus TP).
 */
public abstract class TPArc extends Arc<Transition, Place> {

    public abstract TPArc clone();
}
