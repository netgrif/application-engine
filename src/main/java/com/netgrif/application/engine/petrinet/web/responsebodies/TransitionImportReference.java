package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.Node;
import com.netgrif.application.engine.petrinet.domain.Transition;
import lombok.Data;

@Data
public class TransitionImportReference extends Node {

    public TransitionImportReference(Transition transition) {
        this.setPosition(transition.getPosition().getX(), transition.getPosition().getY());
        this.setTitle(transition.getTitle());
        this.setObjectId(transition.getObjectId());
        this.setImportId(transition.getImportId());
    }
}
