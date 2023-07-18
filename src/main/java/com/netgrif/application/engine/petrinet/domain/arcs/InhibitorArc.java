package com.netgrif.application.engine.petrinet.domain.arcs;

import com.netgrif.application.engine.petrinet.domain.Place;

/**
 * If there is an arc <i>a</i> with a weight <i>w=W(p,t)</i> connecting a place <i>p</i> with a transition <i>t</i>,
 * then <i>t</i> can be enabled in a marking <i>m</i> if the following condition is satisfied:<br>
 * <center><b><i>m(p) < w</i></b><br></center>
 * <a href="https://books.google.sk/books?id=A45rCQAAQBAJ&dq=petri+net+read+arc&hl=sk">More info</a>
 */
public class InhibitorArc extends PTArc {

    /**
     * Returns true if source place has fewer tokens then arc weight.
     *
     * @return true if <br>
     * <center><b><i>m(p) < w</i></b><br></center>
     * false otherwise.
     */
    @Override
    public boolean isExecutable() {
        if(this.reference != null) multiplicity = this.reference.getMultiplicity();
        return ((Place) source).getTokens() < multiplicity;
    }

    /**
     * Does nothing. The token situation on <i>p</i> is not changed by the firing of <i>t</i>, i.e. <i>m'(p)</i> = <i>m(p)</i>.
     */
    @Override
    public void execute() {
    }

    /**
     * Does nothing. The token situation on <i>p</i> is not changed by the firing of <i>t</i>, i.e. <i>m'(p)</i> = <i>m(p)</i>.
     */
    @Override
    public void rollbackExecution(Integer tokensConsumed) {
    }

    @SuppressWarnings("Duplicates")
    @Override
    public InhibitorArc clone() {
        InhibitorArc clone = new InhibitorArc();
        clone.setSourceId(this.sourceId);
        clone.setDestinationId(this.destinationId);
        clone.setMultiplicity(this.multiplicity);
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        clone.setReference(this.reference == null ? null : this.reference.clone());
        return clone;
    }
}