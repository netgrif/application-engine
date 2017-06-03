package com.netgrif.workflow.workflow.web.requestbodies;


import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;

import java.util.List;

public class CreateFilterBody {

    public static final int GLOBAL = 0;
    public static final int ORGANIZATION = 1;
    public static final int PRIVATE = 2;

    public String name;
    public int visibility;
    public List<PetriNetReference> petriNets;
    public List<TransitionReference> transitions;
    public List<String> roles;

}
