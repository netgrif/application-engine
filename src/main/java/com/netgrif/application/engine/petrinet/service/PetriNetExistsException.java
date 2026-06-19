package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.petrinet.domain.PetriNet;

public class PetriNetExistsException extends RuntimeException {

    public PetriNetExistsException(PetriNet net) {
        super("Petri net with identifier " + net.getIdentifier() + " and version " + net.getVersion() + " already exists.");
    }
}
