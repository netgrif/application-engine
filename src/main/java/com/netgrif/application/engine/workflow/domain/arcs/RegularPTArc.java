package com.netgrif.application.engine.workflow.domain.arcs;

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
        clone.setSourceId(this.sourceId);
        clone.setDestinationId(this.destinationId);
        clone.setMultiplicityExpression(this.multiplicityExpression.clone());
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        return clone;
    }
}
