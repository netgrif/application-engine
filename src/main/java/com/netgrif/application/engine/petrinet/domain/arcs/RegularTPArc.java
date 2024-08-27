package com.netgrif.application.engine.petrinet.domain.arcs;

public class RegularTPArc extends TPArc {

    @Override
    public void execute() {
        this.destination.addTokens(this.getMultiplicity());
    }

    @Override
    public RegularTPArc clone() {
        RegularTPArc clone = new RegularTPArc();
        clone.setSource(this.source);
        clone.setDestination(this.destination);
        clone.setMultiplicityExpression(this.multiplicityExpression.clone());
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        return clone;
    }
}
