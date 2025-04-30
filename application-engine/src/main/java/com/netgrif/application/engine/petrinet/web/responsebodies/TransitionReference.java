package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.workflow.web.responsebodies.DataFieldReference;
import lombok.Data;

import java.util.List;

@Data
public class TransitionReference extends Reference {

    private String petriNetId;

    private List<DataFieldReference> immediateData;

    public TransitionReference() {
        super();
    }

    public TransitionReference(String id, String title, String petriNetId, List<DataFieldReference> immediate) {
        super(id, title);
        this.petriNetId = petriNetId;
        this.immediateData = immediate;
    }

    public TransitionReference(String id, String title, PetriNetReference net) {
        super(id, title);
        this.petriNetId = net.getStringId();
    }
}
