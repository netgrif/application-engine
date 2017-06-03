package com.netgrif.workflow.petrinet.web.requestbodies;


import java.util.List;

public class PetriNetReferenceBody {

    public List<String> petriNets;
    public List<String> transitions;

    public PetriNetReferenceBody() {}
}
