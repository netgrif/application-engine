package com.netgrif.application.engine.petrinet.web.responsebodies;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PetriNetImportReference {

    private List<TransitionImportReference> transitions;

    private List<PlaceImportReference> places;

    private List<ArcImportReference> arcs;

    private List<String> assignedTasks;

    private List<String> finishedTasks;

    public PetriNetImportReference() {
        this.transitions = new ArrayList<>();
        this.places = new ArrayList<>();
        this.arcs = new ArrayList<>();
        this.assignedTasks = new ArrayList<>();
        this.finishedTasks = new ArrayList<>();
    }
}
