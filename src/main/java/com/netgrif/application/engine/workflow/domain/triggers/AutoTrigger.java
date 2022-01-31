package com.netgrif.application.engine.workflow.domain.triggers;

public class AutoTrigger extends Trigger {

    public AutoTrigger() {
        super();
    }

    @Override
    public Trigger clone() {
        return new AutoTrigger();
    }
}