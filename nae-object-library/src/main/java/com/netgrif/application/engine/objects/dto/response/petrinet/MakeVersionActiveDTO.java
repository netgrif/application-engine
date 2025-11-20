package com.netgrif.application.engine.objects.dto.response.petrinet;

import java.io.Serializable;

public record MakeVersionActiveDTO(String activatedProcessId, String inactivatedProcessId) implements Serializable {

    public MakeVersionActiveDTO() {
        this(null, null);
    }
}
