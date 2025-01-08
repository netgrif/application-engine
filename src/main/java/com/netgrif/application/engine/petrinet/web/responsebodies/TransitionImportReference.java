package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.workflow.domain.Node;
import com.netgrif.application.engine.workflow.domain.Transition;
import lombok.Data;

@Data
public class TransitionImportReference extends Node {

    public TransitionImportReference(Transition transition) {
        this.setTitle(transition.getTitle());
        this.setObjectId(transition.getObjectId());
        this.setImportId(transition.getImportId());
    }
}
