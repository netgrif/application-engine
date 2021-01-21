package com.netgrif.workflow.petrinet.web.responsebodies;

import lombok.Data;
import com.netgrif.workflow.workflow.web.responsebodies.DataFieldReference;
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
