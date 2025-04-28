package com.netgrif.application.engine.objects.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class Imported implements Serializable {

    @Serial
    private static final long serialVersionUID = -2708949961379974800L;

    protected String importId;
}
