package com.netgrif.application.engine.petrinet.domain.arcs;

public class RegularPTArc extends PTArc {

    @Override
    public boolean isExecutable() {
        return source.getTokens() >= this.getMultiplicity();
    }

    @Override
    public void execute() {
        source.removeTokens(this.getMultiplicity());
    }

    @Override
    public void rollbackExecution(Integer tokensConsumed) {
        source.addTokens(tokensConsumed);
    }

    @Override
    public RegularPTArc clone() {
        RegularPTArc clone = new RegularPTArc();
        clone.setSource(this.source);
        clone.setDestination(this.destination);
        clone.setMultiplicityExpression(this.multiplicityExpression.clone());
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        return clone;
    }
}
