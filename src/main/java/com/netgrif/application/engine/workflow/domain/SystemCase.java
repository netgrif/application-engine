package com.netgrif.application.engine.workflow.domain;

public interface SystemCase {
    Case getCase();

    default String getStringId() {
        return this.getCase().getStringId();
    }
}
