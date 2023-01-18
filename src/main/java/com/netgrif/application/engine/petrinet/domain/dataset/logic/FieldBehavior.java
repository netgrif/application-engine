package com.netgrif.application.engine.petrinet.domain.dataset.logic;

import com.netgrif.application.engine.importer.model.Behavior;

public enum FieldBehavior {
    // TODO: NAE-1645
    //  com.netgrif.application.engine.petrinet.domain.ImporterTest#initialBehaviorTest
    //  No enum constant com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.IMMEDIATE
    VISIBLE("visible"),
    EDITABLE("editable"),
    HIDDEN("hidden"),
    FORBIDDEN("forbidden");

    private final String name;

    FieldBehavior(String name) {
        this.name = name;
    }

    public boolean isDisplayable() {
        return this == EDITABLE || this == VISIBLE || this == HIDDEN;
    }

    public static FieldBehavior fromString(Behavior string) {
        if (string == null) {
            throw new IllegalArgumentException("Behavior can not be null");
        }
        return valueOf(string.value().toUpperCase());
    }

    @Override
    public String toString() {
        return name;
    }

    public static FieldBehavior defaultValue() {
        return FieldBehavior.EDITABLE;
    }
}