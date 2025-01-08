package com.netgrif.application.engine.workflow.domain.arcs;

import com.netgrif.application.engine.workflow.domain.Place;
import com.netgrif.application.engine.workflow.domain.Transition;

/**
 * Arcs that can only lead from Place to Transition (thus PT).
 */
public abstract class PTArc extends Arc<Place, Transition> {

    public abstract boolean isExecutable();

    public abstract void rollbackExecution(Integer tokensConsumed);

    public abstract PTArc clone();
}