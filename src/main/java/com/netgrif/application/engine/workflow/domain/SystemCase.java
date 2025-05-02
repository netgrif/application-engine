package com.netgrif.application.engine.workflow.domain;

import javax.validation.constraints.NotNull;

public abstract class SystemCase {

    @NotNull
    protected final Case systemCase;

    public SystemCase(Case systemCase) {
        if (systemCase == null) {
            throw new NullPointerException("Cannot initialize system case object. Provided case is null");
        }
        CanInitializeOutcome canInitialize = canInitialize(systemCase);
        if (!canInitialize.value) {
            throw new IllegalArgumentException(String.format("Cannot create system case: %s", canInitialize.message));
        }
        this.systemCase = systemCase;
    }

    /**
     * todo javadoc
     * */
    protected abstract CanInitializeOutcome canInitialize(Case systemCase);

    public Case getCase() {
        return this.systemCase;
    }

    public String getStringId() {
        return this.getCase().getStringId();
    }

    protected static class CanInitializeOutcome {
        protected final String message;
        protected final boolean value;

        public CanInitializeOutcome(String message, boolean value) {
            this.message = message;
            this.value = value;
        }
    }
}
