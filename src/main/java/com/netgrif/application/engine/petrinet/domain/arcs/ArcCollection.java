package com.netgrif.application.engine.petrinet.domain.arcs;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ArcCollection {
    // TODO: release/8.0.0 save sorted by execution priority
    private List<PTArc> input;
    private List<TPArc> output;

    public ArcCollection() {
        input = new ArrayList<>();
        output = new ArrayList<>();
    }

    public void addInput(PTArc arc) {
        this.input.add(arc);
    }

    public void addOutput(TPArc arc) {
        this.output.add(arc);
    }

    public ArcCollection clone() {
        ArcCollection cloned = new ArcCollection();
        cloned.input = this.input.stream().map(PTArc::clone).collect(Collectors.toList());
        cloned.output = this.output.stream().map(TPArc::clone).collect(Collectors.toList());
        return cloned;
    }
}
