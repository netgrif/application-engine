package com.netgrif.workflow.petrinet.domain.arcs;

import com.netgrif.workflow.petrinet.domain.Place;
import lombok.Getter;
import lombok.Setter;

/**
 * Reset arc does not alter the enabling condition, but involve a change of the marking on <i>p</i> by firing of <i>t</i>:
 * <ul>
 *     <li><i>m'(p)</i> = 0, if <i>p</i> is not also a postplace of <i>t</i></li>
 *     <li><i>m'(p)</i> = <i>W(t,p)</i>, if <i>p</i> is also a postplace of <i>t</i></li>
 * </ul>
 * <a href="https://books.google.sk/books?id=A45rCQAAQBAJ&dq=petri+net+read+arc&hl=sk">More info</a>
 */
public class ResetArc extends PTArc {

    @Getter
    @Setter
    private Integer removedTokens;

    /**
     * Always returns true, because Reset arc does not alter the enabling condition.
     * @return true
     */
    @Override
    public boolean isExecutable() {
        return true;
    }

    /**
     * Changes the marking on <i>p</i> by firing of <i>t</i>:
     * <ul>
     *     <li><i>m'(p)</i> = 0, if <i>p</i> is not also a postplace of <i>t</i></li>
     *     <li><i>m'(p)</i> = <i>W(t,p)</i>, if <i>p</i> is also a postplace of <i>t</i></li>
     * </ul>
     */
    @Override
    public void execute() {
        Place place = ((Place) source);
        removedTokens = place.getTokens();
        place.removeAllTokens();
    }

    @Override
    public void rollbackExecution() {
        ((Place) source).addTokens(removedTokens);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public ResetArc clone() {
        ResetArc clone = new ResetArc();
        clone.setSourceId(this.sourceId);
        clone.setDestinationId(this.destinationId);
        clone.setMultiplicity(this.multiplicity);
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        clone.setRemovedTokens(this.removedTokens);
        return clone;
    }
}