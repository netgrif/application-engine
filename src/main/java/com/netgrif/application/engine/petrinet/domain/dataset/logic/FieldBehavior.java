package com.netgrif.application.engine.petrinet.domain.dataset.logic;

import com.netgrif.application.engine.importer.model.Behavior;

public enum FieldBehavior {
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

    // TODO: release/8.0.0 replace FieldBehavior with importer Behavior
    public static FieldBehavior fromXml(Behavior xmlBehavior) {
        if (xmlBehavior == null) {
            throw new IllegalArgumentException("Behavior can not be null");
        }
        return valueOf(xmlBehavior.value().toUpperCase());
    }

    @Override
    public String toString() {
        return name;
    }

    public static FieldBehavior defaultValue() {
        return FieldBehavior.EDITABLE;
    }
}