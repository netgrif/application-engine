package com.netgrif.application.engine.workflow.domain.arcs;

import com.netgrif.application.engine.workflow.domain.Place;
import com.netgrif.application.engine.workflow.domain.Transition;

/**
 * Arcs that can only lead from Transition to Place (thus TP).
 */
public abstract class TPArc extends Arc<Transition, Place> {

    public abstract TPArc clone();
}
