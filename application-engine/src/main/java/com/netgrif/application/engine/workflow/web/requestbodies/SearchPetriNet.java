package com.netgrif.application.engine.workflow.web.requestbodies;


import java.util.List;
import java.util.Map;

public class SearchPetriNet {

    public String petriNet;
    public List<String> transitions;
    public Map<String, Object> dataSet;

    public SearchPetriNet() {
    }
}
