package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

import lombok.Getter;

public enum FieldBehavior {
    REQUIRED("required"),
    OPTIONAL("optional"),
    VISIBLE("visible"),
    EDITABLE("editable"),
    HIDDEN("hidden"),
    FORBIDDEN("forbidden"),
    IMMEDIATE("immediate"),
    ANTONYM_SETUP("", true);

    private final String name;

    @Getter
    private FieldBehavior[] antonyms;

    FieldBehavior(String name) {
        this.name = name;
    }

    FieldBehavior(String name, boolean populate) {
        this.name = name;
        if (populate) {
            initAntonyms();
        }
    }

    private static void initAntonyms() {
        REQUIRED.setAntonyms();
        OPTIONAL.setAntonyms();
        VISIBLE.setAntonyms();
        EDITABLE.setAntonyms();
        HIDDEN.setAntonyms();
        FORBIDDEN.setAntonyms();
        IMMEDIATE.setAntonyms();
    }

    private FieldBehavior[] addAntonyms() {
        return switch (name) {
            case "required" -> new FieldBehavior[]{VISIBLE, HIDDEN, OPTIONAL, FORBIDDEN};
            case "optional" -> new FieldBehavior[]{REQUIRED, FORBIDDEN};
            case "visible" -> new FieldBehavior[]{REQUIRED, EDITABLE, HIDDEN, FORBIDDEN};
            case "editable" -> new FieldBehavior[]{VISIBLE, HIDDEN, FORBIDDEN};
            case "hidden" -> new FieldBehavior[]{EDITABLE, VISIBLE, REQUIRED, FORBIDDEN};
            case "forbidden" -> new FieldBehavior[]{VISIBLE, EDITABLE, HIDDEN, REQUIRED, OPTIONAL};
            case "immediate" -> new FieldBehavior[]{};
            default -> null;
        };
    }

    public static FieldBehavior fromString(String string) {
        if (string == null) throw new IllegalArgumentException("Bahavior can not be null");
        return valueOf(string.toUpperCase());
    }

    private void setAntonyms() {
        this.antonyms = addAntonyms();
    }

    @Override
    public String toString() {
        return name;
    }
}
