package com.netgrif.application.engine.petrinet.domain.dataset.logic;

import com.netgrif.application.engine.importer.model.Behavior;

import java.util.Arrays;
import java.util.List;

public enum FieldBehavior {
    REQUIRED("required") {
        @Override
        public List<FieldBehavior> getAntonyms() {
            return Arrays.asList(VISIBLE, HIDDEN, OPTIONAL, FORBIDDEN);
        }
    },
    OPTIONAL("optional") {
        @Override
        public List<FieldBehavior> getAntonyms() {
            return Arrays.asList(REQUIRED, FORBIDDEN);
        }
    },
    VISIBLE("visible") {
        @Override
        public List<FieldBehavior> getAntonyms() {
            return Arrays.asList(REQUIRED, EDITABLE, HIDDEN, FORBIDDEN);
        }
    },
    EDITABLE("editable") {
        @Override
        public List<FieldBehavior> getAntonyms() {
            return Arrays.asList(VISIBLE, HIDDEN, FORBIDDEN);
        }
    },
    HIDDEN("hidden") {
        @Override
        public List<FieldBehavior> getAntonyms() {
            return Arrays.asList(EDITABLE, VISIBLE, REQUIRED, FORBIDDEN);
        }
    },
    FORBIDDEN("forbidden") {
        @Override
        public List<FieldBehavior> getAntonyms() {
            return Arrays.asList(VISIBLE, EDITABLE, HIDDEN, REQUIRED, OPTIONAL);
        }
    },
    IMMEDIATE("immediate") {
        @Override
        public List<FieldBehavior> getAntonyms() {
            return List.of();
        }
    };

    private final String name;

    public abstract List<FieldBehavior> getAntonyms();

    FieldBehavior(String name) {
        this.name = name;
    }

    public static FieldBehavior fromString(Behavior string) {
        if (string == null)
            throw new IllegalArgumentException("Behavior can not be null");
        return valueOf(string.value().toUpperCase());
    }

    @Override
    public String toString() {
        return name;
    }
}