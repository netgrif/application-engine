package com.netgrif.application.engine.petrinet.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class Imported implements Serializable {

    private static final long serialVersionUID = -2708949961379974800L;

    protected String importId;
}