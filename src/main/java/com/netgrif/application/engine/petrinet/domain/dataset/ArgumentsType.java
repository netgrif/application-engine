package com.netgrif.application.engine.petrinet.domain.dataset;

import java.util.Arrays;
import java.util.Objects;

public enum ArgumentsType {
    SERVER("server"),
    CLIENT("client");

    public final String type;

    ArgumentsType(String type) {
        this.type = type;
    }

    public static ArgumentsType fromString(String type) {
        return Arrays.stream(values()).filter(argumentsType -> Objects.equals(argumentsType.type, type)).findFirst().orElse(null);
    }
}
