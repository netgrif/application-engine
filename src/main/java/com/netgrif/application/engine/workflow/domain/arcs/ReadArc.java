package com.netgrif.application.engine.workflow.domain.arcs;

import com.netgrif.application.engine.petrinet.domain.throwable.IllegalMarkingException;

/**
 * If there is an arc <i>a</i> with a weight <i>w=W(p,t)</i> connecting a place <i>p</i> with a transition <i>t</i>,
 * then <i>t</i> can be enabled in a marking <i>m</i> if the following condition is satisfied:<br>
 * <center><b><i>m(p) &ge; w</i></b><br></center>
 * <a href="https://books.google.sk/books?id=A45rCQAAQBAJ&dq=petri+net+read+arc&hl=sk">More info</a>
 */
public class ReadArc extends PTArc {

    /**
     * Returns true if source place has fewer tokens then arc weight.
     *
     * @return true if <br>
     * <center><b><i>m(p) &ge; w</i></b><br></center>
     * false otherwise.
     */
    @Override
    public boolean isExecutable() {
        return source.getTokens() >= this.getMultiplicity();
    }

    /**
     * Does nothing. The token situation on <i>p</i> is not changed by the firing of <i>t</i>, i.e. <i>m'(p)</i> = <i>m(p)</i>.
     */
    @Override
    public void execute() {
        if (!this.isExecutable()) {
            throw new IllegalMarkingException(this.source);
        }
    }

    /**
     * Does nothing. The token situation on <i>p</i> is not changed by the firing of <i>t</i>, i.e. <i>m'(p)</i> = <i>m(p)</i>.
     */
    @Override
    public void rollbackExecution(Integer tokensConsumed) {
    }

    @SuppressWarnings("Duplicates")
    @Override
    public ReadArc clone() {
        ReadArc clone = new ReadArc();
        clone.setSourceId(this.sourceId);
        clone.setDestinationId(this.destinationId);
        clone.setMultiplicityExpression(this.multiplicityExpression.clone());
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        return clone;
    }
}