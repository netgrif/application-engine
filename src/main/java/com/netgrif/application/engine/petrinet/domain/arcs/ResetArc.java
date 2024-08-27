package com.netgrif.application.engine.petrinet.domain.arcs;

/**
 * Reset arc does not alter the enabling condition, but involve a change of the marking on <i>p</i> by firing of <i>t</i>:
 * <ul>
 *     <li><i>m'(p)</i> = 0, if <i>p</i> is not also a postplace of <i>t</i></li>
 *     <li><i>m'(p)</i> = <i>W(t,p)</i>, if <i>p</i> is also a postplace of <i>t</i></li>
 * </ul>
 * <a href="https://books.google.sk/books?id=A45rCQAAQBAJ&dq=petri+net+read+arc&hl=sk">More info</a>
 */
public class ResetArc extends PTArc {

    /**
     * Always returns true, because Reset arc does not alter the enabling condition.
     *
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
        source.removeAllTokens();
    }

    @Override
    public void rollbackExecution(Integer tokensConsumed) {
        source.addTokens(tokensConsumed);
    }

    @Override
    public ResetArc clone() {
        ResetArc clone = new ResetArc();
        clone.setSource(this.source);
        clone.setDestination(this.destination);
        clone.setMultiplicityExpression(this.multiplicityExpression.clone());
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        return clone;
    }
}